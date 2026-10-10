package com.example.data.ai

import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GeminiAiService(
    val backendClient: AiBackendClient = AiBackendClient(),
    private val fallbackService: SmartRuleBasedAiService = SmartRuleBasedAiService()
) : AiService {

    private var lastSuccessfulPingTime: Long? = null

    override fun getEngineStatus(): AiEngineStatus {
        val hasCustomBackend = backendClient.customBackendUrl.isNotBlank()
        val hasKey = backendClient.effectiveApiKey.isNotBlank()
        val isVerified = lastSuccessfulPingTime != null

        return when {
            hasCustomBackend -> AiEngineStatus(
                isGeminiLive = isVerified,
                mode = if (isVerified) AiConnectionMode.CUSTOM_BACKEND else AiConnectionMode.UNCONFIGURED,
                providerName = if (isVerified) "Secure Cloud Backend" else "Backend Configured (Untested)",
                description = if (isVerified) {
                    "Live calls routed through verified backend (${backendClient.customBackendUrl})."
                } else {
                    "Backend configured (${backendClient.customBackendUrl}). Run 'Test Connection' in Settings to verify live status."
                },
                customEndpointUrl = backendClient.customBackendUrl,
                lastTestedTime = lastSuccessfulPingTime
            )
            hasKey -> AiEngineStatus(
                isGeminiLive = isVerified,
                mode = if (isVerified) AiConnectionMode.CONNECTED_LIVE else AiConnectionMode.UNCONFIGURED,
                providerName = if (isVerified) "Gemini 3.5 Flash" else "Gemini (Configured, Untested)",
                description = if (isVerified) {
                    "Connected and verified with Google GenAI REST API."
                } else {
                    "Gemini API key configured. Run 'Test Connection' in Settings to verify live status."
                },
                lastTestedTime = lastSuccessfulPingTime
            )
            else -> AiEngineStatus(
                isGeminiLive = false,
                mode = AiConnectionMode.OFFLINE_FALLBACK,
                providerName = "Offline Execution Engine",
                description = "Operating on deterministic on-device intelligence. (Configure API key or backend endpoint in Settings)."
            )
        }
    }

    override suspend fun testConnection(): Boolean {
        val success = backendClient.testConnection()
        if (success) {
            lastSuccessfulPingTime = System.currentTimeMillis()
        }
        return success
    }

    override suspend fun analyzeGoal(goal: GoalEntity, profile: UserProfileEntity): AIResponse<AiGoalAnalysis> {
        if (!backendClient.isConfigured) {
            return fallbackService.analyzeGoal(goal, profile)
        }

        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val prompt = """
                    You are the senior goal execution architect for WE'RE HERE.
                    Tagline: "Tell us where you want to go. We'll help you figure out how to get there."
                    Analyze this user goal and return a JSON object ONLY:
                    Goal Title: ${goal.title}
                    Description: ${goal.description}
                    Category: ${goal.category}
                    Current State: ${goal.currentState}
                    Desired Outcome: ${goal.desiredOutcome}
                    Target Deadline: ${goal.deadline}
                    Motivation: ${goal.motivation}
                    Hours per week: ${goal.hoursPerWeek}
                    User Baseline Skills: ${profile.currentSkills}
                    User Constraints: ${profile.biggestConstraints}
                    Learning Style: ${profile.learningStyle}

                    Return JSON format matching this schema strictly:
                    {
                      "intendedOutcome": "...",
                      "startingPointSummary": "...",
                      "missingSkillsAndResources": ["...", "..."],
                      "estimatedWorkHours": 120,
                      "feasibilityRating": "Realistic", // Choose one strictly: "Realistic", "Ambitious but possible", "Requires major assumptions", "Highly unlikely under current constraints", "Insufficient information"
                      "feasibilityReasoning": "...",
                      "assumptionsAndRisks": ["...", "..."],
                      "recommendedMilestones": ["...", "..."],
                      "highestValueFirstAction": "...",
                      "firstActionWhy": "...",
                      "firstActionDurationMinutes": 45
                    }
                    Important: Do NOT guarantee business success, profits, or unrealistic timelines. Be honest and grounded.
                """.trimIndent()

                val result = backendClient.executeGenerateContent(
                    prompt = prompt,
                    systemInstruction = "You are an honest, elite personal goal architect. Respond with valid JSON only.",
                    forceJson = true
                )

                if (result.isSuccess) {
                    val rawJson = result.getOrThrow()
                    val parsed = parseGoalAnalysis(rawJson)
                    if (parsed != null && parsed.isValid()) {
                        AIResponse.Success(data = parsed, isLiveAi = true, latencyMs = System.currentTimeMillis() - startTime)
                    } else {
                        val fallback = fallbackService.generateInternalGoalAnalysis(goal, profile)
                        AIResponse.Fallback(data = fallback, reason = "Live response schema validation failed. Used verified fallback.")
                    }
                } else {
                    val fallback = fallbackService.generateInternalGoalAnalysis(goal, profile)
                    AIResponse.Fallback(data = fallback, reason = "AI service request failed: ${result.exceptionOrNull()?.message ?: "unknown"}")
                }
            } catch (e: Exception) {
                val fallback = fallbackService.generateInternalGoalAnalysis(goal, profile)
                AIResponse.Fallback(data = fallback, reason = "Exception contacting AI service: ${e.message}")
            }
        }
    }

    override suspend fun generateRoadmap(goal: GoalEntity, profile: UserProfileEntity): AIResponse<RoadmapPlan> {
        if (!backendClient.isConfigured) {
            return fallbackService.generateRoadmap(goal, profile)
        }

        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val prompt = """
                    You are the master roadmap generator for WE'RE HERE.
                    Generate a structured execution roadmap for this goal:
                    Goal: ${goal.title} (${goal.category})
                    Outcome: ${goal.desiredOutcome}
                    Deadline: ${goal.deadline}
                    Hours per week: ${goal.hoursPerWeek}
                    User skills: ${profile.currentSkills}
                    Constraints: ${profile.biggestConstraints}

                    Structure:
                    Goal -> 3 Phases -> 2 Milestones per phase -> 2 Tasks per milestone.
                    Return a JSON object ONLY matching this schema:
                    {
                      "totalEstimatedWeeks": 6,
                      "phases": [
                        {
                          "phaseIndex": 1,
                          "name": "Phase 1: Name",
                          "purpose": "Purpose description",
                          "estimatedWeeks": 2,
                          "requiredSkills": ["Skill 1", "Skill 2"],
                          "completionCriteria": "Specific verifiable deliverable",
                          "milestones": [
                            {
                              "title": "M1.1: Milestone Name",
                              "description": "Milestone description",
                              "targetWeeksFromStart": 1,
                              "tasks": [
                                {
                                  "title": "Task title",
                                  "description": "Task description",
                                  "instructions": "Step-by-step instructions",
                                  "priority": "P0",
                                  "estimatedMinutes": 45,
                                  "category": "Setup",
                                  "prerequisites": []
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                """.trimIndent()

                val result = backendClient.executeGenerateContent(
                    prompt = prompt,
                    systemInstruction = "You are a senior technical project planner. Generate structured execution plans in JSON only.",
                    forceJson = true
                )

                if (result.isSuccess) {
                    val rawJson = result.getOrThrow()
                    val parsed = parseRoadmapPlan(rawJson)
                    if (parsed != null && parsed.isValid()) {
                        AIResponse.Success(data = parsed, isLiveAi = true, latencyMs = System.currentTimeMillis() - startTime)
                    } else {
                        val fallback = fallbackService.generateInternalRoadmapPlan(goal, profile)
                        AIResponse.Fallback(data = fallback, reason = "Live roadmap schema validation failed. Used verified fallback.")
                    }
                } else {
                    val fallback = fallbackService.generateInternalRoadmapPlan(goal, profile)
                    AIResponse.Fallback(data = fallback, reason = "AI roadmap call failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                val fallback = fallbackService.generateInternalRoadmapPlan(goal, profile)
                AIResponse.Fallback(data = fallback, reason = "Exception in roadmap call: ${e.message}")
            }
        }
    }

    override fun determineNextMove(
        goal: GoalEntity?,
        pendingTasks: List<TaskEntity>,
        completedTasks: List<TaskEntity>,
        checkIns: List<DailyCheckInEntity>,
        memories: List<MemoryEntity>
    ): NextMoveRecommendation {
        val baseRec = fallbackService.determineNextMove(goal, pendingTasks, completedTasks, checkIns, memories)
        return baseRec.copy(
            isLiveAi = backendClient.isConfigured
        )
    }

    override suspend fun askCoach(
        query: String,
        context: AIRequestContext,
        recentChatTurns: List<Pair<String, String>>
    ): AIResponse<String> {
        if (!backendClient.isConfigured) {
            return fallbackService.askCoach(query, context, recentChatTurns)
        }

        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val historyString = if (recentChatTurns.isNotEmpty()) {
                    "Recent Conversation History:\n" + recentChatTurns.takeLast(4).joinToString("\n") { (user, coach) ->
                        "User: $user\nCoach: $coach"
                    }
                } else "No previous conversation turns."

                val goalContext = if (context.activeGoalTitle.isNotBlank()) {
                    "Goal: ${context.activeGoalTitle} (${context.activeGoalCategory}, ${context.progressPercent}% complete, Target: ${context.activeGoalDeadline})\nOutcome: ${context.activeGoalOutcome}\nActive Phase: ${context.currentPhaseName ?: "Phase 1"}"
                } else "No active goal set."

                val tasksContext = if (context.topPendingTasksSummary.isNotEmpty()) {
                    "Pending Tasks Queue:\n" + context.topPendingTasksSummary.joinToString("\n") { "• $it" }
                } else "No pending tasks queued."

                val checkInContext = if (!context.recentCheckInRating.isNullOrBlank()) {
                    "Recent Check-In: ${context.recentCheckInRating}" + if (!context.recentBlocker.isNullOrBlank()) " (Blocker: ${context.recentBlocker})" else ""
                } else "No check-ins logged yet."

                val prompt = """
                    You are the personal execution coach in the WE'RE HERE native Android application.
                    Tagline: 'Tell us where you want to go. We'll help you figure out how to get there.'
                    
                    PRUNED USER CONTEXT:
                    User: ${context.userName.ifBlank { "Explorer" }} (${context.userStatus}, ${context.dailyHours}h/day commitment)
                    Constraints: ${context.biggestConstraints.ifBlank { "None reported" }}
                    $goalContext
                    $tasksContext
                    $checkInContext
                    $historyString

                    User query: "$query"

                    Instructions:
                    - Be calm, intelligent, ambitious, pragmatic, and trustworthy.
                    - Offer concrete, specific next steps rather than hollow platitudes.
                    - Reference their active goal and constraints directly.
                    - Keep response concise, structured with clean markdown bullets where helpful. Max 160 words.
                """.trimIndent()

                val result = backendClient.executeGenerateContent(
                    prompt = prompt,
                    systemInstruction = "You are an elite, calm personal goal execution coach. Respond with pragmatic, concise advice."
                )

                if (result.isSuccess && result.getOrThrow().isNotBlank()) {
                    AIResponse.Success(
                        data = result.getOrThrow(),
                        isLiveAi = true,
                        latencyMs = System.currentTimeMillis() - startTime
                    )
                } else {
                    fallbackService.askCoach(query, context, recentChatTurns)
                }
            } catch (e: Exception) {
                fallbackService.askCoach(query, context, recentChatTurns)
            }
        }
    }

    override suspend fun adaptPlan(
        goal: GoalEntity,
        checkIn: DailyCheckInEntity,
        pendingTasks: List<TaskEntity>
    ): PlanAdaptationResult {
        if (!backendClient.isConfigured) {
            return fallbackService.adaptPlan(goal, checkIn, pendingTasks)
        }

        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    You are the adaptive execution coach for WE'RE HERE.
                    The user completed a daily check-in for goal: "${goal.title}" (${goal.category}).
                    
                    CHECK-IN METRICS:
                    - Rating: ${checkIn.rating}
                    - Hours invested: ${checkIn.actualHoursInvested}h
                    - What was accomplished: ${checkIn.whatAccomplished.ifBlank { "Not specified" }}
                    - What went wrong / friction: ${checkIn.whatWentWrong.ifBlank { "None reported" }}
                    - Lessons learned: ${checkIn.whatWasLearned.ifBlank { "None reported" }}
                    - Reported blockers: ${checkIn.blockers.ifBlank { "None reported" }}
                    
                    PENDING TASKS:
                    ${pendingTasks.take(4).joinToString("\n") { "• ${it.title} (${it.priority}, ~${it.estimatedMinutes}m)" }}
                    
                    Respond with a valid JSON object ONLY matching this schema:
                    {
                      "strategySummary": "Short 1-sentence assessment of current velocity",
                      "adjustmentReason": "Clear reason for any adjustments or pacing guidance",
                      "recommendedAction": "Concrete immediate next recommendation",
                      "suggestedTaskSizeAdjustment": "e.g. Keep standard 45m / Downsize to 25m / Break into subtasks",
                      "reasonForChange": "Why this adaptation optimizes goal completion",
                      "newPrerequisiteTask": null
                    }
                """.trimIndent()

                val result = backendClient.executeGenerateContent(
                    prompt = prompt,
                    systemInstruction = "You are a tactical, realistic execution coach. Output strictly valid JSON.",
                    forceJson = true
                )

                if (result.isSuccess) {
                    val rawJson = result.getOrThrow()
                    val parsed = parseAdaptationResult(rawJson)
                    if (parsed != null) {
                        lastSuccessfulPingTime = System.currentTimeMillis()
                        return@withContext parsed
                    }
                }
                fallbackService.adaptPlan(goal, checkIn, pendingTasks)
            } catch (e: Exception) {
                fallbackService.adaptPlan(goal, checkIn, pendingTasks)
            }
        }
    }

    fun parseAdaptationResult(jsonText: String): PlanAdaptationResult? {
        return try {
            val clean = jsonText.substringAfter("{").substringBeforeLast("}")
            val json = JSONObject("{$clean}")
            val taskObj = json.optJSONObject("newPrerequisiteTask")
            val newTask = if (taskObj != null) {
                TaskPlan(
                    title = taskObj.optString("title", "Unblocking Action"),
                    description = taskObj.optString("description", ""),
                    instructions = taskObj.optString("instructions", ""),
                    priority = taskObj.optString("priority", "P0"),
                    estimatedMinutes = taskObj.optInt("estimatedMinutes", 25),
                    category = taskObj.optString("category", "Unblocking")
                )
            } else null

            PlanAdaptationResult(
                strategySummary = json.optString("strategySummary", "Plan adapted to recent execution pace."),
                adjustmentReason = json.optString("adjustmentReason", "Based on reported check-in results."),
                recommendedAction = json.optString("recommendedAction", "Focus on single highest-leverage task."),
                suggestedTaskSizeAdjustment = json.optString("suggestedTaskSizeAdjustment", "Standard 45-minute sprint"),
                newPrerequisiteTask = newTask,
                reasonForChange = json.optString("reasonForChange", "Optimizes consistency and momentum.")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseGoalAnalysis(jsonText: String): AiGoalAnalysis? {
        return try {
            val clean = jsonText.substringAfter("{").substringBeforeLast("}")
            val json = JSONObject("{$clean}")

            val missingSkills = mutableListOf<String>()
            val missingArr = json.optJSONArray("missingSkillsAndResources")
            if (missingArr != null) {
                for (i in 0 until missingArr.length()) {
                    missingSkills.add(missingArr.getString(i))
                }
            }

            val risks = mutableListOf<String>()
            val risksArr = json.optJSONArray("assumptionsAndRisks")
            if (risksArr != null) {
                for (i in 0 until risksArr.length()) {
                    risks.add(risksArr.getString(i))
                }
            }

            val milestones = mutableListOf<String>()
            val milesArr = json.optJSONArray("recommendedMilestones")
            if (milesArr != null) {
                for (i in 0 until milesArr.length()) {
                    milestones.add(milesArr.getString(i))
                }
            }

            AiGoalAnalysis(
                intendedOutcome = json.optString("intendedOutcome", "Complete goal successfully"),
                startingPointSummary = json.optString("startingPointSummary", "Initial baseline"),
                missingSkillsAndResources = if (missingSkills.isNotEmpty()) missingSkills else listOf("Foundational domain practice"),
                estimatedWorkHours = json.optInt("estimatedWorkHours", 120),
                feasibilityRating = json.optString("feasibilityRating", "Realistic"),
                feasibilityReasoning = json.optString("feasibilityReasoning", "Analysis based on provided constraints."),
                assumptionsAndRisks = if (risks.isNotEmpty()) risks else listOf("Consistency required"),
                recommendedMilestones = if (milestones.isNotEmpty()) milestones else listOf("Complete Phase 1 prototype"),
                highestValueFirstAction = json.optString("highestValueFirstAction", "Prepare starter environment"),
                firstActionWhy = json.optString("firstActionWhy", "Establishes baseline momentum"),
                firstActionDurationMinutes = json.optInt("firstActionDurationMinutes", 45)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseRoadmapPlan(jsonText: String): RoadmapPlan? {
        return try {
            val clean = jsonText.substringAfter("{").substringBeforeLast("}")
            val root = JSONObject("{$clean}")

            val totalWeeks = root.optInt("totalEstimatedWeeks", 6)
            val phasesArray = root.optJSONArray("phases") ?: return null

            val phasePlans = mutableListOf<PhasePlan>()
            for (i in 0 until phasesArray.length()) {
                val phaseObj = phasesArray.getJSONObject(i)
                val phaseIndex = phaseObj.optInt("phaseIndex", i + 1)
                val name = phaseObj.optString("name", "Phase $phaseIndex")
                val purpose = phaseObj.optString("purpose", "")
                val estWeeks = phaseObj.optInt("estimatedWeeks", 2)

                val reqSkills = mutableListOf<String>()
                val skillsArr = phaseObj.optJSONArray("requiredSkills")
                if (skillsArr != null) {
                    for (s in 0 until skillsArr.length()) {
                        reqSkills.add(skillsArr.getString(s))
                    }
                }

                val criteria = phaseObj.optString("completionCriteria", "")

                val milestonePlans = mutableListOf<MilestonePlan>()
                val milestonesArr = phaseObj.optJSONArray("milestones")
                if (milestonesArr != null) {
                    for (m in 0 until milestonesArr.length()) {
                        val mObj = milestonesArr.getJSONObject(m)
                        val mTitle = mObj.optString("title", "Milestone ${m + 1}")
                        val mDesc = mObj.optString("description", "")
                        val mWeeks = mObj.optInt("targetWeeksFromStart", (i * 2) + m + 1)

                        val taskPlans = mutableListOf<TaskPlan>()
                        val tasksArr = mObj.optJSONArray("tasks")
                        if (tasksArr != null) {
                            for (t in 0 until tasksArr.length()) {
                                val tObj = tasksArr.getJSONObject(t)
                                val tTitle = tObj.optString("title", "Task ${t + 1}")
                                val tDesc = tObj.optString("description", "")
                                val tInstr = tObj.optString("instructions", "")
                                val tPriority = tObj.optString("priority", "P1")
                                val tMins = tObj.optInt("estimatedMinutes", 45)
                                val tCat = tObj.optString("category", "Execution")

                                val prereqs = mutableListOf<String>()
                                val pArr = tObj.optJSONArray("prerequisites")
                                if (pArr != null) {
                                    for (p in 0 until pArr.length()) {
                                        prereqs.add(pArr.getString(p))
                                    }
                                }

                                taskPlans.add(
                                    TaskPlan(
                                        title = tTitle,
                                        description = tDesc,
                                        instructions = tInstr,
                                        priority = tPriority,
                                        estimatedMinutes = tMins,
                                        category = tCat,
                                        prerequisites = prereqs
                                    )
                                )
                            }
                        }

                        milestonePlans.add(
                            MilestonePlan(
                                title = mTitle,
                                description = mDesc,
                                targetWeeksFromStart = mWeeks,
                                tasks = taskPlans
                            )
                        )
                    }
                }

                phasePlans.add(
                    PhasePlan(
                        phaseIndex = phaseIndex,
                        name = name,
                        purpose = purpose,
                        estimatedWeeks = estWeeks,
                        requiredSkills = reqSkills,
                        completionCriteria = criteria,
                        milestones = milestonePlans
                    )
                )
            }

            RoadmapPlan(phases = phasePlans, totalEstimatedWeeks = totalWeeks)
        } catch (e: Exception) {
            null
        }
    }
}
