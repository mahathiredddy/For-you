package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val name: String = "",
    val age: Int? = null,
    val location: String = "",
    val status: String = "Student", // Student, Employed, Entrepreneur, Job Seeker, Other
    val educationField: String = "",
    val currentSkills: String = "",
    val hoursPerDay: Float = 2.0f,
    val budget: String = "",
    val biggestConstraints: String = "",
    val learningStyle: String = "Hands-on projects",
    val planIntensity: String = "Balanced",
    val isOnboardingCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val category: String, // Career, Education, Programming, Hackathons, Business, Personal Project, Exam, Financial Independence
    val currentState: String = "",
    val desiredOutcome: String = "",
    val deadline: String = "",
    val motivation: String = "",
    val hoursPerWeek: Float = 14.0f,
    val budget: String = "",
    val constraints: String = "",
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED, ARCHIVED
    val feasibilityRating: String = "Realistic", // Realistic, Ambitious but possible, Requires major assumptions, Highly unlikely under current constraints, Insufficient information
    val feasibilityReasoning: String = "",
    val highestValueFirstAction: String = "",
    val progressPercent: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "roadmap_phases")
data class RoadmapPhaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long,
    val phaseIndex: Int,
    val name: String,
    val purpose: String,
    val estimatedWeeks: Int = 2,
    val requiredSkills: String = "",
    val completionCriteria: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val phaseId: Long,
    val goalId: Long,
    val title: String,
    val description: String,
    val targetDate: String = "",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val orderIndex: Int = 0
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long,
    val phaseId: Long = 0L,
    val milestoneId: Long? = null,
    val title: String,
    val description: String = "",
    val instructions: String = "",
    val priority: String = "P1", // P0, P1, P2
    val estimatedMinutes: Int = 45,
    val actualMinutesSpent: Int = 0,
    val dueDate: String = "",
    val status: String = "PENDING", // PENDING, COMPLETED, SKIPPED, RESCHEDULED
    val isTodayTask: Boolean = false,
    val blockerNote: String? = null,
    val category: String = "Execution",
    val orderIndex: Int = 0,
    val completedAt: Long? = null
)

@Entity(tableName = "daily_check_ins")
data class DailyCheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long,
    val date: String, // YYYY-MM-DD
    val rating: String, // Crushed it, Good, Average, Struggled, Didn't happen
    val actualHoursInvested: Float = 0f,
    val whatAccomplished: String = "",
    val whatWentWrong: String = "",
    val whatWasLearned: String = "",
    val blockers: String = "",
    val planAdjustmentReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long? = null,
    val category: String, // PREFERENCE, LESSON_LEARNED, DECISION, OBSTACLE, MILESTONE_WIN, AI_ADAPTATION
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long? = null,
    val linkedTaskId: Long? = null,
    val title: String,
    val content: String,
    val tags: String = "",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val category: String = "General",
    val currentLevel: Int = 1, // 1 to 5
    val targetLevel: Int = 3, // 1 to 5
    val prerequisites: String = "",
    val practiceHours: Float = 0f,
    val projectsCompletedCount: Int = 0,
    val lastPracticedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hackathons")
data class HackathonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val officialUrl: String = "",
    val problemStatement: String = "",
    val deadline: String = "",
    val teamMembersSkills: String = "",
    val techRestrictions: String = "",
    val judgingCriteria: String = "",
    val mvpScope: String = "",
    val techArchitecture: String = "",
    val ideaScoreJson: String = "",
    val pitchChecklistJson: String = "",
    val status: String = "PLANNING", // PLANNING, IN_PROGRESS, SUBMITTED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_plans")
data class BusinessPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val businessIdea: String,
    val targetCustomer: String = "",
    val customerProblem: String = "",
    val location: String = "",
    val startingCapital: String = "",
    val hoursPerWeek: Float = 10f,
    val revenueObjective: String = "",
    val deadline: String = "",
    val riskTolerance: String = "Moderate",
    val valueProposition: String = "",
    val businessModel: String = "",
    val startupCosts: String = "",
    val recurringCosts: String = "",
    val unitEconomics: String = "",
    val breakEvenAnalysis: String = "",
    val validationExperiments: String = "",
    val thirtyDayPlan: String = "",
    val status: String = "VALIDATING",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val role: String, // USER, COACH, SYSTEM
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedGoalId: Long? = null
)
