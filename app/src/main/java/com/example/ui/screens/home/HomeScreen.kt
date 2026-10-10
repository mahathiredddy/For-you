package com.example.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiEngineStatus
import com.example.data.ai.NextMoveRecommendation
import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.RoadmapPhaseEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.components.AiStatusChip
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GoalHeaderCard
import com.example.ui.components.MetricCard
import com.example.ui.components.NextMoveCard
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.SkySecondary

@Composable
fun HomeScreen(
    userProfile: UserProfileEntity?,
    activeGoal: GoalEntity?,
    nextMove: NextMoveRecommendation,
    todayTasks: List<TaskEntity>,
    phases: List<RoadmapPhaseEntity>,
    milestones: List<MilestoneEntity>,
    checkIns: List<DailyCheckInEntity>,
    engineStatus: AiEngineStatus,
    onStartNextMove: () -> Unit,
    onAlternativeSelected: (String) -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToRoadmap: () -> Unit,
    onNavigateToCoach: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onOpenCheckInDialog: () -> Unit,
    onCompleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("home_screen_content"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WE’RE HERE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = AmberGoldPrimary
                )
                Text(
                    text = "Welcome back, ${userProfile?.name?.ifBlank { "Explorer" } ?: "Explorer"}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            AiStatusChip(status = engineStatus)
        }

        // Active Goal Glance
        if (activeGoal != null) {
            GoalHeaderCard(
                goal = activeGoal,
                onViewDetails = onNavigateToGoals
            )
        } else {
            EmptyStateView(
                title = "No Active Goal",
                message = "Set a goal to let our AI build your structured roadmap and daily plan.",
                actionLabel = "Create Goal",
                onAction = onNavigateToGoals
            )
        }

        // CORE FEATURE: YOUR NEXT MOVE
        NextMoveCard(
            recommendation = nextMove,
            onStartAction = onStartNextMove,
            onAlternativeSelected = onAlternativeSelected
        )

        // Metrics Quick Glance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val completedTasksCount = todayTasks.count { it.status == "COMPLETED" }
            val completedMilestonesCount = milestones.count { it.isCompleted }

            MetricCard(
                label = "Today's Tasks",
                value = "$completedTasksCount/${todayTasks.size}",
                icon = Icons.Default.CheckCircle,
                iconTint = EmeraldTertiary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Milestones",
                value = "$completedMilestonesCount/${milestones.size}",
                icon = Icons.Default.Flag,
                iconTint = AmberGoldPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Check-ins",
                value = "${checkIns.size}",
                icon = Icons.Default.RateReview,
                iconTint = SkySecondary,
                modifier = Modifier.weight(1f)
            )
        }

        // Today's Priority Queue Preview
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Action Items",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = AmberGoldPrimary,
                        modifier = Modifier.clickable { onNavigateToToday() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val pendingTasks = todayTasks.filter { it.status != "COMPLETED" }
                if (pendingTasks.isNotEmpty()) {
                    pendingTasks.take(3).forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onCompleteTask(task) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Complete",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${task.estimatedMinutes} min • ${task.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            PriorityBadge(priority = task.priority)
                        }
                    }
                } else if (todayTasks.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = EmeraldTertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All scheduled tasks completed for today!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldTertiary
                        )
                    }
                } else {
                    Text(
                        text = "No tasks queued yet. Tap 'View All' to add tasks or check your roadmap.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Active Phase & Next Milestone Banner
        val currentPhase = phases.firstOrNull { !it.isCompleted } ?: phases.firstOrNull()
        val nextMilestone = milestones.firstOrNull { !it.isCompleted }

        if (currentPhase != null) {
            Card(
                onClick = onNavigateToRoadmap,
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ACTIVE ROADMAP PHASE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = SkySecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentPhase.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentPhase.purpose,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (nextMilestone != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Flag, null, tint = AmberGoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Target Milestone: ${nextMilestone.title}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenCheckInDialog,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.RateReview, null, tint = AmberGoldPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Check In", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }

            Button(
                onClick = onNavigateToCoach,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Psychology, null, tint = SkySecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Coach", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
