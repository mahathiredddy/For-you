package com.example.ui.screens.today

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.ai.NextMoveRecommendation
import com.example.data.ai.PlanAdaptationResult
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MilestoneEntity
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NextMoveCard
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.PriorityP0
import com.example.ui.theme.PriorityP1
import com.example.ui.theme.PriorityP2
import com.example.ui.theme.SkySecondary

@Composable
fun TodayScreen(
    activeGoal: GoalEntity?,
    nextMove: NextMoveRecommendation,
    tasks: List<TaskEntity>,
    nextMilestone: MilestoneEntity?,
    onCompleteTask: (TaskEntity, Int) -> Unit,
    onSkipTask: (TaskEntity) -> Unit,
    onRescheduleTask: (TaskEntity) -> Unit,
    onAddTask: (TaskEntity) -> Unit,
    onUpdateTask: (TaskEntity) -> Unit,
    onStartNextMove: () -> Unit,
    onAlternativeSelected: (String) -> Unit = {},
    onSubmitCheckIn: (String, Float, String, String, String, String, (PlanAdaptationResult) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showCheckInDialog by remember { mutableStateOf(false) }
    var selectedTaskForDetails by remember { mutableStateOf<TaskEntity?>(null) }
    var adaptationFeedback by remember { mutableStateOf<PlanAdaptationResult?>(null) }

    val completedCount = tasks.count { it.status == "COMPLETED" }
    val totalTimeMinutes = tasks.filter { it.status != "COMPLETED" }.sumOf { it.estimatedMinutes }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("today_screen_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today Top Header & Check-in Trigger
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Execution",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (activeGoal != null) "Focus: ${activeGoal.title}" else "No active goal selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showCheckInDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("daily_checkin_button")
                ) {
                    Icon(Icons.Default.RateReview, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Check In", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Today's Main Objective Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TODAY'S OBJECTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = SkySecondary
                        )
                        Text(
                            text = "~$totalTimeMinutes min workload",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (nextMilestone != null) {
                            "Advance towards: ${nextMilestone.title}"
                        } else {
                            "Execute foundational actions and preserve daily consistency."
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Completion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$completedCount of ${tasks.size} done",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AmberGoldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size.toFloat() else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = EmeraldTertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // Prominent YOUR NEXT MOVE card
        item {
            NextMoveCard(
                recommendation = nextMove,
                onStartAction = onStartNextMove,
                onAlternativeSelected = onAlternativeSelected
            )
        }

        // Today Tasks Header with Add Task button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Action Items",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { showAddTaskDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = AmberGoldPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Task Items
        if (tasks.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No Tasks Scheduled for Today",
                    message = "Tap 'Add Task' to schedule a task or check your Roadmap to pull upcoming tasks.",
                    actionLabel = "Add Today Task",
                    onAction = { showAddTaskDialog = true }
                )
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onComplete = { onCompleteTask(task, task.estimatedMinutes) },
                    onSkip = { onSkipTask(task) },
                    onReschedule = { onRescheduleTask(task) },
                    onViewInstructions = { selectedTaskForDetails = task }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            activeGoalId = activeGoal?.id ?: 1L,
            onDismiss = { showAddTaskDialog = false },
            onAdd = { newTask ->
                onAddTask(newTask)
                showAddTaskDialog = false
            }
        )
    }

    // Task Instructions & Details Dialog
    selectedTaskForDetails?.let { task ->
        TaskDetailDialog(
            task = task,
            onDismiss = { selectedTaskForDetails = null },
            onComplete = { minutes ->
                onCompleteTask(task, minutes)
                selectedTaskForDetails = null
            }
        )
    }

    // Daily Check-in Dialog
    if (showCheckInDialog) {
        DailyCheckInDialog(
            onDismiss = { showCheckInDialog = false },
            onSubmit = { rating, hours, accomplished, wentWrong, learned, blockers ->
                onSubmitCheckIn(rating, hours, accomplished, wentWrong, learned, blockers) { adaptation ->
                    adaptationFeedback = adaptation
                }
                showCheckInDialog = false
            }
        )
    }

    // Plan Adaptation Feedback Alert
    adaptationFeedback?.let { adaptation ->
        AlertDialog(
            onDismissRequest = { adaptationFeedback = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = AmberGoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Plan Adapted for Tomorrow", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = adaptation.strategySummary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Recommendation: ${adaptation.recommendedAction}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Target task pacing: ${adaptation.suggestedTaskSizeAdjustment}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkySecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { adaptationFeedback = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A))
                ) {
                    Text("Understood")
                }
            }
        )
    }
}

@Composable
private fun TaskItemCard(
    task: TaskEntity,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onReschedule: () -> Unit,
    onViewInstructions: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isDone = task.status == "COMPLETED"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onComplete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Toggle Complete",
                    tint = if (isDone) EmeraldTertiary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewInstructions() }
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    ),
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${task.estimatedMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${task.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            PriorityBadge(priority = task.priority)

            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("View Instructions") },
                        onClick = {
                            menuExpanded = false
                            onViewInstructions()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reschedule Task") },
                        onClick = {
                            menuExpanded = false
                            onReschedule()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Skip Task") },
                        onClick = {
                            menuExpanded = false
                            onSkip()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDetailDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onComplete: (Int) -> Unit
) {
    var actualMinutes by remember { mutableIntStateOf(task.estimatedMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                PriorityBadge(priority = task.priority)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = task.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (task.description.isNotBlank()) {
                    Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Execution Instructions:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SkySecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.instructions.ifBlank { "Execute step-by-step without multitasking. Verify output when finished." },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Actual Time Spent (Minutes):", style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { if (actualMinutes > 5) actualMinutes -= 5 },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.size(32.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("-")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "$actualMinutes", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { actualMinutes += 5 },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.size(32.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("+")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onComplete(actualMinutes) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary, contentColor = Color.White)
            ) {
                Text("Mark Completed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AddTaskDialog(
    activeGoalId: Long,
    onDismiss: () -> Unit,
    onAdd: (TaskEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("P1") }
    var minutesText by remember { mutableStateOf("45") }
    var category by remember { mutableStateOf("Execution") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Daily Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it },
                        label = { Text("Est. Minutes") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Text(text = "Priority Level:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("P0", "P1", "P2").forEach { p ->
                        val isSel = priority == p
                        Surface(
                            color = if (isSel) when (p) {
                                "P0" -> PriorityP0
                                "P1" -> PriorityP1
                                else -> PriorityP2
                            } else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = p }
                        ) {
                            Text(
                                text = p,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            TaskEntity(
                                goalId = activeGoalId,
                                title = title,
                                description = desc,
                                instructions = instructions,
                                priority = priority,
                                estimatedMinutes = minutesText.toIntOrNull() ?: 45,
                                isTodayTask = true,
                                category = category
                            )
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("Add Task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DailyCheckInDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, Float, String, String, String, String) -> Unit
) {
    var rating by remember { mutableStateOf("Good") }
    var hoursText by remember { mutableStateOf("2.0") }
    var accomplished by remember { mutableStateOf("") }
    var wentWrong by remember { mutableStateOf("") }
    var learned by remember { mutableStateOf("") }
    var blockers by remember { mutableStateOf("") }

    val ratings = listOf("Crushed it", "Good", "Average", "Struggled", "Didn't happen")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Daily Check-In", fontWeight = FontWeight.Bold)
                Text(
                    text = "How did execution go today? Your plan will adapt automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Rating:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    ratings.forEach { r ->
                        val isSel = rating == r
                        Surface(
                            color = if (isSel) AmberGoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { rating = r }
                        ) {
                            Text(
                                text = r.split(" ").first(),
                                color = if (isSel) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { hoursText = it },
                    label = { Text("Actual Hours Invested Today") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = accomplished,
                    onValueChange = { accomplished = it },
                    label = { Text("What was accomplished?") },
                    placeholder = { Text("Key deliverables completed") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = blockers,
                    onValueChange = { blockers = it },
                    label = { Text("Any friction or blockers?") },
                    placeholder = { Text("What got in the way, if anything?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = learned,
                    onValueChange = { learned = it },
                    label = { Text("What was learned?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hrs = hoursText.toFloatOrNull() ?: 2.0f
                    onSubmit(rating, hrs, accomplished, wentWrong, learned, blockers)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("Submit & Adapt Plan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
