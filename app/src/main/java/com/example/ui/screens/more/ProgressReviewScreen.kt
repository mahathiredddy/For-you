package com.example.ui.screens.more

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.MetricCard
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.SkySecondary

@Composable
fun ProgressReviewScreen(
    activeGoal: GoalEntity?,
    tasks: List<TaskEntity>,
    milestones: List<MilestoneEntity>,
    checkIns: List<DailyCheckInEntity>,
    onSaveWeeklyReview: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val scrollState = rememberScrollState()

    var winsText by remember { mutableStateOf("") }
    var missedText by remember { mutableStateOf("") }
    var lessonsText by remember { mutableStateOf("") }
    var nextPrioritiesText by remember { mutableStateOf("") }
    var reviewSubmitted by remember { mutableStateOf(false) }

    val totalCompletedTasks = tasks.count { it.status == "COMPLETED" }
    val totalTimeSpentHours = tasks.filter { it.status == "COMPLETED" }.sumOf { it.actualMinutesSpent } / 60f
    val milestonesAchieved = milestones.count { it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("progress_review_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Progress & Review",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Measurable execution analytics & weekly calibration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                label = "Goal Completion",
                value = "${activeGoal?.progressPercent ?: 0}%",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconTint = AmberGoldPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Hours Logged",
                value = "%.1fh".format(totalTimeSpentHours),
                icon = Icons.Default.Schedule,
                iconTint = SkySecondary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Milestones",
                value = "$milestonesAchieved/${milestones.size}",
                icon = Icons.Default.Flag,
                iconTint = EmeraldTertiary,
                modifier = Modifier.weight(1f)
            )
        }

        // Progress bar card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Goal Trajectory: ${activeGoal?.title ?: "Active Goal"}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (activeGoal?.progressPercent ?: 0) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = AmberGoldPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$totalCompletedTasks of ${tasks.size} total tasks completed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Weekly Retrospective Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "WEEKLY EXECUTION REVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = AmberGoldPrimary
                )
                Text(
                    text = "Calibrate what worked and resolve friction before beginning the next week's sprint.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = winsText,
                    onValueChange = { winsText = it },
                    label = { Text("Wins & Key Accomplishments") },
                    placeholder = { Text("What went exceptionally well this week?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = missedText,
                    onValueChange = { missedText = it },
                    label = { Text("Missed Tasks or Friction Points") },
                    placeholder = { Text("What did not happen and why?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = lessonsText,
                    onValueChange = { lessonsText = it },
                    label = { Text("Lessons Learned & Mental Models") },
                    placeholder = { Text("What new realization or technique did you uncover?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = nextPrioritiesText,
                    onValueChange = { nextPrioritiesText = it },
                    label = { Text("Next Week's Non-Negotiable Priorities") },
                    placeholder = { Text("Top 2 P0 outcomes for next week") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        onSaveWeeklyReview(winsText, missedText, lessonsText, nextPrioritiesText)
                        reviewSubmitted = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (reviewSubmitted) "Review Saved to Memory!" else "Save Weekly Review", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
