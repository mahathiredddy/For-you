package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BusinessPlanEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HackathonEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.RoadmapPhaseEntity
import com.example.data.local.entity.SkillEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY updatedAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY updatedAt DESC LIMIT 1")
    fun getActiveGoal(): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getActiveGoalOnce(): GoalEntity?

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    fun getGoalById(id: Long): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalByIdOnce(id: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
}

@Dao
interface RoadmapDao {
    @Query("SELECT * FROM roadmap_phases WHERE goalId = :goalId ORDER BY phaseIndex ASC")
    fun getPhasesForGoal(goalId: Long): Flow<List<RoadmapPhaseEntity>>

    @Query("SELECT * FROM roadmap_phases WHERE goalId = :goalId ORDER BY phaseIndex ASC")
    suspend fun getPhasesForGoalOnce(goalId: Long): List<RoadmapPhaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhases(phases: List<RoadmapPhaseEntity>): List<Long>

    @Update
    suspend fun updatePhase(phase: RoadmapPhaseEntity)

    @Query("DELETE FROM roadmap_phases WHERE goalId = :goalId")
    suspend fun deletePhasesForGoal(goalId: Long)

    @Query("SELECT * FROM milestones WHERE goalId = :goalId ORDER BY orderIndex ASC")
    fun getMilestonesForGoal(goalId: Long): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE goalId = :goalId ORDER BY orderIndex ASC")
    suspend fun getMilestonesForGoalOnce(goalId: Long): List<MilestoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>): List<Long>

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    @Query("DELETE FROM milestones WHERE goalId = :goalId")
    suspend fun deleteMilestonesForGoal(goalId: Long)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE goalId = :goalId ORDER BY orderIndex ASC, id ASC")
    fun getTasksForGoal(goalId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE goalId = :goalId ORDER BY orderIndex ASC, id ASC")
    suspend fun getTasksForGoalOnce(goalId: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isTodayTask = 1 AND status != 'COMPLETED' ORDER BY priority ASC, id ASC")
    fun getActiveTodayTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isTodayTask = 1 ORDER BY status ASC, priority ASC, id ASC")
    fun getAllTodayTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>): List<Long>

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE goalId = :goalId")
    suspend fun deleteTasksForGoal(goalId: Long)
}

@Dao
interface CheckInDao {
    @Query("SELECT * FROM daily_check_ins WHERE goalId = :goalId ORDER BY timestamp DESC")
    fun getCheckInsForGoal(goalId: Long): Flow<List<DailyCheckInEntity>>

    @Query("SELECT * FROM daily_check_ins ORDER BY timestamp DESC")
    fun getAllCheckIns(): Flow<List<DailyCheckInEntity>>

    @Query("SELECT * FROM daily_check_ins ORDER BY timestamp DESC LIMIT 7")
    suspend fun getRecentCheckInsOnce(): List<DailyCheckInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: DailyCheckInEntity): Long
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    suspend fun getAllMemoriesOnce(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE goalId = :goalId ORDER BY timestamp DESC")
    fun getMemoriesForGoal(goalId: Long): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE goalId = :goalId ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesForGoal(goalId: Long): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY currentLevel DESC, practiceHours DESC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity): Long

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Delete
    suspend fun deleteSkill(skill: SkillEntity)
}

@Dao
interface HackathonDao {
    @Query("SELECT * FROM hackathons ORDER BY createdAt DESC")
    fun getAllHackathons(): Flow<List<HackathonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHackathon(hackathon: HackathonEntity): Long

    @Update
    suspend fun updateHackathon(hackathon: HackathonEntity)

    @Delete
    suspend fun deleteHackathon(hackathon: HackathonEntity)
}

@Dao
interface BusinessDao {
    @Query("SELECT * FROM business_plans ORDER BY createdAt DESC")
    fun getAllBusinessPlans(): Flow<List<BusinessPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinessPlan(plan: BusinessPlanEntity): Long

    @Update
    suspend fun updateBusinessPlan(plan: BusinessPlanEntity)

    @Delete
    suspend fun deleteBusinessPlan(plan: BusinessPlanEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesOnce(): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()
}
