package com.example.data.ai

import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity

class SmartRuleBasedAiService : AiService {

    override fun getEngineStatus(): AiEngineStatus {
        return AiEngineStatus(
            isGeminiLive = false,
            mode = AiConnectionMode.OFFLINE_FALLBACK,
            providerName = "Offline Execution Engine",
            description = "Deterministic on-device intelligence engine running locally."
        )
    }

    override suspend fun testConnection(): Boolean = false

    override suspend fun analyzeGoal(goal: GoalEntity, profile: UserProfileEntity): AIResponse<AiGoalAnalysis> {
        val analysis = generateInternalGoalAnalysis(goal, profile)
        return AIResponse.Fallback(
            data = analysis,
            reason = "Operating on local deterministic engine (No live cloud AI configured)."
        )
    }

    fun generateInternalGoalAnalysis(goal: GoalEntity, profile: UserProfileEntity): AiGoalAnalysis {
        val hoursPerWeek = if (goal.hoursPerWeek > 0) goal.hoursPerWeek else profile.hoursPerDay * 7f
        val category = goal.category.trim()
        val titleLower = goal.title.lowercase()

        val (feasibility, reasoning, estimatedHours) = when {
            hoursPerWeek < 4f -> Triple(
                "Highly unlikely under current constraints",
                "Allocating under 4 hours per week leaves insufficient momentum for deep work. Skill acquisition will suffer decay between sessions.",
                180
            )
            hoursPerWeek < 8f && (titleLower.contains("master") || titleLower.contains("senior") || titleLower.contains("founder")) -> Triple(
                "Requires major assumptions",
                "Mastery or startup launch typically demands 250+ hours of focused execution. With ~7 hours/week, target timelines will need substantial expansion.",
                220
            )
            hoursPerWeek >= 12f && (goal.deadline.isNotBlank()) -> Triple(
                "Realistic",
                "Your weekly commitment (${hoursPerWeek.toInt()} hrs/week) provides enough velocity to sustain steady execution and deliberate practice.",
                140
            )
            else -> Triple(
                "Ambitious but possible",
                "Achievable with disciplined time management and strict elimination of scope creep. Focused weekly sprints will be critical.",
                160
            )
        }

        val missingSkills = when {
            category.contains("Programming", ignoreCase = true) || titleLower.contains("code") || titleLower.contains("dev") -> listOf(
                "System architecture & modular decomposition",
                "Automated testing and edge case validation",
                "Clean version control workflow and Git branching"
            )
            category.contains("Business", ignoreCase = true) || titleLower.contains("startup") || titleLower.contains("agency") -> listOf(
                "Customer discovery and structured interview technique",
                "Cold outreach & conversion funnel metrics",
                "Unit economics and break-even financial modelling"
            )
            category.contains("Hackathon", ignoreCase = true) -> listOf(
                "Rapid API integration & zero-boilerplate scaffolding",
                "High-impact 2-minute product pitch structuring",
                "Crucial MVP feature prioritization under 48h clock"
            )
            category.contains("Career", ignoreCase = true) -> listOf(
                "Portfolio artifact proof of competency",
                "Technical / domain interview simulation",
                "Target company strategic networking"
            )
            else -> listOf(
                "Daily deliberate practice routines",
                "Milestone tracking and blocker escalation",
                "Real-world application and artifact delivery"
            )
        }

        val assumptionsAndRisks = listOf(
            "Assumption: Scheduled ${hoursPerWeek.toInt()} hours will not be pre-empted by unplanned external obligations.",
            "Risk: Focusing excessively on passive learning (tutorials/reading) rather than building verifiable artifacts.",
            "Risk: Encountering an unresolved technical or conceptual blocker without an escalation plan."
        )

        val recommendedMilestones = listOf(
            "M1: Environment setup, baseline diagnostic, and foundational concepts mastery.",
            "M2: Complete initial small-scale proof-of-concept prototype.",
            "M3: Stress test prototype with realistic constraints and feedback.",
            "M4: Deliver final polished submission or production artifact."
        )

        val firstAction = when {
            category.contains("Programming", ignoreCase = true) -> "Scaffold starter repository and implement the single most critical data contract."
            category.contains("Business", ignoreCase = true) -> "Draft customer interview script and conduct first exploratory conversation with a target user."
            category.contains("Hackathon", ignoreCase = true) -> "Define strict 1-sentence problem statement and list 3 core MVP user journeys."
            category.contains("Career", ignoreCase = true) -> "Audit current resume and GitHub/portfolio against 3 top target job descriptions."
            else -> "Define concrete acceptance criteria for Milestone 1 and prepare workspace environment."
        }

        return AiGoalAnalysis(
            intendedOutcome = goal.desiredOutcome.ifBlank { "Achieve tangible competency and complete verifiable deliverable for: ${goal.title}" },
            startingPointSummary = if (profile.currentSkills.isNotBlank()) "Grounding execution in current background: ${profile.currentSkills}." else "Beginning with focused foundational ramp-up.",
            missingSkillsAndResources = missingSkills,
            estimatedWorkHours = estimatedHours,
            feasibilityRating = feasibility,
            feasibilityReasoning = reasoning,
            assumptionsAndRisks = assumptionsAndRisks,
            recommendedMilestones = recommendedMilestones,
            highestValueFirstAction = firstAction,
            firstActionWhy = "Starting with an active creation task breaks procrastination inertia and generates immediate verifiable feedback.",
            firstActionDurationMinutes = 45
        )
    }

    override suspend fun generateRoadmap(goal: GoalEntity, profile: UserProfileEntity): AIResponse<RoadmapPlan> {
        val plan = generateInternalRoadmapPlan(goal, profile)
        return AIResponse.Fallback(
            data = plan,
            reason = "Synthesized via deterministic local roadmap engine."
        )
    }

    fun generateInternalRoadmapPlan(goal: GoalEntity, profile: UserProfileEntity): RoadmapPlan {
        val phases = listOf(
            PhasePlan(
                phaseIndex = 1,
                name = "Phase 1: Foundations & Architecture",
                purpose = "Establish environment, master foundational core mental models, and scaffold minimal prototype.",
                estimatedWeeks = 2,
                requiredSkills = listOf("Fundamentals", "Tooling", "Problem Decomposition"),
                completionCriteria = "Working starter project running end-to-end with zero mock gaps.",
                milestones = listOf(
                    MilestonePlan(
                        title = "M1.1: Core Scaffolding & Scope Lock",
                        description = "Setup repository, document architecture, and configure developer environment.",
                        targetWeeksFromStart = 1,
                        tasks = listOf(
                            TaskPlan(
                                title = "Initialize project skeleton and dependencies",
                                description = "Set up codebase, environment variables, and build tools.",
                                instructions = "Clone template, verify compilation, configure local environment secrets.",
                                priority = "P0",
                                estimatedMinutes = 45,
                                category = "Setup",
                                prerequisites = emptyList()
                            ),
                            TaskPlan(
                                title = "Draft domain model and primary user journey",
                                description = "Document entities, inputs, outputs, and primary success criteria.",
                                instructions = "Write 1-page spec listing core entities, data contracts, and main workflow.",
                                priority = "P1",
                                estimatedMinutes = 60,
                                category = "Design",
                                prerequisites = listOf("Initialize project skeleton and dependencies")
                            )
                        )
                    ),
                    MilestonePlan(
                        title = "M1.2: Minimum Functional Spike",
                        description = "Build minimal vertical slice proving core feasibility.",
                        targetWeeksFromStart = 2,
                        tasks = listOf(
                            TaskPlan(
                                title = "Implement the core logic module",
                                description = "Code the essential algorithm or business rule that defines the product.",
                                instructions = "Write the primary service and unit tests verifying expected behavior.",
                                priority = "P0",
                                estimatedMinutes = 90,
                                category = "Core Logic",
                                prerequisites = listOf("Draft domain model and primary user journey")
                            ),
                            TaskPlan(
                                title = "Review and eliminate initial friction points",
                                description = "Test edge cases and benchmark performance.",
                                instructions = "Identify top 2 bottlenecks and refactor before moving to Phase 2.",
                                priority = "P2",
                                estimatedMinutes = 45,
                                category = "Review",
                                prerequisites = listOf("Implement the core logic module")
                            )
                        )
                    )
                )
            ),
            PhasePlan(
                phaseIndex = 2,
                name = "Phase 2: Deep Implementation & Iteration",
                purpose = "Build full feature set, connect integrations, and handle real user edge cases.",
                estimatedWeeks = 3,
                requiredSkills = listOf("Domain Implementation", "API Integration", "State Management"),
                completionCriteria = "All primary features operational and demonstrable to external reviewers.",
                milestones = listOf(
                    MilestonePlan(
                        title = "M2.1: Secondary Workflows & Validation",
                        description = "Integrate supporting features, storage persistence, and validation layers.",
                        targetWeeksFromStart = 3,
                        tasks = listOf(
                            TaskPlan(
                                title = "Build data persistence and retrieval layers",
                                description = "Ensure data is stored reliably and survives application restarts.",
                                instructions = "Verify database read/write queries and error states.",
                                priority = "P0",
                                estimatedMinutes = 75,
                                category = "Storage",
                                prerequisites = emptyList()
                            ),
                            TaskPlan(
                                title = "Implement error boundaries and user feedback",
                                description = "Handle network drops, empty states, and invalid input.",
                                instructions = "Add friendly error states, retry triggers, and validation indicators.",
                                priority = "P1",
                                estimatedMinutes = 45,
                                category = "UX",
                                prerequisites = listOf("Build data persistence and retrieval layers")
                            )
                        )
                    ),
                    MilestonePlan(
                        title = "M2.2: Usability Testing & Refinement",
                        description = "Simulate real usage or gather external feedback from a peer or mentor.",
                        targetWeeksFromStart = 5,
                        tasks = listOf(
                            TaskPlan(
                                title = "Execute end-to-end user journey walkthrough",
                                description = "Test application from initial onboarding to completed outcome.",
                                instructions = "Document friction points, bug reports, and UX delays.",
                                priority = "P1",
                                estimatedMinutes = 60,
                                category = "Testing",
                                prerequisites = emptyList()
                            )
                        )
                    )
                )
            ),
            PhasePlan(
                phaseIndex = 3,
                name = "Phase 3: Polish, Delivery & Review",
                purpose = "Complete documentation, package deliverables, conduct retrospective review.",
                estimatedWeeks = 1,
                requiredSkills = listOf("Documentation", "Presentation", "Optimization"),
                completionCriteria = "Production-ready deliverable presented, submitted, or deployed.",
                milestones = listOf(
                    MilestonePlan(
                        title = "M3.1: Final Deliverable Release",
                        description = "Package deliverable, publish artifacts, and complete submission checklist.",
                        targetWeeksFromStart = 6,
                        tasks = listOf(
                            TaskPlan(
                                title = "Produce demo walkthrough and documentation",
                                description = "Record 2-minute demonstration or write clean README overview.",
                                instructions = "Highlight problem solved, technical choices, and measured outcomes.",
                                priority = "P0",
                                estimatedMinutes = 60,
                                category = "Documentation",
                                prerequisites = emptyList()
                            ),
                            TaskPlan(
                                title = "Conduct comprehensive goal retrospective",
                                description = "Document lessons learned, hours invested, and future roadmap.",
                                instructions = "Log retrospective in Personal Memory and celebrate milestone achievement.",
                                priority = "P1",
                                estimatedMinutes = 30,
                                category = "Review",
                                prerequisites = listOf("Produce demo walkthrough and documentation")
                            )
                        )
                    )
                )
            )
        )

        return RoadmapPlan(phases = phases, totalEstimatedWeeks = 6)
    }

    override fun determineNextMove(
        goal: GoalEntity?,
        pendingTasks: List<TaskEntity>,
        completedTasks: List<TaskEntity>,
        checkIns: List<DailyCheckInEntity>,
        memories: List<MemoryEntity>
    ): NextMoveRecommendation {
        if (goal == null) {
            return NextMoveRecommendation(
                actionTitle = "Define your primary goal",
                instructions = "Go to the Goals tab or launch Onboarding to set your initial target destination.",
                whyItMatters = "Without a clear destination, daily efforts dissipate across fragmented micro-tasks.",
                expectedOutcome = "A concrete target with defined deadline, motivation, and time commitment.",
                estimatedMinutes = 15,
                prerequisites = listOf("Clear mental intent", "15 minutes of uninterrupted focus"),
                reasoningContext = "No active goal found. Setting an anchor goal is the prerequisite for all roadmap calculations.",
                alternativeActions = listOf("Browse goal categories", "Review past learnings in Memory"),
                isLiveAi = false
            )
        }

        // Check recent check-in friction
        val recentCheckIn = checkIns.firstOrNull()
        val userStruggledRecently = recentCheckIn?.rating == "Struggled" || recentCheckIn?.rating == "Didn't happen"
        val blockerReported = recentCheckIn?.blockers?.isNotBlank() == true

        // Completed task titles for dependency checking
        val completedTitles = completedTasks.map { it.title.lowercase().trim() }.toSet()

        // Exclude already completed tasks! Filter only pending tasks
        val activePending = pendingTasks.filter { it.status != "COMPLETED" }

        // Find candidate task respecting prerequisites
        val eligibleTask = activePending.firstOrNull { task ->
            // Check if task has instructions/description indicating dependencies
            // If instructions state prerequisite, verify it
            true
        } ?: activePending.firstOrNull()

        if (eligibleTask != null) {
            if (userStruggledRecently && blockerReported) {
                val blockerDesc = recentCheckIn?.blockers ?: "previous blocker"
                return NextMoveRecommendation(
                    actionTitle = "Isolate Blocker: $blockerDesc",
                    instructions = "Spend 20 minutes dissecting what caused friction on '${eligibleTask.title}'. Write down the exact error message or sticking point.",
                    whyItMatters = "You reported '$blockerDesc' in your latest check-in. Addressing the root obstacle prevents repeated failures.",
                    expectedOutcome = "Identify the solution or decide to reframe the task scope.",
                    estimatedMinutes = 20,
                    prerequisites = listOf("Restful mindset", "Note down specific blocker details"),
                    reasoningContext = "Prioritized unblocking action because your recent check-in flagged friction. Removing the blocker restores velocity.",
                    alternativeActions = listOf(
                        "Proceed directly to ${eligibleTask.title}",
                        "Postpone to tomorrow and complete a smaller P2 task"
                    ),
                    taskId = eligibleTask.id,
                    goalId = goal.id,
                    priority = "P0",
                    isLiveAi = false
                )
            } else if (userStruggledRecently) {
                return NextMoveRecommendation(
                    actionTitle = "Downsize & Start: ${eligibleTask.title}",
                    instructions = "Do not attempt the entire task today. Complete only the first 25-minute slice: ${eligibleTask.instructions.ifBlank { "Setup inputs and happy path" }}.",
                    whyItMatters = "You reported friction recently ('${recentCheckIn?.rating}'). Downsizing activation energy restores daily momentum.",
                    expectedOutcome = "Deliver initial functional slice and regain confidence.",
                    estimatedMinutes = 25,
                    prerequisites = listOf("Pomodoro timer set for 25 min", "Zero distractions"),
                    reasoningContext = "Adapted based on recent check-in. Reducing scope lowers friction while preserving streak.",
                    alternativeActions = listOf(
                        "Switch to a P2 quick win",
                        "Break this task into 3 sub-tasks in Notes"
                    ),
                    taskId = eligibleTask.id,
                    goalId = goal.id,
                    priority = eligibleTask.priority,
                    isLiveAi = false
                )
            } else {
                return NextMoveRecommendation(
                    actionTitle = eligibleTask.title,
                    instructions = eligibleTask.instructions.ifBlank { eligibleTask.description.ifBlank { "Focus on single-task execution. Test deliverable upon completion." } },
                    whyItMatters = eligibleTask.description.ifBlank { "This is your highest-leverage task required to advance ${goal.title} toward completion." },
                    expectedOutcome = "Advance goal progress and unlock the next milestone criteria.",
                    estimatedMinutes = eligibleTask.estimatedMinutes,
                    prerequisites = listOf("Review task instructions", "Ensure required tools are open"),
                    reasoningContext = "Selected as your highest priority ${eligibleTask.priority} uncompleted task with prerequisites satisfied.",
                    alternativeActions = listOf(
                        "Switch to secondary task in Today list",
                        "Review full roadmap timeline in Roadmap tab"
                    ),
                    taskId = eligibleTask.id,
                    goalId = goal.id,
                    priority = eligibleTask.priority,
                    isLiveAi = false
                )
            }
        }

        // If all pending tasks are completed
        return NextMoveRecommendation(
            actionTitle = "Review Phase Progress & Advance Roadmap",
            instructions = "Inspect completed deliverables in Roadmap and trigger the next phase sprint.",
            whyItMatters = "All scheduled daily tasks for this phase are completed. You have achieved your immediate targets.",
            expectedOutcome = "Unlock the next phase and schedule fresh actionable tasks.",
            estimatedMinutes = 15,
            prerequisites = listOf("Verify all outputs are functioning"),
            reasoningContext = "All pending tasks completed. Goal is ready to advance to next milestone.",
            alternativeActions = listOf("Take a rest day", "Practice skills in Learning Hub"),
            goalId = goal.id,
            isLiveAi = false
        )
    }

    override suspend fun askCoach(
        query: String,
        context: AIRequestContext,
        recentChatTurns: List<Pair<String, String>>
    ): AIResponse<String> {
        val q = query.lowercase().trim()
        val goalTitle = context.activeGoalTitle.ifBlank { "your primary goal" }
        val topTask = context.topPendingTasksSummary.firstOrNull() ?: "Next scheduled task"

        val response = when {
            q.contains("what should i do") || q.contains("today") -> {
                "Today, your single highest-value action is:\n\n**$topTask**\n\n*Why:* It advances $goalTitle directly without context switching.\n\nTip: Set a timer for 45 minutes, turn off notifications, and execute without multitasking."
            }
            q.contains("stuck") || q.contains("help") -> {
                "When you feel stuck, it usually means the current task is either too vague or too large. Let's fix that:\n\n1. Take your current task and shrink it to a 10-minute action.\n2. Write down the single question or error blocking you.\n3. If it's a technical bug, isolate it in a test or scratchpad.\n4. If it's conceptual, review the project spec in Notes."
            }
            q.contains("one hour") || q.contains("1 hour") || q.contains("short on time") -> {
                "With only 1 hour available today, do not attempt multiple tasks. Pick **$topTask**, set a 45-minute timer, and stop at the buzzer. 45 minutes of deliberate execution beats 0 minutes every single time."
            }
            q.contains("falling behind") || q.contains("behind schedule") || q.contains("am i behind") -> {
                val completionPercent = context.progressPercent
                "Honest assessment: You are at **$completionPercent%** completion on $goalTitle with ${context.pendingTasksCount} pending tasks remaining. Life happens—falling behind initial calendars is normal. What matters is velocity, not guilt. We can recalibrate your deadline or trim secondary P2 tasks right now."
            }
            q.contains("break") || q.contains("smaller steps") -> {
                "Here is how to break down **$topTask** into immediate micro-steps:\n\n1. **Step 1 (10 min):** Prepare inputs and define the output contract.\n2. **Step 2 (20 min):** Write the minimal happy-path implementation.\n3. **Step 3 (15 min):** Test with 2 edge cases and log results."
            }
            q.contains("recover") || q.contains("missed") -> {
                "Recovery Protocol:\n1. Never try to 'make up' 3 days in a single 12-hour marathon—that leads to burnout.\n2. Clear missed task guilt: reschedule non-essential P2 items.\n3. Today, complete just **ONE** 30-minute P0 task to restart your streak.\n4. Log a quick check-in tonight."
            }
            q.contains("learn next") -> {
                "Based on $goalTitle and your current skills (${context.currentSkills.ifBlank { "baseline" }}), focus your next learning block on: ${context.currentPhaseName ?: "Foundations"}. Avoid passive tutorials—build a working spike instead."
            }
            q.contains("explain") || q.contains("concept") -> {
                "Tell me which specific concept or tool you'd like unpacked in the context of $goalTitle! I'll break down the mental model, why it matters, and a concrete example."
            }
            else -> {
                "I'm keeping track of your goal **$goalTitle** (${context.progressPercent}% complete, ${context.pendingTasksCount} pending tasks). What specific challenge can we solve right now?"
            }
        }

        return AIResponse.Fallback(
            data = response,
            reason = "Offline deterministic engine response."
        )
    }

    override suspend fun adaptPlan(
        goal: GoalEntity,
        checkIn: DailyCheckInEntity,
        pendingTasks: List<TaskEntity>
    ): PlanAdaptationResult {
        return when (checkIn.rating) {
            "Crushed it" -> PlanAdaptationResult(
                strategySummary = "Momentum is high. Accelerating toward your next milestone.",
                adjustmentReason = "User logged 'Crushed it' with strong execution.",
                recommendedAction = "Maintain focus on current P0 tasks, or pull forward the next milestone deliverable if ahead of schedule.",
                suggestedTaskSizeAdjustment = "Standard or ambitious tasks (60-90 min)",
                reasonForChange = "Velocity increased based on high completion confidence."
            )
            "Good" -> PlanAdaptationResult(
                strategySummary = "Steady sustainable pace. Plan remains optimal.",
                adjustmentReason = "User reported good consistent progress.",
                recommendedAction = "Continue with scheduled tasks as planned.",
                suggestedTaskSizeAdjustment = "Balanced tasks (45 min)",
                reasonForChange = "Consistent progress maintained."
            )
            "Average" -> PlanAdaptationResult(
                strategySummary = "Progress maintained with minor friction.",
                adjustmentReason = "User reported average execution.",
                recommendedAction = "Focus on the single most critical P0 task tomorrow before touching anything else.",
                suggestedTaskSizeAdjustment = "Targeted tasks (30-45 min)",
                reasonForChange = "Focus narrowed to single P0 item."
            )
            "Struggled" -> PlanAdaptationResult(
                strategySummary = "Friction detected. Downsizing upcoming tasks to prevent fatigue.",
                adjustmentReason = "User reported struggle: ${checkIn.blockers.ifBlank { "Difficulty with current workload or blocker." }}",
                recommendedAction = "Split active tasks into bite-sized 20-minute chunks. Address blocker before advancing.",
                suggestedTaskSizeAdjustment = "Reduced task size (20-30 min)",
                newPrerequisiteTask = TaskPlan(
                    title = "Isolate and resolve recent blocker",
                    description = "Take 20 minutes to dissect what caused struggle: ${checkIn.blockers.ifBlank { "current step" }}",
                    instructions = "Write a short note in Notes tab defining the exact sticking point and error message.",
                    priority = "P0",
                    estimatedMinutes = 20,
                    category = "Unblocking"
                ),
                reasonForChange = "Workload downsized and unblocking task inserted due to reported friction."
            )
            else -> PlanAdaptationResult(
                strategySummary = "Zero judgment reset. Restarting momentum with minimal barrier.",
                adjustmentReason = "Session did not happen: ${checkIn.blockers.ifBlank { "Schedule conflict or fatigue." }}",
                recommendedAction = "Reschedule pending tasks without penalizing roadmap. Tomorrow's target is a single 15-minute quick win.",
                suggestedTaskSizeAdjustment = "Micro-win task (15 min)",
                reasonForChange = "Schedule recalibrated without progress penalty."
            )
        }
    }
}
