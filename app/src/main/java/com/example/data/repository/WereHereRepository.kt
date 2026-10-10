package com.example.data.repository

import com.example.data.ai.AIRepository
import com.example.data.ai.AIResponse
import com.example.data.ai.AiGoalAnalysis
import com.example.data.ai.AiService
import com.example.data.ai.NextMoveRecommendation
import com.example.data.ai.PlanAdaptationResult
import com.example.data.ai.RoadmapPlan
import com.example.data.ai.SmartRuleBasedAiService
import com.example.data.local.AppDatabase
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
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class WereHereRepository(
    private val db: AppDatabase,
    private val prefs: UserPreferencesRepository,
    val aiService: AiService
) {
    val aiRepository = AIRepository(aiService)

    // User Profile
    val userProfile: Flow<UserProfileEntity?> = db.userDao().getUserProfile()

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        db.userDao().upsertProfile(profile)
    }

    // Goals
    val allGoals: Flow<List<GoalEntity>> = db.goalDao().getAllGoals()
    val activeGoal: Flow<GoalEntity?> = db.goalDao().getActiveGoal()

    suspend fun createGoalWithAnalysis(
        title: String,
        description: String,
        category: String,
        currentState: String,
        desiredOutcome: String,
        deadline: String,
        motivation: String,
        hoursPerWeek: Float,
        budget: String,
        constraints: String
    ): Long {
        val profile = db.userDao().getUserProfileOnce() ?: UserProfileEntity()

        val tempGoal = GoalEntity(
            title = title,
            description = description,
            category = category,
            currentState = currentState,
            desiredOutcome = desiredOutcome,
            deadline = deadline,
            motivation = motivation,
            hoursPerWeek = hoursPerWeek,
            budget = budget,
            constraints = constraints,
            status = "ACTIVE"
        )

        // Perform AI Feasibility & Work Estimation
        val analysisResponse = aiRepository.analyzeGoal(tempGoal, profile)
        val analysis = when (analysisResponse) {
            is AIResponse.Success -> analysisResponse.data
            is AIResponse.Fallback -> analysisResponse.data
            else -> SmartRuleBasedAiService().generateInternalGoalAnalysis(tempGoal, profile)
        }

        val finalizedGoal = tempGoal.copy(
            feasibilityRating = analysis.feasibilityRating,
            feasibilityReasoning = analysis.feasibilityReasoning,
            highestValueFirstAction = analysis.highestValueFirstAction,
            progressPercent = 0
        )

        val goalId = db.goalDao().insertGoal(finalizedGoal)
        prefs.setSelectedGoalId(goalId)

        // Record Initial Memory
        db.memoryDao().insertMemory(
            MemoryEntity(
                goalId = goalId,
                category = "DECISION",
                title = "Goal Activated: $title",
                content = "Target deadline: $deadline. Feasibility rated as '${analysis.feasibilityRating}'. Reasoning: ${analysis.feasibilityReasoning}"
            )
        )

        // Generate Structured Roadmap
        generateRoadmapForGoal(finalizedGoal.copy(id = goalId), profile, analysis)

        return goalId
    }

    suspend fun generateRoadmapForGoal(
        goal: GoalEntity,
        profile: UserProfileEntity? = null,
        analysis: AiGoalAnalysis? = null
    ) {
        val user = profile ?: db.userDao().getUserProfileOnce() ?: UserProfileEntity()
        val roadmapResponse = aiRepository.generateRoadmap(goal, user)

        val roadmapPlan: RoadmapPlan = when (roadmapResponse) {
            is AIResponse.Success -> roadmapResponse.data
            is AIResponse.Fallback -> roadmapResponse.data
            is AIResponse.Error -> SmartRuleBasedAiService().generateInternalRoadmapPlan(goal, user)
            AIResponse.Loading -> SmartRuleBasedAiService().generateInternalRoadmapPlan(goal, user)
        }

        // PRESERVE COMPLETED WORK:
        // Read existing tasks and milestones so completed accomplishments and logged time are never lost!
        val existingTasks = db.taskDao().getTasksForGoalOnce(goal.id)
        val completedTasks = existingTasks.filter { it.status == "COMPLETED" }
        val completedMilestones = db.roadmapDao().getMilestonesForGoalOnce(goal.id).filter { it.isCompleted }

        // Remove only incomplete/pending tasks for this goal
        val incompleteTasks = existingTasks.filter { it.status != "COMPLETED" }
        incompleteTasks.forEach { db.taskDao().deleteTask(it) }

        // Clear existing phases to rebuild structure, preserving completed milestones/tasks
        db.roadmapDao().deletePhasesForGoal(goal.id)

        // Insert fresh phases and milestones
        roadmapPlan.phases.forEach { phasePlan ->
            val phaseEntity = RoadmapPhaseEntity(
                goalId = goal.id,
                phaseIndex = phasePlan.phaseIndex,
                name = phasePlan.name,
                purpose = phasePlan.purpose,
                estimatedWeeks = phasePlan.estimatedWeeks,
                requiredSkills = phasePlan.requiredSkills.joinToString(", "),
                completionCriteria = phasePlan.completionCriteria,
                isCompleted = false
            )
            val phaseIds = db.roadmapDao().insertPhases(listOf(phaseEntity))
            val phaseId = phaseIds.firstOrNull() ?: 0L

            phasePlan.milestones.forEachIndexed { mIndex, milestonePlan ->
                val milestoneEntity = MilestoneEntity(
                    phaseId = phaseId,
                    goalId = goal.id,
                    title = milestonePlan.title,
                    description = milestonePlan.description,
                    targetDate = "Week ${milestonePlan.targetWeeksFromStart}",
                    isCompleted = false,
                    orderIndex = mIndex
                )
                val milestoneIds = db.roadmapDao().insertMilestones(listOf(milestoneEntity))
                val milestoneId = milestoneIds.firstOrNull() ?: 0L

                val taskEntities = milestonePlan.tasks.mapIndexed { tIndex, taskPlan ->
                    TaskEntity(
                        goalId = goal.id,
                        phaseId = phaseId,
                        milestoneId = milestoneId,
                        title = taskPlan.title,
                        description = taskPlan.description,
                        instructions = taskPlan.instructions,
                        priority = taskPlan.priority,
                        estimatedMinutes = taskPlan.estimatedMinutes,
                        actualMinutesSpent = 0,
                        dueDate = "Day ${(mIndex * 3) + tIndex + 1}",
                        status = "PENDING",
                        isTodayTask = (phasePlan.phaseIndex == 1 && mIndex == 0 && tIndex <= 1),
                        category = taskPlan.category,
                        orderIndex = tIndex
                    )
                }
                db.taskDao().insertTasks(taskEntities)
            }
        }

        // If user already had completed tasks, attach or preserve them
        if (completedTasks.isNotEmpty()) {
            db.memoryDao().insertMemory(
                MemoryEntity(
                    goalId = goal.id,
                    category = "AI_ADAPTATION",
                    title = "Roadmap Re-architected",
                    content = "Roadmap updated with ${roadmapPlan.phases.size} phases while preserving ${completedTasks.size} completed tasks."
                )
            )
        }

        // Add starter skill if needed
        if (goal.category.isNotBlank()) {
            db.skillDao().insertSkill(
                SkillEntity(
                    name = "${goal.category} Execution",
                    category = goal.category,
                    currentLevel = 2,
                    targetLevel = 4,
                    prerequisites = "Commitment & foundational tools",
                    practiceHours = 0f,
                    projectsCompletedCount = 0
                )
            )
        }

        recalculateGoalProgress(goal.id)
    }

    suspend fun updateGoal(goal: GoalEntity) {
        db.goalDao().updateGoal(goal.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        db.taskDao().deleteTasksForGoal(goal.id)
        db.roadmapDao().deletePhasesForGoal(goal.id)
        db.roadmapDao().deleteMilestonesForGoal(goal.id)
        db.goalDao().deleteGoal(goal)
    }

    // Roadmap queries
    fun getPhasesForGoal(goalId: Long): Flow<List<RoadmapPhaseEntity>> = db.roadmapDao().getPhasesForGoal(goalId)
    fun getMilestonesForGoal(goalId: Long): Flow<List<MilestoneEntity>> = db.roadmapDao().getMilestonesForGoal(goalId)

    suspend fun toggleMilestone(milestone: MilestoneEntity) {
        val updated = milestone.copy(
            isCompleted = !milestone.isCompleted,
            completedAt = if (!milestone.isCompleted) System.currentTimeMillis() else null
        )
        db.roadmapDao().updateMilestone(updated)
        recalculateGoalProgress(milestone.goalId)
    }

    // Tasks
    fun getTasksForGoal(goalId: Long): Flow<List<TaskEntity>> = db.taskDao().getTasksForGoal(goalId)
    val todayTasks: Flow<List<TaskEntity>> = db.taskDao().getAllTodayTasks()

    suspend fun completeTask(task: TaskEntity, minutesSpent: Int) {
        val updated = task.copy(
            status = "COMPLETED",
            actualMinutesSpent = if (minutesSpent > 0) minutesSpent else task.estimatedMinutes,
            completedAt = System.currentTimeMillis()
        )
        db.taskDao().updateTask(updated)
        recalculateGoalProgress(task.goalId)
    }

    suspend fun skipTask(task: TaskEntity) {
        db.taskDao().updateTask(task.copy(status = "SKIPPED"))
    }

    suspend fun rescheduleTask(task: TaskEntity) {
        db.taskDao().updateTask(task.copy(status = "RESCHEDULED", isTodayTask = false))
    }

    suspend fun insertTask(task: TaskEntity): Long {
        val id = db.taskDao().insertTask(task)
        recalculateGoalProgress(task.goalId)
        return id
    }

    suspend fun updateTask(task: TaskEntity) {
        db.taskDao().updateTask(task)
        recalculateGoalProgress(task.goalId)
    }

    suspend fun deleteTask(task: TaskEntity) {
        db.taskDao().deleteTask(task)
        recalculateGoalProgress(task.goalId)
    }

    private suspend fun recalculateGoalProgress(goalId: Long) {
        val tasks = db.taskDao().getTasksForGoalOnce(goalId)
        if (tasks.isNotEmpty()) {
            val completed = tasks.count { it.status == "COMPLETED" }
            val percent = ((completed.toFloat() / tasks.size.toFloat()) * 100).toInt()
            val goal = db.goalDao().getGoalByIdOnce(goalId)
            if (goal != null) {
                db.goalDao().updateGoal(goal.copy(progressPercent = percent, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    // Check-ins
    fun getCheckInsForGoal(goalId: Long): Flow<List<DailyCheckInEntity>> = db.checkInDao().getCheckInsForGoal(goalId)
    val allCheckIns: Flow<List<DailyCheckInEntity>> = db.checkInDao().getAllCheckIns()

    suspend fun submitCheckIn(
        goalId: Long,
        date: String,
        rating: String,
        actualHours: Float,
        accomplished: String,
        wentWrong: String,
        learned: String,
        blockers: String
    ): PlanAdaptationResult {
        val goal = db.goalDao().getGoalByIdOnce(goalId) ?: GoalEntity(title = "Goal", description = "", category = "")
        val pendingTasks = db.taskDao().getTasksForGoalOnce(goalId).filter { it.status != "COMPLETED" }

        val checkIn = DailyCheckInEntity(
            goalId = goalId,
            date = date,
            rating = rating,
            actualHoursInvested = actualHours,
            whatAccomplished = accomplished,
            whatWentWrong = wentWrong,
            whatWasLearned = learned,
            blockers = blockers
        )

        val adaptation = aiRepository.adaptPlan(goal, checkIn, pendingTasks)

        // Save check-in with the adaptation reason
        val finalizedCheckIn = checkIn.copy(
            planAdjustmentReason = adaptation.reasonForChange.ifBlank { adaptation.adjustmentReason }
        )
        db.checkInDao().insertCheckIn(finalizedCheckIn)

        // Record memory of adaptation
        db.memoryDao().insertMemory(
            MemoryEntity(
                goalId = goalId,
                category = "AI_ADAPTATION",
                title = "Check-in: $rating (${actualHours}h)",
                content = "${adaptation.strategySummary} Adjustment: ${adaptation.adjustmentReason}"
            )
        )

        // If a new prerequisite unblocking task was generated, insert it
        adaptation.newPrerequisiteTask?.let { newPlan ->
            db.taskDao().insertTask(
                TaskEntity(
                    goalId = goalId,
                    title = newPlan.title,
                    description = newPlan.description,
                    instructions = newPlan.instructions,
                    priority = newPlan.priority,
                    estimatedMinutes = newPlan.estimatedMinutes,
                    dueDate = "Today",
                    status = "PENDING",
                    isTodayTask = true,
                    category = newPlan.category
                )
            )
            recalculateGoalProgress(goalId)
        }

        return adaptation
    }

    // Memories
    val allMemories: Flow<List<MemoryEntity>> = db.memoryDao().getAllMemories()

    suspend fun saveMemory(memory: MemoryEntity) {
        db.memoryDao().insertMemory(memory)
    }

    suspend fun deleteMemory(memory: MemoryEntity) {
        db.memoryDao().deleteMemory(memory)
    }

    // Notes
    val allNotes: Flow<List<NoteEntity>> = db.noteDao().getAllNotes()

    suspend fun saveNote(note: NoteEntity) {
        if (note.id == 0L) {
            db.noteDao().insertNote(note)
        } else {
            db.noteDao().updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteNote(note: NoteEntity) {
        db.noteDao().deleteNote(note)
    }

    // Skills
    val allSkills: Flow<List<SkillEntity>> = db.skillDao().getAllSkills()

    suspend fun saveSkill(skill: SkillEntity) {
        if (skill.id == 0L) {
            db.skillDao().insertSkill(skill)
        } else {
            db.skillDao().updateSkill(skill)
        }
    }

    suspend fun logSkillPractice(skill: SkillEntity, additionalHours: Float) {
        val updatedHours = skill.practiceHours + additionalHours
        val newLevel = when {
            updatedHours >= 40f -> 5
            updatedHours >= 20f -> 4
            updatedHours >= 10f -> 3
            updatedHours >= 5f -> 2
            else -> 1
        }
        db.skillDao().updateSkill(
            skill.copy(
                practiceHours = updatedHours,
                currentLevel = maxOf(skill.currentLevel, newLevel),
                lastPracticedAt = System.currentTimeMillis()
            )
        )
    }

    // Hackathons
    val allHackathons: Flow<List<HackathonEntity>> = db.hackathonDao().getAllHackathons()

    suspend fun saveHackathon(hackathon: HackathonEntity) {
        if (hackathon.id == 0L) {
            db.hackathonDao().insertHackathon(hackathon)
        } else {
            db.hackathonDao().updateHackathon(hackathon)
        }
    }

    suspend fun deleteHackathon(hackathon: HackathonEntity) {
        db.hackathonDao().deleteHackathon(hackathon)
    }

    // Business Plans
    val allBusinessPlans: Flow<List<BusinessPlanEntity>> = db.businessDao().getAllBusinessPlans()

    suspend fun saveBusinessPlan(plan: BusinessPlanEntity) {
        if (plan.id == 0L) {
            db.businessDao().insertBusinessPlan(plan)
        } else {
            db.businessDao().updateBusinessPlan(plan)
        }
    }

    suspend fun deleteBusinessPlan(plan: BusinessPlanEntity) {
        db.businessDao().deleteBusinessPlan(plan)
    }

    // Chat
    val chatMessages: Flow<List<ChatMessageEntity>> = db.chatDao().getAllMessages()

    suspend fun sendMessage(userQuery: String) {
        val profile = db.userDao().getUserProfileOnce()
        val activeGoal = db.goalDao().getActiveGoalOnce()
        val phases = if (activeGoal != null) db.roadmapDao().getPhasesForGoalOnce(activeGoal.id) else emptyList()
        val allTasks = if (activeGoal != null) db.taskDao().getTasksForGoalOnce(activeGoal.id) else emptyList()
        val pendingTasks = allTasks.filter { it.status != "COMPLETED" }
        val completedTasks = allTasks.filter { it.status == "COMPLETED" }
        val recentCheckIns = db.checkInDao().getRecentCheckInsOnce()
        val memories = db.memoryDao().getAllMemoriesOnce()

        // Insert user message
        db.chatDao().insertMessage(
            ChatMessageEntity(
                role = "USER",
                message = userQuery,
                attachedGoalId = activeGoal?.id
            )
        )

        // Build pruned context
        val context = aiRepository.buildRequestContext(
            profile = profile,
            goal = activeGoal,
            phases = phases,
            pendingTasks = pendingTasks,
            completedTasks = completedTasks,
            checkIns = recentCheckIns,
            memories = memories
        )

        val previousMessages = db.chatDao().getAllMessagesOnce().takeLast(6)
        val turns = mutableListOf<Pair<String, String>>()
        var lastUser: String? = null
        for (msg in previousMessages) {
            if (msg.role == "USER") {
                lastUser = msg.message
            } else if (msg.role == "COACH" && lastUser != null) {
                turns.add(lastUser to msg.message)
                lastUser = null
            }
        }

        val coachResponse = aiRepository.askCoach(userQuery, context, turns)
        val responseText = when (coachResponse) {
            is AIResponse.Success -> coachResponse.data
            is AIResponse.Fallback -> coachResponse.data
            is AIResponse.Error -> "I encountered an issue connecting to the AI service: ${coachResponse.message}. Please check your connection or settings."
            AIResponse.Loading -> ""
        }

        db.chatDao().insertMessage(
            ChatMessageEntity(
                role = "COACH",
                message = responseText,
                attachedGoalId = activeGoal?.id
            )
        )
    }

    suspend fun clearChat() {
        db.chatDao().clearMessages()
    }

    suspend fun testAiConnection(): Boolean {
        val success = aiRepository.testConnection()
        prefs.setAiConnectionStatus(
            status = if (success) "CONNECTED_LIVE" else "OFFLINE_FALLBACK",
            timestamp = System.currentTimeMillis()
        )
        return success
    }
}
