package com.example.data.ai

import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.RoadmapPhaseEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AIRepository(
    val aiService: AiService
) {
    fun getEngineStatus(): AiEngineStatus = aiService.getEngineStatus()

    suspend fun testConnection(): Boolean = aiService.testConnection()

    suspend fun analyzeGoal(goal: GoalEntity, profile: UserProfileEntity): AIResponse<AiGoalAnalysis> {
        return aiService.analyzeGoal(goal, profile)
    }

    suspend fun generateRoadmap(goal: GoalEntity, profile: UserProfileEntity): AIResponse<RoadmapPlan> {
        return aiService.generateRoadmap(goal, profile)
    }

    fun determineNextMove(
        goal: GoalEntity?,
        pendingTasks: List<TaskEntity>,
        completedTasks: List<TaskEntity>,
        checkIns: List<DailyCheckInEntity>,
        memories: List<MemoryEntity>
    ): NextMoveRecommendation {
        return aiService.determineNextMove(goal, pendingTasks, completedTasks, checkIns, memories)
    }

    suspend fun askCoach(
        query: String,
        context: AIRequestContext,
        recentChatTurns: List<Pair<String, String>>
    ): AIResponse<String> {
        return aiService.askCoach(query, context, recentChatTurns)
    }

    suspend fun adaptPlan(
        goal: GoalEntity,
        checkIn: DailyCheckInEntity,
        pendingTasks: List<TaskEntity>
    ): PlanAdaptationResult {
        return aiService.adaptPlan(goal, checkIn, pendingTasks)
    }

    fun buildRequestContext(
        profile: UserProfileEntity?,
        goal: GoalEntity?,
        phases: List<RoadmapPhaseEntity>,
        pendingTasks: List<TaskEntity>,
        completedTasks: List<TaskEntity>,
        checkIns: List<DailyCheckInEntity>,
        memories: List<MemoryEntity>
    ): AIRequestContext {
        val user = profile ?: UserProfileEntity()
        val currentGoal = goal ?: GoalEntity(title = "", description = "", category = "")
        val activePhase = phases.firstOrNull { !it.isCompleted }?.name ?: phases.firstOrNull()?.name
        val recentCheckIn = checkIns.firstOrNull()

        return AIRequestContext(
            userName = user.name,
            userStatus = user.status,
            educationField = user.educationField,
            currentSkills = user.currentSkills,
            dailyHours = user.hoursPerDay,
            biggestConstraints = user.biggestConstraints,
            learningStyle = user.learningStyle,
            activeGoalTitle = currentGoal.title,
            activeGoalCategory = currentGoal.category,
            activeGoalOutcome = currentGoal.desiredOutcome,
            activeGoalDeadline = currentGoal.deadline,
            activeGoalMotivation = currentGoal.motivation,
            activeGoalHoursPerWeek = currentGoal.hoursPerWeek,
            progressPercent = currentGoal.progressPercent,
            currentPhaseName = activePhase,
            pendingTasksCount = pendingTasks.size,
            completedTasksCount = completedTasks.size,
            topPendingTasksSummary = pendingTasks.take(3).map { "${it.title} (${it.priority}, ~${it.estimatedMinutes}m)" },
            recentCheckInRating = recentCheckIn?.rating,
            recentBlocker = recentCheckIn?.blockers,
            recentLessons = memories.filter { it.category == "LESSON_LEARNED" }.take(3).map { it.content }
        )
    }
}
