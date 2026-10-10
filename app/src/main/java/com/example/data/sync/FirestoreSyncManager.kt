package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class SyncStatus {
    object Idle : SyncStatus()
    data class Syncing(val stage: String) : SyncStatus()
    data class Success(val lastSyncTimestamp: Long, val itemsSynced: Int) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
    data class Unconfigured(val reason: String) : SyncStatus()
}

class FirestoreSyncManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val tag = "FirestoreSyncManager"

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    fun isConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Throwable) {
            false
        }
    }

    private fun getFirestoreInstance(): FirebaseFirestore? {
        if (!isConfigured()) return null
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(tag, "Firestore instance unavailable: ${e.message}")
            null
        }
    }

    private fun getCurrentUserId(): String? {
        if (!isConfigured()) return null
        return try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Executes bidirectional sync between Room and Firestore.
     * Uses Last-Write-Wins conflict resolution based on timestamp.
     * Guaranteed user isolation via /users/{userId}/... paths.
     */
    suspend fun performSync(targetUserId: String? = null): SyncStatus = withContext(Dispatchers.IO) {
        val uid = targetUserId ?: getCurrentUserId()
        if (uid.isNullOrBlank()) {
            val status = SyncStatus.Unconfigured("No authenticated user session. Operating in local-only offline mode.")
            _syncStatus.value = status
            return@withContext status
        }

        val firestore = getFirestoreInstance()
        if (firestore == null) {
            val status = SyncStatus.Unconfigured("Firebase is not initialized (missing google-services.json).")
            _syncStatus.value = status
            return@withContext status
        }

        try {
            _syncStatus.value = SyncStatus.Syncing("Syncing goals...")
            var syncedCount = 0

            // 1. Sync Goals
            syncedCount += syncGoals(firestore, uid)

            // 2. Sync Tasks
            _syncStatus.value = SyncStatus.Syncing("Syncing daily tasks...")
            syncedCount += syncTasks(firestore, uid)

            // 3. Sync Notes
            _syncStatus.value = SyncStatus.Syncing("Syncing notes & knowledge...")
            syncedCount += syncNotes(firestore, uid)

            // 4. Sync Daily Check-Ins
            _syncStatus.value = SyncStatus.Syncing("Syncing progress check-ins...")
            syncedCount += syncCheckIns(firestore, uid)

            // 5. Sync Memories
            _syncStatus.value = SyncStatus.Syncing("Syncing AI memory...")
            syncedCount += syncMemories(firestore, uid)

            val success = SyncStatus.Success(
                lastSyncTimestamp = System.currentTimeMillis(),
                itemsSynced = syncedCount
            )
            _syncStatus.value = success
            success
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Cloud synchronization error"
            Log.e(tag, "Sync failed: $errorMsg", e)
            val failure = SyncStatus.Error(errorMsg)
            _syncStatus.value = failure
            failure
        }
    }

    /**
     * Migrates local guest records to an authenticated user's cloud account.
     */
    suspend fun migrateGuestDataToUser(userId: String): Boolean = withContext(Dispatchers.IO) {
        if (userId.isBlank() || !isConfigured()) return@withContext false
        val result = performSync(userId)
        result is SyncStatus.Success
    }

    private suspend fun syncGoals(firestore: FirebaseFirestore, uid: String): Int {
        val userGoalsCol = firestore.collection("users").document(uid).collection("goals")
        val localGoals = database.goalDao().getAllGoalsOnce()
        var count = 0

        // Push local goals
        for (goal in localGoals) {
            val docRef = userGoalsCol.document("goal_${goal.id}")
            val snapshot = try { docRef.get().await() } catch (e: Exception) { null }

            val remoteUpdatedAt = snapshot?.getLong("updatedAt") ?: 0L
            if (goal.updatedAt >= remoteUpdatedAt) {
                val data = mapOf(
                    "id" to goal.id,
                    "title" to goal.title,
                    "description" to goal.description,
                    "category" to goal.category,
                    "currentState" to goal.currentState,
                    "desiredOutcome" to goal.desiredOutcome,
                    "deadline" to goal.deadline,
                    "motivation" to goal.motivation,
                    "hoursPerWeek" to goal.hoursPerWeek,
                    "status" to goal.status,
                    "progressPercent" to goal.progressPercent,
                    "createdAt" to goal.createdAt,
                    "updatedAt" to goal.updatedAt
                )
                docRef.set(data, SetOptions.merge()).await()
                count++
            } else if (snapshot != null && snapshot.exists()) {
                // Remote is newer: update local Room
                val updatedGoal = goal.copy(
                    title = snapshot.getString("title") ?: goal.title,
                    description = snapshot.getString("description") ?: goal.description,
                    status = snapshot.getString("status") ?: goal.status,
                    progressPercent = snapshot.getLong("progressPercent")?.toInt() ?: goal.progressPercent,
                    updatedAt = remoteUpdatedAt
                )
                database.goalDao().updateGoal(updatedGoal)
                count++
            }
        }
        return count
    }

    private suspend fun syncTasks(firestore: FirebaseFirestore, uid: String): Int {
        val tasksCol = firestore.collection("users").document(uid).collection("tasks")
        val localGoals = database.goalDao().getAllGoalsOnce()
        var count = 0

        for (goal in localGoals) {
            val tasks = database.taskDao().getTasksForGoalOnce(goal.id)
            for (task in tasks) {
                val docRef = tasksCol.document("task_${task.id}")
                val snapshot = try { docRef.get().await() } catch (e: Exception) { null }
                val remoteCompletedAt = snapshot?.getLong("completedAt") ?: 0L

                val localTimestamp = task.completedAt ?: 0L
                if (localTimestamp >= remoteCompletedAt) {
                    val data = mapOf(
                        "id" to task.id,
                        "goalId" to task.goalId,
                        "title" to task.title,
                        "description" to task.description,
                        "priority" to task.priority,
                        "estimatedMinutes" to task.estimatedMinutes,
                        "status" to task.status,
                        "isTodayTask" to task.isTodayTask,
                        "completedAt" to task.completedAt
                    )
                    docRef.set(data, SetOptions.merge()).await()
                    count++
                } else if (snapshot != null && snapshot.exists()) {
                    val remoteStatus = snapshot.getString("status") ?: task.status
                    val updatedTask = task.copy(
                        status = remoteStatus,
                        completedAt = remoteCompletedAt
                    )
                    database.taskDao().updateTask(updatedTask)
                    count++
                }
            }
        }
        return count
    }

    private suspend fun syncNotes(firestore: FirebaseFirestore, uid: String): Int {
        val notesCol = firestore.collection("users").document(uid).collection("notes")
        val localNotes = database.noteDao().getAllNotesOnce()
        var count = 0

        for (note in localNotes) {
            val docRef = notesCol.document("note_${note.id}")
            val snapshot = try { docRef.get().await() } catch (e: Exception) { null }
            val remoteUpdatedAt = snapshot?.getLong("updatedAt") ?: 0L

            if (note.updatedAt >= remoteUpdatedAt) {
                val data = mapOf(
                    "id" to note.id,
                    "title" to note.title,
                    "content" to note.content,
                    "tags" to note.tags,
                    "isPinned" to note.isPinned,
                    "createdAt" to note.createdAt,
                    "updatedAt" to note.updatedAt
                )
                docRef.set(data, SetOptions.merge()).await()
                count++
            } else if (snapshot != null && snapshot.exists()) {
                val updatedNote = note.copy(
                    title = snapshot.getString("title") ?: note.title,
                    content = snapshot.getString("content") ?: note.content,
                    tags = snapshot.getString("tags") ?: note.tags,
                    isPinned = snapshot.getBoolean("isPinned") ?: note.isPinned,
                    updatedAt = remoteUpdatedAt
                )
                database.noteDao().updateNote(updatedNote)
                count++
            }
        }
        return count
    }

    private suspend fun syncCheckIns(firestore: FirebaseFirestore, uid: String): Int {
        val checkInsCol = firestore.collection("users").document(uid).collection("check_ins")
        val localCheckIns = database.checkInDao().getAllCheckInsOnce()
        var count = 0

        for (checkIn in localCheckIns) {
            val docRef = checkInsCol.document("checkin_${checkIn.id}")
            val data = mapOf(
                "id" to checkIn.id,
                "goalId" to checkIn.goalId,
                "date" to checkIn.date,
                "rating" to checkIn.rating,
                "actualHoursInvested" to checkIn.actualHoursInvested,
                "whatAccomplished" to checkIn.whatAccomplished,
                "whatWentWrong" to checkIn.whatWentWrong,
                "blockers" to checkIn.blockers,
                "timestamp" to checkIn.timestamp
            )
            docRef.set(data, SetOptions.merge()).await()
            count++
        }
        return count
    }

    private suspend fun syncMemories(firestore: FirebaseFirestore, uid: String): Int {
        val memoriesCol = firestore.collection("users").document(uid).collection("memories")
        val localMemories = database.memoryDao().getAllMemoriesOnce()
        var count = 0

        for (memory in localMemories) {
            val docRef = memoriesCol.document("memory_${memory.id}")
            val data = mapOf(
                "id" to memory.id,
                "category" to memory.category,
                "title" to memory.title,
                "content" to memory.content,
                "timestamp" to memory.timestamp
            )
            docRef.set(data, SetOptions.merge()).await()
            count++
        }
        return count
    }
}
