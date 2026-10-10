package com.example.data.ai

// Sealed result representing AI operation states
sealed interface AIResponse<out T> {
    data class Success<out T>(
        val data: T,
        val isLiveAi: Boolean = true,
        val latencyMs: Long = 0
    ) : AIResponse<T>

    data class Fallback<out T>(
        val data: T,
        val reason: String
    ) : AIResponse<T>

    data class Error(
        val message: String,
        val throwable: Throwable? = null,
        val canRetry: Boolean = true
    ) : AIResponse<Nothing>

    object Loading : AIResponse<Nothing>
}

// Pruned context sent to AI models to avoid sending entire database
data class AIRequestContext(
    val userName: String = "",
    val userStatus: String = "",
    val educationField: String = "",
    val currentSkills: String = "",
    val dailyHours: Float = 2.0f,
    val biggestConstraints: String = "",
    val learningStyle: String = "Hands-on projects",
    val activeGoalTitle: String = "",
    val activeGoalCategory: String = "",
    val activeGoalOutcome: String = "",
    val activeGoalDeadline: String = "",
    val activeGoalMotivation: String = "",
    val activeGoalHoursPerWeek: Float = 14.0f,
    val progressPercent: Int = 0,
    val currentPhaseName: String? = null,
    val pendingTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val topPendingTasksSummary: List<String> = emptyList(),
    val recentCheckInRating: String? = null,
    val recentBlocker: String? = null,
    val recentLessons: List<String> = emptyList()
)

// Goal Analysis Models with validation
data class AiGoalAnalysis(
    val intendedOutcome: String,
    val startingPointSummary: String,
    val missingSkillsAndResources: List<String>,
    val estimatedWorkHours: Int,
    val feasibilityRating: String, // "Realistic", "Ambitious but possible", "Requires major assumptions", "Highly unlikely under current constraints", "Insufficient information"
    val feasibilityReasoning: String,
    val assumptionsAndRisks: List<String>,
    val recommendedMilestones: List<String>,
    val highestValueFirstAction: String,
    val firstActionWhy: String,
    val firstActionDurationMinutes: Int = 45
) {
    fun isValid(): Boolean {
        return intendedOutcome.isNotBlank() &&
                feasibilityRating.isNotBlank() &&
                estimatedWorkHours > 0 &&
                highestValueFirstAction.isNotBlank() &&
                missingSkillsAndResources.isNotEmpty()
    }
}

// Roadmap Generation Models with validation
data class RoadmapPlan(
    val phases: List<PhasePlan>,
    val totalEstimatedWeeks: Int
) {
    fun isValid(): Boolean {
        return phases.isNotEmpty() &&
                totalEstimatedWeeks > 0 &&
                phases.all { it.isValid() }
    }
}

data class PhasePlan(
    val phaseIndex: Int,
    val name: String,
    val purpose: String,
    val estimatedWeeks: Int,
    val requiredSkills: List<String>,
    val completionCriteria: String,
    val milestones: List<MilestonePlan>
) {
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                estimatedWeeks > 0 &&
                milestones.isNotEmpty() &&
                milestones.all { it.isValid() }
    }
}

data class MilestonePlan(
    val title: String,
    val description: String,
    val targetWeeksFromStart: Int,
    val tasks: List<TaskPlan>
) {
    fun isValid(): Boolean {
        return title.isNotBlank() &&
                tasks.isNotEmpty() &&
                tasks.all { it.isValid() }
    }
}

data class TaskPlan(
    val title: String,
    val description: String,
    val instructions: String = "",
    val priority: String = "P1", // P0, P1, P2
    val estimatedMinutes: Int = 45,
    val category: String = "Execution",
    val prerequisites: List<String> = emptyList()
) {
    fun isValid(): Boolean {
        return title.isNotBlank() && estimatedMinutes > 0
    }
}

// Next Move Recommendation Model
data class NextMoveRecommendation(
    val actionTitle: String,
    val instructions: String = "",
    val whyItMatters: String,
    val expectedOutcome: String,
    val estimatedMinutes: Int,
    val prerequisites: List<String>,
    val reasoningContext: String,
    val alternativeActions: List<String> = emptyList(),
    val taskId: Long? = null,
    val goalId: Long? = null,
    val priority: String = "P0",
    val isLiveAi: Boolean = false,
    val hasUnmetPrerequisites: Boolean = false
)

// Coach Message Model
data class CoachMessage(
    val id: Long = 0L,
    val role: String, // USER, COACH, SYSTEM
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiveAi: Boolean = false
)

// Adaptive Planning Result
data class PlanAdaptationResult(
    val strategySummary: String,
    val adjustmentReason: String,
    val recommendedAction: String,
    val suggestedTaskSizeAdjustment: String,
    val newPrerequisiteTask: TaskPlan? = null,
    val reasonForChange: String = ""
)

// AI Engine Status & Configuration
enum class AiConnectionMode {
    UNCONFIGURED,
    OFFLINE_FALLBACK,
    CONNECTED_LIVE,
    CUSTOM_BACKEND
}

data class AiEngineStatus(
    val isGeminiLive: Boolean,
    val mode: AiConnectionMode = if (isGeminiLive) AiConnectionMode.CONNECTED_LIVE else AiConnectionMode.OFFLINE_FALLBACK,
    val providerName: String,
    val description: String,
    val customEndpointUrl: String? = null,
    val lastTestedTime: Long? = null
)
