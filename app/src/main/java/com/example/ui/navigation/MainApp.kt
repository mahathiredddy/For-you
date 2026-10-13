package com.example.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.coach.AiCoachScreen
import com.example.ui.screens.goals.GoalsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.more.BusinessScreen
import com.example.ui.screens.more.ExploreScreen
import com.example.ui.screens.more.HackathonScreen
import com.example.ui.screens.more.ImLostScreen
import com.example.ui.screens.more.LearningHubScreen
import com.example.ui.screens.more.MemoryScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.more.NotesScreen
import com.example.ui.screens.more.ProgressReviewScreen
import com.example.ui.screens.more.ProjectsScreen
import com.example.ui.screens.more.SettingsScreen
import com.example.ui.screens.more.WealthScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.roadmap.RoadmapScreen
import com.example.ui.screens.today.TodayScreen
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.MidnightDarkSurface
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.WereHereViewModel

@Composable
fun MainApp(
    viewModel: WereHereViewModel,
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val activeGoal by viewModel.activeGoal.collectAsStateWithLifecycle()
    val nextMove by viewModel.nextMove.collectAsStateWithLifecycle()
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val phases by viewModel.phases.collectAsStateWithLifecycle()
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    val goalTasks by viewModel.goalTasks.collectAsStateWithLifecycle()
    val checkIns by viewModel.checkIns.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val skills by viewModel.skills.collectAsStateWithLifecycle()
    val hackathons by viewModel.hackathons.collectAsStateWithLifecycle()
    val businessPlans by viewModel.businessPlans.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val customBackendUrl by viewModel.customBackendUrl.collectAsStateWithLifecycle()
    val devApiKey by viewModel.devApiKey.collectAsStateWithLifecycle()
    val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // If onboarding is not completed, display guided OnboardingScreen
    if (userProfile?.isOnboardingCompleted != true) {
        OnboardingScreen(
            isGenerating = isGenerating,
            onCompleteOnboarding = { profile, title, desc, cat, outcome, deadline, motivation ->
                viewModel.completeOnboarding(
                    profile = profile,
                    initialGoalTitle = title,
                    initialGoalDesc = desc,
                    category = cat,
                    desiredOutcome = outcome,
                    deadline = deadline,
                    motivation = motivation,
                    onComplete = {
                        currentScreen = Screen.Home
                    }
                )
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                // Show bottom navigation bar only on primary screens
                if (currentScreen in Screen.bottomNavItems) {
                    NavigationBar(
                        containerColor = MidnightDarkSurface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        Screen.bottomNavItems.forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    screen.icon?.let { icon ->
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            fontSize = 10.sp
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AmberGoldPrimary,
                                    selectedTextColor = AmberGoldPrimary,
                                    indicatorColor = AmberGoldPrimary.copy(alpha = 0.18f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_item_${screen.route}")
                            )
                        }
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                    when (screen) {
                        Screen.Home -> HomeScreen(
                            userProfile = userProfile,
                            activeGoal = activeGoal,
                            nextMove = nextMove,
                            todayTasks = todayTasks,
                            phases = phases,
                            milestones = milestones,
                            checkIns = checkIns,
                            engineStatus = viewModel.getEngineStatus(),
                            onStartNextMove = {
                                nextMove.taskId?.let { taskId ->
                                    val task = todayTasks.find { it.id == taskId } ?: goalTasks.find { it.id == taskId }
                                    if (task != null) {
                                        viewModel.completeTask(task, task.estimatedMinutes)
                                    }
                                }
                            },
                            onAlternativeSelected = { alt -> viewModel.selectAlternativeNextMove(alt) },
                            onNavigateToGoals = { currentScreen = Screen.Goals },
                            onNavigateToToday = { currentScreen = Screen.Today },
                            onNavigateToRoadmap = { currentScreen = Screen.Roadmap },
                            onNavigateToCoach = { currentScreen = Screen.Coach },
                            onNavigateToNotes = { currentScreen = Screen.Notes },
                            onOpenCheckInDialog = { currentScreen = Screen.Today },
                            onCompleteTask = { task -> viewModel.completeTask(task) }
                        )

                        Screen.Goals -> GoalsScreen(
                            goals = allGoals,
                            activeGoal = activeGoal,
                            isGenerating = isGenerating,
                            onSelectGoal = { goal -> viewModel.selectActiveGoal(goal) },
                            onCreateGoal = { title, desc, cat, outcome, deadline, mot, hrs, budget, constr ->
                                viewModel.createGoal(title, desc, cat, outcome, deadline, mot, hrs, budget, constr) {}
                            },
                            onRegenerateRoadmap = { viewModel.regenerateRoadmapForActiveGoal() },
                            onUpdateGoalStatus = { goal, newStatus ->
                                viewModel.updateGoalStatus(goal, newStatus)
                            }
                        )

                        Screen.Today -> TodayScreen(
                            activeGoal = activeGoal,
                            nextMove = nextMove,
                            tasks = todayTasks,
                            nextMilestone = milestones.firstOrNull { !it.isCompleted },
                            onCompleteTask = { task, minutes -> viewModel.completeTask(task, minutes) },
                            onSkipTask = { task -> viewModel.skipTask(task) },
                            onRescheduleTask = { task -> viewModel.rescheduleTask(task) },
                            onAddTask = { task -> viewModel.addTask(task) },
                            onUpdateTask = { task -> viewModel.updateTask(task) },
                            onStartNextMove = {
                                nextMove.taskId?.let { taskId ->
                                    val task = todayTasks.find { it.id == taskId } ?: goalTasks.find { it.id == taskId }
                                    if (task != null) {
                                        viewModel.completeTask(task, task.estimatedMinutes)
                                    }
                                }
                            },
                            onAlternativeSelected = { alt -> viewModel.selectAlternativeNextMove(alt) },
                            onSubmitCheckIn = { rating, hours, accomplished, wentWrong, learned, blockers, onDone ->
                                viewModel.submitDailyCheckIn(rating, hours, accomplished, wentWrong, learned, blockers, onDone)
                            }
                        )

                        Screen.Roadmap -> RoadmapScreen(
                            activeGoal = activeGoal,
                            phases = phases,
                            milestones = milestones,
                            tasks = goalTasks,
                            isGenerating = isGenerating,
                            onToggleMilestone = { milestone -> viewModel.toggleMilestone(milestone) },
                            onCompleteTask = { task -> viewModel.completeTask(task) },
                            onRegenerateRoadmap = { viewModel.regenerateRoadmapForActiveGoal() }
                        )

                        Screen.Coach -> AiCoachScreen(
                            messages = chatMessages,
                            activeGoal = activeGoal,
                            engineStatus = viewModel.getEngineStatus(),
                            onSendMessage = { query -> viewModel.sendChatMessage(query) },
                            onClearChat = { viewModel.clearChat() }
                        )

                        Screen.More -> MoreScreen(
                            onNavigateToDestination = { target -> currentScreen = target }
                        )

                        Screen.LearningHub -> LearningHubScreen(
                            skills = skills,
                            onSaveSkill = { skill -> viewModel.saveSkill(skill) },
                            onLogPractice = { skill, hours -> viewModel.logSkillPractice(skill, hours) },
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.Notes -> NotesScreen(
                            notes = notes,
                            activeGoalId = activeGoal?.id,
                            onSaveNote = { note -> viewModel.saveNote(note) },
                            onDeleteNote = { note -> viewModel.deleteNote(note) },
                            onConvertNoteToTask = { task ->
                                viewModel.addTask(task)
                                currentScreen = Screen.Today
                            },
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.Progress -> ProgressReviewScreen(
                            activeGoal = activeGoal,
                            tasks = goalTasks,
                            milestones = milestones,
                            checkIns = checkIns,
                            onSaveWeeklyReview = { wins, missed, lessons, priorities ->
                                viewModel.saveWeeklyReview(wins, missed, lessons, priorities)
                            },
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.Hackathon -> HackathonScreen(
                            hackathons = hackathons,
                            onSaveHackathon = { hackathon -> viewModel.saveHackathon(hackathon) },
                            onDeleteHackathon = { hackathon -> viewModel.deleteHackathon(hackathon) },
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.Projects -> ProjectsScreen(
                            activeGoal = activeGoal,
                            tasks = goalTasks,
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.Business -> BusinessScreen(
                            plans = businessPlans,
                            onSavePlan = { plan -> viewModel.saveBusinessPlan(plan) },
                            onDeletePlan = { plan -> viewModel.deleteBusinessPlan(plan) },
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.Wealth -> WealthScreen(
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.ImLost -> ImLostScreen(
                            onAdoptGoal = { title, desc, cat, firstAction ->
                                viewModel.createGoal(
                                    title = title,
                                    description = desc,
                                    category = cat,
                                    desiredOutcome = "Complete $title",
                                    deadline = "6 months",
                                    motivation = "Direction finder strategic alignment: $firstAction",
                                    hoursPerWeek = 21f,
                                    budget = "₹0",
                                    constraints = "Focus on initial step"
                                ) {
                                    currentScreen = Screen.Today
                                }
                            },
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.Memory -> MemoryScreen(
                            memories = memories,
                            onDeleteMemory = { memory -> viewModel.deleteMemory(memory) },
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.Explore -> ExploreScreen(
                            onSaveToNotes = { note -> viewModel.saveNote(note) },
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.Auth -> AuthScreen(
                            authViewModel = authViewModel,
                            onContinueOffline = { currentScreen = Screen.Home },
                            onBack = { currentScreen = Screen.More }
                        )

                        Screen.Settings -> SettingsScreen(
                            userProfile = userProfile,
                            engineStatus = viewModel.getEngineStatus(),
                            customBackendUrl = customBackendUrl,
                            devApiKey = devApiKey,
                            isTestingConnection = isTestingConnection,
                            onSaveCustomBackendUrl = { url -> viewModel.setCustomBackendUrl(url) },
                            onSaveDevApiKey = { key -> viewModel.setDevApiKey(key) },
                            onTestConnection = { onResult -> viewModel.testAiConnection(onResult) },
                            onSaveProfile = { profile ->
                                viewModel.saveUserProfile(profile)
                            },
                            onClearChat = { viewModel.clearChat() },
                            onEmergencyReset = {
                                viewModel.emergencyResetPlan {
                                    currentScreen = Screen.Today
                                }
                            },
                            onLoadDemoMode = {
                                viewModel.loadDemoAlexProfile {
                                    currentScreen = Screen.Home
                                }
                            },
                            onNavigateToAuth = { currentScreen = Screen.Auth },
                            onClearAllData = {
                                viewModel.clearAllData {
                                    currentScreen = Screen.Home
                                }
                            },
                            onBack = { currentScreen = Screen.More }
                        )
                    }
                }
            }
        }
    }
}
