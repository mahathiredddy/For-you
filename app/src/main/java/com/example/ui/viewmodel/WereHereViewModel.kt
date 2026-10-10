package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiBackendClient
import com.example.data.ai.AiEngineStatus
import com.example.data.ai.GeminiAiService
import com.example.data.ai.NextMoveRecommendation
import com.example.data.ai.PlanAdaptationResult
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
import com.example.data.repository.WereHereRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WereHereViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val prefs = UserPreferencesRepository(application)

    private var customUrlInMemory = ""
    private var devKeyInMemory = ""

    private val backendClient = AiBackendClient(
        customBackendUrlProvider = { customUrlInMemory },
        devApiKeyProvider = { devKeyInMemory }
    )
    val aiService = GeminiAiService(backendClient = backendClient)
    val repository = WereHereRepository(db, prefs, aiService)

    val customBackendUrl: StateFlow<String> = prefs.customBackendUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val devApiKey: StateFlow<String> = prefs.devApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val aiConnectionStatus: StateFlow<String> = prefs.aiConnectionStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "OFFLINE_FALLBACK")

    val syncManager = com.example.data.sync.FirestoreSyncManager(application, db)
    val syncStatus: StateFlow<com.example.data.sync.SyncStatus> = syncManager.syncStatus

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    fun triggerCloudSync(userId: String? = null, onComplete: (com.example.data.sync.SyncStatus) -> Unit = {}) {
        viewModelScope.launch {
            val result = syncManager.performSync(userId)
            onComplete(result)
        }
    }

    fun migrateGuestData(userId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = syncManager.migrateGuestDataToUser(userId)
            onComplete(success)
        }
    }

    init {
        viewModelScope.launch {
            prefs.customBackendUrl.collect { customUrlInMemory = it }
        }
        viewModelScope.launch {
            prefs.devApiKey.collect { devKeyInMemory = it }
        }
    }

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeGoal: StateFlow<GoalEntity?> = repository.activeGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayTasks: StateFlow<List<TaskEntity>> = repository.todayTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val phases: StateFlow<List<RoadmapPhaseEntity>> = activeGoal.flatMapLatest { goal ->
        if (goal != null) repository.getPhasesForGoal(goal.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestones: StateFlow<List<MilestoneEntity>> = activeGoal.flatMapLatest { goal ->
        if (goal != null) repository.getMilestonesForGoal(goal.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goalTasks: StateFlow<List<TaskEntity>> = activeGoal.flatMapLatest { goal ->
        if (goal != null) repository.getTasksForGoal(goal.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checkIns: StateFlow<List<DailyCheckInEntity>> = repository.allCheckIns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val skills: StateFlow<List<SkillEntity>> = repository.allSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hackathons: StateFlow<List<HackathonEntity>> = repository.allHackathons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessPlans: StateFlow<List<BusinessPlanEntity>> = repository.allBusinessPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamically evaluated Next Move based on active goal, pending tasks, recent check-ins, and memories
    val nextMove: StateFlow<NextMoveRecommendation> = combine(
        activeGoal,
        todayTasks,
        goalTasks,
        checkIns,
        memories
    ) { goal, todayList, allTaskList, checkInList, memoryList ->
        val pendingTasks = if (todayList.any { it.status == "PENDING" }) {
            todayList.filter { it.status == "PENDING" }
        } else {
            allTaskList.filter { it.status == "PENDING" }
        }
        val completed = allTaskList.filter { it.status == "COMPLETED" }
        repository.aiService.determineNextMove(goal, pendingTasks, completed, checkInList, memoryList)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NextMoveRecommendation(
            actionTitle = "Initialize your first goal",
            whyItMatters = "Define your target destination to activate your personalized roadmap.",
            expectedOutcome = "Clear roadmap, daily actions, and structured milestones.",
            estimatedMinutes = 15,
            prerequisites = emptyList(),
            reasoningContext = "Awaiting initial setup."
        )
    )

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _adaptationResult = MutableStateFlow<PlanAdaptationResult?>(null)
    val adaptationResult: StateFlow<PlanAdaptationResult?> = _adaptationResult.asStateFlow()

    fun getEngineStatus(): AiEngineStatus = repository.aiService.getEngineStatus()

    fun testAiConnection(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isTestingConnection.value = true
            val success = repository.testAiConnection()
            _isTestingConnection.value = false
            onResult(success)
        }
    }

    fun setCustomBackendUrl(url: String) {
        viewModelScope.launch {
            prefs.setCustomBackendUrl(url)
            customUrlInMemory = url
        }
    }

    fun setDevApiKey(key: String) {
        viewModelScope.launch {
            prefs.setDevApiKey(key)
            devKeyInMemory = key
        }
    }

    fun selectAlternativeNextMove(alternativeText: String) {
        viewModelScope.launch {
            val goal = activeGoal.value ?: return@launch
            if (alternativeText.contains("downsize", ignoreCase = true) || alternativeText.contains("25", ignoreCase = true)) {
                // Find top task and adjust its estimated time to 25m
                val topTask = todayTasks.value.firstOrNull { it.status == "PENDING" }
                if (topTask != null) {
                    repository.updateTask(topTask.copy(estimatedMinutes = 25, priority = "P0"))
                }
            } else if (alternativeText.contains("sub-tasks", ignoreCase = true) || alternativeText.contains("Notes", ignoreCase = true)) {
                // Add sub-task note in notes
                val topTask = todayTasks.value.firstOrNull { it.status == "PENDING" }
                repository.saveNote(
                    NoteEntity(
                        goalId = goal.id,
                        title = "Sub-tasks: ${topTask?.title ?: "Action Item"}",
                        content = "1. Setup inputs (10m)\n2. Happy path prototype (20m)\n3. Verify edge cases (15m)",
                        tags = "subtasks, planning"
                    )
                )
            }
        }
    }

    fun completeOnboarding(
        profile: UserProfileEntity,
        initialGoalTitle: String,
        initialGoalDesc: String,
        category: String,
        desiredOutcome: String,
        deadline: String,
        motivation: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isGenerating.value = true
            val updatedProfile = profile.copy(isOnboardingCompleted = true)
            repository.saveUserProfile(updatedProfile)

            if (initialGoalTitle.isNotBlank()) {
                repository.createGoalWithAnalysis(
                    title = initialGoalTitle,
                    description = initialGoalDesc,
                    category = category,
                    currentState = profile.currentSkills,
                    desiredOutcome = desiredOutcome,
                    deadline = deadline,
                    motivation = motivation,
                    hoursPerWeek = profile.hoursPerDay * 7f,
                    budget = profile.budget,
                    constraints = profile.biggestConstraints
                )
            }
            _isGenerating.value = false
            onComplete()
        }
    }

    fun createGoal(
        title: String,
        description: String,
        category: String,
        desiredOutcome: String,
        deadline: String,
        motivation: String,
        hoursPerWeek: Float,
        budget: String,
        constraints: String,
        onFinished: () -> Unit
    ) {
        viewModelScope.launch {
            _isGenerating.value = true
            repository.createGoalWithAnalysis(
                title = title,
                description = description,
                category = category,
                currentState = "",
                desiredOutcome = desiredOutcome,
                deadline = deadline,
                motivation = motivation,
                hoursPerWeek = hoursPerWeek,
                budget = budget,
                constraints = constraints
            )
            _isGenerating.value = false
            onFinished()
        }
    }

    fun regenerateRoadmapForActiveGoal() {
        val goal = activeGoal.value ?: return
        viewModelScope.launch {
            _isGenerating.value = true
            repository.generateRoadmapForGoal(goal)
            _isGenerating.value = false
        }
    }

    fun toggleMilestone(milestone: MilestoneEntity) {
        viewModelScope.launch {
            repository.toggleMilestone(milestone)
        }
    }

    fun completeTask(task: TaskEntity, minutesSpent: Int = 0) {
        viewModelScope.launch {
            repository.completeTask(task, minutesSpent)
        }
    }

    fun skipTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.skipTask(task)
        }
    }

    fun rescheduleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.rescheduleTask(task)
        }
    }

    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun submitDailyCheckIn(
        rating: String,
        hours: Float,
        accomplished: String,
        wentWrong: String,
        learned: String,
        blockers: String,
        onDone: (PlanAdaptationResult) -> Unit
    ) {
        val goalId = activeGoal.value?.id ?: 1L
        viewModelScope.launch {
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val result = repository.submitCheckIn(
                goalId = goalId,
                date = dateStr,
                rating = rating,
                actualHours = hours,
                accomplished = accomplished,
                wentWrong = wentWrong,
                learned = learned,
                blockers = blockers
            )
            _adaptationResult.value = result
            onDone(result)
        }
    }

    fun clearAdaptationResult() {
        _adaptationResult.value = null
    }

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.saveNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun saveSkill(skill: SkillEntity) {
        viewModelScope.launch {
            repository.saveSkill(skill)
        }
    }

    fun logSkillPractice(skill: SkillEntity, hours: Float) {
        viewModelScope.launch {
            repository.logSkillPractice(skill, hours)
        }
    }

    fun saveHackathon(hackathon: HackathonEntity) {
        viewModelScope.launch {
            repository.saveHackathon(hackathon)
        }
    }

    fun deleteHackathon(hackathon: HackathonEntity) {
        viewModelScope.launch {
            repository.deleteHackathon(hackathon)
        }
    }

    fun saveBusinessPlan(plan: BusinessPlanEntity) {
        viewModelScope.launch {
            repository.saveBusinessPlan(plan)
        }
    }

    fun deleteBusinessPlan(plan: BusinessPlanEntity) {
        viewModelScope.launch {
            repository.deleteBusinessPlan(plan)
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(text)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun selectActiveGoal(goal: GoalEntity) {
        viewModelScope.launch {
            val currentGoals = allGoals.value
            currentGoals.forEach { g ->
                if (g.id == goal.id) {
                    repository.updateGoal(g.copy(status = "ACTIVE"))
                } else if (g.status == "ACTIVE") {
                    repository.updateGoal(g.copy(status = "PAUSED"))
                }
            }
            prefs.setSelectedGoalId(goal.id)
        }
    }

    fun updateGoalStatus(goal: GoalEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(status = newStatus))
        }
    }

    fun saveWeeklyReview(wins: String, missed: String, lessons: String, priorities: String) {
        viewModelScope.launch {
            repository.saveMemory(
                MemoryEntity(
                    goalId = activeGoal.value?.id,
                    category = "LESSON_LEARNED",
                    title = "Weekly Retrospective",
                    content = "Wins: $wins\nMissed: $missed\nLessons: $lessons\nPriorities: $priorities"
                )
            )
        }
    }

    fun saveUserProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    /**
     * Emergency Reset: "When everything feels overwhelming, make the next step smaller."
     * Reschedules pending tasks for today and inserts 3 small 25-minute emergency focus tasks.
     */
    fun emergencyResetPlan(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val goal = activeGoal.value ?: return@launch
            val todayPending = todayTasks.value.filter { it.status == "PENDING" }
            todayPending.forEach { task ->
                repository.rescheduleTask(task)
            }
            repository.insertTask(
                TaskEntity(
                    goalId = goal.id,
                    title = "Emergency Focus 1: Unpack the #1 bottleneck",
                    description = "Take 25 minutes with a blank note to write down exactly what is blocking progress and eliminate non-essential work.",
                    instructions = "1. Open Notes. 2. Write down the #1 bottleneck. 3. Decide on one single deliverable today.",
                    priority = "P0",
                    estimatedMinutes = 25,
                    dueDate = "Today",
                    status = "PENDING",
                    isTodayTask = true,
                    category = "Execution"
                )
            )
            repository.insertTask(
                TaskEntity(
                    goalId = goal.id,
                    title = "Emergency Focus 2: Build one micro-deliverable",
                    description = "Commit a single functional piece of work (1 small component, 1 paragraph, or 1 code module).",
                    instructions = "Focus strictly on completing this micro-unit without perfectionism.",
                    priority = "P0",
                    estimatedMinutes = 25,
                    dueDate = "Today",
                    status = "PENDING",
                    isTodayTask = true,
                    category = "Execution"
                )
            )
            repository.insertTask(
                TaskEntity(
                    goalId = goal.id,
                    title = "Emergency Focus 3: Quick review & restore momentum",
                    description = "Spend 15 minutes reviewing today's micro-win to restore momentum.",
                    instructions = "Submit a short check-in noting the recovery.",
                    priority = "P1",
                    estimatedMinutes = 15,
                    dueDate = "Today",
                    status = "PENDING",
                    isTodayTask = true,
                    category = "Review"
                )
            )
            repository.saveMemory(
                MemoryEntity(
                    goalId = goal.id,
                    category = "DECISION",
                    title = "Emergency Plan Reset Triggered",
                    content = "When everything feels overwhelming, make the next step smaller. Reduced today's scope to 3 micro-actions totaling 65 minutes."
                )
            )
            onComplete()
        }
    }

    /**
     * Canonical Demo Mode: Loads Alex, 19, Student, Hyderabad - "Win a National Hackathon in 90 Days"
     */
    fun loadDemoAlexProfile(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isGenerating.value = true
            val alexProfile = UserProfileEntity(
                name = "Alex",
                age = 19,
                location = "Hyderabad, India",
                status = "Student",
                educationField = "Computer Science & Engineering",
                currentSkills = "Python, Basics of Web Dev, Git, Prompt Engineering",
                hoursPerDay = 3.5f,
                budget = "₹0 (Free / Open Source tools)",
                biggestConstraints = "No prior hackathon experience, managing semester coursework",
                learningStyle = "Building real projects & hackathons",
                planIntensity = "Aggressive",
                isOnboardingCompleted = true
            )
            repository.saveUserProfile(alexProfile)

            val goalId = repository.createGoalWithAnalysis(
                title = "Win a National Hackathon in 90 Days",
                description = "Form a high-performing 4-person team, build a verifiable multi-modal AI solution for an official ministry problem statement, and win top prize.",
                category = "Hackathons",
                currentState = "Second year student with core programming knowledge, eager to build high-impact real-world systems.",
                desiredOutcome = "Win 1st place / track prize, gain incubation grant and hiring visibility.",
                deadline = "90 days",
                motivation = "Prove high-velocity engineering capability, build genuine startup-grade technology, and gain financial freedom.",
                hoursPerWeek = 24.5f,
                budget = "₹0",
                constraints = "College exams, team coordination, no prior hackathon wins"
            )

            repository.saveHackathon(
                HackathonEntity(
                    name = "Smart India Hackathon 2026 / Devfolio AI Cup",
                    officialUrl = "https://sih.gov.in",
                    problemStatement = "Automated Multimodal Verification for Rural Telemedicine Diagnoses",
                    deadline = "8 weeks",
                    teamMembersSkills = "Alex (Lead/ML), Priya (Frontend), Rahul (Backend), Sneha (Domain Research)",
                    techRestrictions = "Must run on Android / Web with offline capability",
                    judgingCriteria = "Innovation (25%), Technical Depth (25%), Demo Feasibility (25%), Social Impact (25%)",
                    mvpScope = "TeleVerify: Edge-assisted offline verification of clinical diagnostic checklists using Gemini fallback.",
                    techArchitecture = "Kotlin / Compose frontend, FastAPI backend, Gemini Multimodal API",
                    status = "IN_PROGRESS"
                )
            )

            repository.saveNote(
                NoteEntity(
                    goalId = goalId,
                    title = "Hackathon Golden Rules & Demo Criteria",
                    content = "1. Judges test the live demo in under 3 minutes; zero slides-only pitches.\n2. Prioritize ₹0 validation with 5 user quotes.\n3. Make technical depth evident via real API integration and clean architecture.",
                    tags = "hackathon, criteria, rules"
                )
            )

            repository.sendMessage("I'm starting my 90-day journey to win a national hackathon. What is my first move?")
            _isGenerating.value = false
            onComplete()
        }
    }

    /**
     * Clear all journey data (for testing or account reset)
     */
    fun clearAllData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            allGoals.value.forEach { repository.deleteGoal(it) }
            notes.value.forEach { repository.deleteNote(it) }
            memories.value.forEach { repository.deleteMemory(it) }
            hackathons.value.forEach { repository.deleteHackathon(it) }
            businessPlans.value.forEach { repository.deleteBusinessPlan(it) }
            repository.clearChat()
            repository.saveUserProfile(UserProfileEntity(isOnboardingCompleted = false))
            onComplete()
        }
    }
}
