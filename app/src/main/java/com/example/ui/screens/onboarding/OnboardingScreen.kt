package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.SkySecondary

@Composable
fun OnboardingScreen(
    isGenerating: Boolean,
    onCompleteOnboarding: (UserProfileEntity, String, String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) } // 0: Welcome, 1: Profile, 2: Capacity & Constraints, 3: Goal, 4: Style, 5: Review

    // User Profile fields
    var name by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Student") }
    var educationField by remember { mutableStateOf("") }
    var currentSkills by remember { mutableStateOf("") }
    var hoursPerDay by remember { mutableFloatStateOf(2.5f) }
    var budget by remember { mutableStateOf("$0 (Free resources)") }
    var biggestConstraints by remember { mutableStateOf("Time & structured guidance") }
    var learningStyle by remember { mutableStateOf("Hands-on projects") }
    var planIntensity by remember { mutableStateOf("Balanced") }

    // Goal fields
    var goalTitle by remember { mutableStateOf("Build a production-grade full-stack portfolio app") }
    var goalCategory by remember { mutableStateOf("Programming") }
    var goalDescription by remember { mutableStateOf("Design, build, and deploy a complete functional product to showcase in interviews.") }
    var desiredOutcome by remember { mutableStateOf("Land a high-impact software engineering role within 4 months.") }
    var targetDeadline by remember { mutableStateOf("12 weeks") }
    var motivation by remember { mutableStateOf("Gain genuine technical mastery and career independence.") }

    val totalSteps = 6

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top Bar with Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                IconButton(
                    onClick = { step -= 1 },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Step",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "WE’RE HERE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = AmberGoldPrimary
                )
                Text(
                    text = "Step ${step + 1} of $totalSteps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { (step + 1) / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = AmberGoldPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Step Content Scrollable
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = step,
                label = "onboarding_step_anim"
            ) { targetStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (targetStep) {
                        0 -> WelcomeStep()
                        1 -> ProfileStep(
                            name = name, onNameChange = { name = it },
                            age = ageText, onAgeChange = { ageText = it },
                            location = location, onLocationChange = { location = it },
                            status = status, onStatusChange = { status = it },
                            education = educationField, onEducationChange = { educationField = it },
                            skills = currentSkills, onSkillsChange = { currentSkills = it }
                        )
                        2 -> CapacityStep(
                            hoursPerDay = hoursPerDay, onHoursChange = { hoursPerDay = it },
                            budget = budget, onBudgetChange = { budget = it },
                            constraints = biggestConstraints, onConstraintsChange = { biggestConstraints = it }
                        )
                        3 -> GoalStep(
                            title = goalTitle, onTitleChange = { goalTitle = it },
                            category = goalCategory, onCategoryChange = { goalCategory = it },
                            description = goalDescription, onDescriptionChange = { goalDescription = it },
                            outcome = desiredOutcome, onOutcomeChange = { desiredOutcome = it },
                            deadline = targetDeadline, onDeadlineChange = { targetDeadline = it },
                            motivation = motivation, onMotivationChange = { motivation = it }
                        )
                        4 -> StyleStep(
                            learningStyle = learningStyle, onStyleChange = { learningStyle = it },
                            planIntensity = planIntensity, onIntensityChange = { planIntensity = it }
                        )
                        5 -> ReviewStep(
                            name = name,
                            status = status,
                            hoursPerDay = hoursPerDay,
                            goalTitle = goalTitle,
                            category = goalCategory,
                            deadline = targetDeadline,
                            motivation = motivation
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step in 1..4) {
                OutlinedButton(
                    onClick = { step += 1 },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Skip Optional", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = {
                    if (step < 5) {
                        step += 1
                    } else {
                        val profile = UserProfileEntity(
                            name = name.ifBlank { "Explorer" },
                            age = ageText.toIntOrNull(),
                            location = location,
                            status = status,
                            educationField = educationField,
                            currentSkills = currentSkills,
                            hoursPerDay = hoursPerDay,
                            budget = budget,
                            biggestConstraints = biggestConstraints,
                            learningStyle = learningStyle,
                            planIntensity = planIntensity,
                            isOnboardingCompleted = true
                        )
                        onCompleteOnboarding(
                            profile,
                            goalTitle,
                            goalDescription,
                            goalCategory,
                            desiredOutcome,
                            targetDeadline,
                            motivation
                        )
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier
                    .weight(if (step in 1..4) 1.2f else 1f)
                    .testTag("onboarding_next_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGoldPrimary,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF0F172A),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Architecting Roadmap...", fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = if (step == 0) "Get Started" else if (step == 5) "Generate Roadmap" else "Next",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (step == 5) Icons.Default.AutoAwesome else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            shape = CircleShape,
            color = AmberGoldPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AmberGoldPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tell us where you want to go.\nWe’ll help you figure out how to get there.",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "WE’RE HERE is your personal goal execution platform. We transform any ambitious ambition into structured roadmap phases, actionable daily tasks, and an adaptive coach that adjusts to real life.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PillValue(Icons.Default.Flag, "Set any meaningful goal (Career, Code, Business, Hackathons)")
                PillValue(Icons.Default.Schedule, "Honest feasibility analysis based on your real hours")
                PillValue(Icons.Default.AutoAwesome, "Daily 'YOUR NEXT MOVE' guidance to eliminate paralysis")
            }
        }
    }
}

@Composable
private fun PillValue(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = AmberGoldPrimary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ProfileStep(
    name: String, onNameChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    location: String, onLocationChange: (String) -> Unit,
    status: String, onStatusChange: (String) -> Unit,
    education: String, onEducationChange: (String) -> Unit,
    skills: String, onSkillsChange: (String) -> Unit
) {
    Text(
        text = "About You",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "This helps the AI calibrate starting points and avoid unrealistic assumptions.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Your Name or Moniker") },
        placeholder = { Text("e.g. Alex, Sam") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = age,
            onValueChange = onAgeChange,
            label = { Text("Age (Optional)") },
            placeholder = { Text("e.g. 24") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("Location (Optional)") },
            placeholder = { Text("City, Country") },
            modifier = Modifier.weight(1.5f),
            shape = RoundedCornerShape(12.dp)
        )
    }

    Text(
        text = "Current Status",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
    )
    val statuses = listOf("Student", "Employed", "Entrepreneur", "Job Seeker", "Other")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statuses.forEach { item ->
            val isSelected = status == item
            Surface(
                color = if (isSelected) AmberGoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .clickable { onStatusChange(item) }
                    .weight(1f)
            ) {
                Text(
                    text = item,
                    color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    OutlinedTextField(
        value = education,
        onValueChange = onEducationChange,
        label = { Text("Education & Field") },
        placeholder = { Text("e.g. Computer Science, Business, Self-taught") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = skills,
        onValueChange = onSkillsChange,
        label = { Text("Current Skills & Baseline Experience") },
        placeholder = { Text("e.g. Basic Kotlin, Git, UI design, beginner marketing") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        minLines = 2
    )
}

@Composable
private fun CapacityStep(
    hoursPerDay: Float, onHoursChange: (Float) -> Unit,
    budget: String, onBudgetChange: (String) -> Unit,
    constraints: String, onConstraintsChange: (String) -> Unit
) {
    Text(
        text = "Capacity & Constraints",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "Be ruthlessly honest with your time. Overcommitting leads to burnout.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Available Time Per Day:", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${hoursPerDay} hrs/day (${(hoursPerDay * 7).toInt()} hrs/week)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = AmberGoldPrimary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            androidx.compose.material3.Slider(
                value = hoursPerDay,
                onValueChange = onHoursChange,
                valueRange = 0.5f..8f,
                steps = 15
            )
        }
    }

    OutlinedTextField(
        value = budget,
        onValueChange = onBudgetChange,
        label = { Text("Available Budget / Resources (Optional)") },
        placeholder = { Text("e.g. Free only, $50/mo for tools, laptop only") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = constraints,
        onValueChange = onConstraintsChange,
        label = { Text("Biggest Constraints / Bottlenecks") },
        placeholder = { Text("e.g. Full-time job, low energy evenings, lack of accountability") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        minLines = 2
    )
}

@Composable
private fun GoalStep(
    title: String, onTitleChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    outcome: String, onOutcomeChange: (String) -> Unit,
    deadline: String, onDeadlineChange: (String) -> Unit,
    motivation: String, onMotivationChange: (String) -> Unit
) {
    Text(
        text = "Your Primary Goal",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "Tell us what you want to achieve. We will build your structured execution plan.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text("Goal Title") },
        placeholder = { Text("e.g. Win a Web3 Hackathon / Launch an AI Micro-SaaS") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Text(
        text = "Category",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
    )
    val categories = listOf("Programming", "Career", "Hackathons", "Business", "Education", "Personal Project")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            categories.take(3).forEach { cat ->
                CategoryChip(cat, category == cat) { onCategoryChange(cat) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            categories.drop(3).forEach { cat ->
                CategoryChip(cat, category == cat) { onCategoryChange(cat) }
            }
        }
    }

    OutlinedTextField(
        value = outcome,
        onValueChange = onOutcomeChange,
        label = { Text("Desired Outcome (Definition of Done)") },
        placeholder = { Text("e.g. Working deployed MVP with 5 active users and a submission video") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = deadline,
            onValueChange = onDeadlineChange,
            label = { Text("Target Deadline") },
            placeholder = { Text("e.g. 6 weeks") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )
    }

    OutlinedTextField(
        value = motivation,
        onValueChange = onMotivationChange,
        label = { Text("Why This Goal Matters (Deep Motivation)") },
        placeholder = { Text("e.g. To prove technical competency and transition into founder mode") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        minLines = 2
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.CategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) AmberGoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
    ) {
        Text(
            text = name,
            color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun StyleStep(
    learningStyle: String, onStyleChange: (String) -> Unit,
    planIntensity: String, onIntensityChange: (String) -> Unit
) {
    Text(
        text = "Execution Style",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )

    Text(
        text = "Preferred Learning & Execution Mode:",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
    )
    val styles = listOf(
        "Hands-on projects" to "Build real working apps and artifacts from Day 1",
        "Step-by-step theory" to "Master mental models before touching implementations",
        "Intensive bootcamp" to "High-volume practical exercises and speed runs"
    )
    styles.forEach { (st, desc) ->
        val selected = learningStyle == st
        Surface(
            color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (selected) AmberGoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStyleChange(st) }
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) AmberGoldPrimary else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGoldPrimary),
                    modifier = Modifier.size(20.dp)
                ) {
                    if (selected) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF0F172A), modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = st, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "Plan Intensity:",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
    )
    val intensities = listOf("Steady & Sustainable", "Balanced", "Sprint Mode")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        intensities.forEach { item ->
            val isSelected = planIntensity == item
            Surface(
                color = if (isSelected) SkySecondary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onIntensityChange(item) }
            ) {
                Text(
                    text = item,
                    color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ReviewStep(
    name: String,
    status: String,
    hoursPerDay: Float,
    goalTitle: String,
    category: String,
    deadline: String,
    motivation: String
) {
    Text(
        text = "Review & Launch Plan",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "Everything looks ready. Our execution engine will analyze feasibility and generate your customized roadmap.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ReviewRow("User", if (name.isNotBlank()) name else "Explorer")
            ReviewRow("Status", status)
            ReviewRow("Commitment", "${hoursPerDay} hrs/day (${(hoursPerDay * 7).toInt()} hrs/week)")
            ReviewRow("Target Goal", goalTitle)
            ReviewRow("Category", category)
            ReviewRow("Deadline", deadline)
            ReviewRow("Motivation", motivation)
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
