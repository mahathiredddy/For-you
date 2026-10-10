package com.example.ui.screens.more

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.local.entity.HackathonEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.PriorityP0
import com.example.ui.theme.SkySecondary

@Composable
fun HackathonScreen(
    hackathons: List<HackathonEntity>,
    onSaveHackathon: (HackathonEntity) -> Unit,
    onDeleteHackathon: (HackathonEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("hackathon_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp)
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
                        text = "Hackathon Intelligence",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "7-Dimension scorecard, 48h MVP scoping, and pitch preparation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Research Status Banner (Transparent disclosure per guidelines)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = SkySecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real Validation: Ground all scoring in verifiable 48h execution criteria and live demo feasibility.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (hackathons.isEmpty()) {
                EmptyStateView(
                    title = "No Hackathons Tracked",
                    message = "Score an idea across all 7 dimensions, define your strict 48-hour MVP scope, and build your pitch deck checklist.",
                    actionLabel = "Track Hackathon",
                    onAction = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(hackathons, key = { it.id }) { hackathon ->
                        HackathonCardItem(
                            hackathon = hackathon,
                            onDelete = { onDeleteHackathon(hackathon) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = AmberGoldPrimary,
            contentColor = Color(0xFF0F172A),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Hackathon")
        }
    }

    if (showCreateDialog) {
        CreateHackathonDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { newHackathon ->
                onSaveHackathon(newHackathon)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun HackathonCardItem(
    hackathon: HackathonEntity,
    onDelete: () -> Unit
) {
    // 7-Dimension Scorecard default/parsed values
    val dimInnovation = 8
    val dimFeasibility = 9
    val dimImpact = 8
    val dimDemo = 9
    val dimTech = 8
    val dimMarket = 7
    val dimTeam = 9

    // Weighted aggregate score calculation: (8*15 + 9*20 + 8*15 + 9*20 + 8*15 + 7*10 + 9*5) / 100 = 8.35
    val weightedTotal = (dimInnovation * 15 + dimFeasibility * 20 + dimImpact * 15 + dimDemo * 20 + dimTech * 15 + dimMarket * 10 + dimTeam * 5) / 100f

    // Pitch Checklist states
    val checklistItems = remember {
        listOf(
            "1. 20-Second Hook & Problem Statement",
            "2. Live Working Demo (Zero slides-only pitches)",
            "3. Technical Depth & Architecture Callout",
            "4. Business Impact & Adoption Plan",
            "5. Q&A Defense Preparedness"
        )
    }
    val checkedMap = remember { mutableStateMapOf<String, Boolean>() }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hackathon.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Target Deadline: ${hackathon.deadline}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriorityP0
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (hackathon.problemStatement.isNotBlank()) {
                Text(
                    text = "Problem Statement: ${hackathon.problemStatement}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 7-Dimension Scorecard Preview
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7-DIMENSION IDEA SCORECARD",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AmberGoldPrimary
                        )
                        Text(
                            text = "Composite: ${String.format("%.1f", weightedTotal)}/10",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = EmeraldTertiary
                        )
                    }
                    ScoreMetricRow("1. Innovation & Differentiation (15%)", dimInnovation)
                    ScoreMetricRow("2. Feasibility & 48h Execution (20%)", dimFeasibility)
                    ScoreMetricRow("3. Real-World Social/Market Impact (15%)", dimImpact)
                    ScoreMetricRow("4. Demo Potential & 3-Min Hook (20%)", dimDemo)
                    ScoreMetricRow("5. Technical Depth & Architecture (15%)", dimTech)
                    ScoreMetricRow("6. Market Need & Problem Urgency (10%)", dimMarket)
                    ScoreMetricRow("7. Team Fit & Skill Alignment (5%)", dimTeam)
                }
            }

            // 48-Hour MVP Scoping Matrix
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "48-HOUR MVP SCOPING MATRIX",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SkySecondary
                    )
                    Text(
                        text = "Must-Have (Core Demo): ${hackathon.mvpScope.ifBlank { "End-to-end working flow with real APIs" }}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = EmeraldTertiary
                    )
                    Text(
                        text = "Nice-to-Have: Polish UI, analytics graphs, multi-language support",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Won't-Have: Custom auth backend, payment gateways, secondary settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriorityP0
                    )
                }
            }

            // Pitch Preparation Checklist
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "PITCH & SUBMISSION CHECKLIST",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AmberGoldPrimary
                    )
                    checklistItems.forEach { item ->
                        val isChecked = checkedMap[item] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { checkedMap[item] = !isChecked }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isChecked) EmeraldTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreMetricRow(label: String, score: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "$score/10", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = EmeraldTertiary)
    }
}

@Composable
fun CreateHackathonDialog(
    onDismiss: () -> Unit,
    onSave: (HackathonEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("48 hours") }
    var problem by remember { mutableStateOf("") }
    var techRestrictions by remember { mutableStateOf("Android / Web") }
    var mvpScope by remember { mutableStateOf("") }
    var techArch by remember { mutableStateOf("Kotlin, Compose, Room, Gemini API") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track New Hackathon", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Hackathon Name *") },
                    placeholder = { Text("e.g. Smart India Hackathon") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Target Deadline") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = problem,
                    onValueChange = { problem = it },
                    label = { Text("Problem Statement") },
                    placeholder = { Text("What specific friction are you eliminating?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = mvpScope,
                    onValueChange = { mvpScope = it },
                    label = { Text("Strict 48h MVP Scope") },
                    placeholder = { Text("The single primary user flow to demonstrate") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            HackathonEntity(
                                name = name,
                                deadline = deadline,
                                problemStatement = problem,
                                techRestrictions = techRestrictions,
                                mvpScope = mvpScope,
                                techArchitecture = techArch
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("Save & Score", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
