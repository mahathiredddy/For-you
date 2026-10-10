package com.example.data.ai

import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity

interface AiService {
    fun getEngineStatus(): AiEngineStatus

    suspend fun testConnection(): Boolean

    suspend fun analyzeGoal(goal: GoalEntity, profile: UserProfileEntity): AIResponse<AiGoalAnalysis>

    suspend fun generateRoadmap(goal: GoalEntity, profile: UserProfileEntity): AIResponse<RoadmapPlan>

    fun determineNextMove(
        goal: GoalEntity?,
        pendingTasks: List<TaskEntity>,
        completedTasks: List<TaskEntity>,
        checkIns: List<DailyCheckInEntity>,
        memories: List<MemoryEntity>
    ): NextMoveRecommendation

    suspend fun askCoach(
        query: String,
        context: AIRequestContext,
        recentChatTurns: List<Pair<String, String>> = emptyList()
    ): AIResponse<String>

    suspend fun adaptPlan(
        goal: GoalEntity,
        checkIn: DailyCheckInEntity,
        pendingTasks: List<TaskEntity>
    ): PlanAdaptationResult
}
