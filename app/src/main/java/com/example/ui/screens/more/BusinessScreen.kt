package com.example.ui.screens.more

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessPlanEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.PriorityP0
import com.example.ui.theme.PriorityP1
import com.example.ui.theme.SkySecondary

@Composable
fun BusinessScreen(
    plans: List<BusinessPlanEntity>,
    onSavePlan: (BusinessPlanEntity) -> Unit,
    onDeletePlan: (BusinessPlanEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("business_screen")
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
                        text = "Business Intelligence",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pragmatic validation, unit economics & 30-day plan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Non-promissory Disclaimer per instructions
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, null, tint = AmberGoldPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Principle: Never assume profitability before customer validation. Prioritize zero-cost experiments before investing capital.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (plans.isEmpty()) {
                EmptyStateView(
                    title = "No Business Models Created",
                    message = "Structure your idea, identify target customers, and map low-cost experiments.",
                    actionLabel = "New Business Plan",
                    onAction = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(plans, key = { it.id }) { plan ->
                        BusinessPlanCard(
                            plan = plan,
                            onDelete = { onDeletePlan(plan) }
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
            Icon(Icons.Default.Add, contentDescription = "New Business Model")
        }
    }

    if (showCreateDialog) {
        CreateBusinessPlanDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { newPlan ->
                onSavePlan(newPlan)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun BusinessPlanCard(
    plan: BusinessPlanEntity,
    onDelete: () -> Unit
) {
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
                Column {
                    Text(
                        text = plan.businessIdea,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Target: ${plan.targetCustomer} • Objective: ${plan.revenueObjective}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkySecondary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Customer Problem & Value Prop
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Problem & Value Proposition",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AmberGoldPrimary
                    )
                    Text(text = "Customer Problem: ${plan.customerProblem}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Value Proposition: ${plan.valueProposition}", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Unit Economics & Break-even
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Economics & Capital Requirements",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldTertiary
                    )
                    Text(text = "Starting Capital: ${plan.startingCapital} • Recurring: ${plan.recurringCosts}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Unit Economics: ${plan.unitEconomics}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Break-even Analysis: ${plan.breakEvenAnalysis}", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Low cost validation experiments
            Text(
                text = "Low-Cost Validation Experiments:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = plan.validationExperiments,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 30-Day Action Plan
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "30-Day Sprint Roadmap:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SkySecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = plan.thirtyDayPlan, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun CreateBusinessPlanDialog(
    onDismiss: () -> Unit,
    onSave: (BusinessPlanEntity) -> Unit
) {
    var idea by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var capital by remember { mutableStateOf("$0 - $100") }
    var revenueObj by remember { mutableStateOf("$1,000/mo") }
    var valueProp by remember { mutableStateOf("") }
    var experiments by remember { mutableStateOf("1. Interview 5 target users\n2. Create mock landing page") }
    var thirtyDayPlan by remember { mutableStateOf("Week 1: Customer discovery\nWeek 2: Minimum viable offer\nWeek 3: Outreach & feedback\nWeek 4: First transaction") }
    var sellingPriceStr by remember { mutableStateOf("29") }
    var unitCostStr by remember { mutableStateOf("3") }
    var monthlyFixedCostsStr by remember { mutableStateOf("60") }

    val sellingPrice = sellingPriceStr.toFloatOrNull() ?: 0f
    val unitCost = unitCostStr.toFloatOrNull() ?: 0f
    val monthlyFixed = monthlyFixedCostsStr.toFloatOrNull() ?: 0f
    val contributionMargin = sellingPrice - unitCost
    val marginPercent = if (sellingPrice > 0f) ((contributionMargin / sellingPrice) * 100).toInt() else 0
    val breakEvenUnits = if (contributionMargin > 0f) kotlin.math.ceil(monthlyFixed / contributionMargin).toInt() else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Business Strategy", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = idea,
                    onValueChange = { idea = it },
                    label = { Text("Business Idea *") },
                    placeholder = { Text("e.g. Localized AI menu generator") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = customer,
                    onValueChange = { customer = it },
                    label = { Text("Target Customer Profile") },
                    placeholder = { Text("e.g. Independent restaurant managers") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it },
                        label = { Text("Price/Unit ($)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = unitCostStr,
                        onValueChange = { unitCostStr = it },
                        label = { Text("Unit Cost ($)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = monthlyFixedCostsStr,
                        onValueChange = { monthlyFixedCostsStr = it },
                        label = { Text("Fixed/Mo ($)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Calculated Unit Economics Callout
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Unit Margin: $$contributionMargin ($marginPercent% margin)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldTertiary
                        )
                        Text(
                            text = "Break-even Volume: $breakEvenUnits customers/month",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberGoldPrimary
                        )
                    }
                }

                OutlinedTextField(
                    value = valueProp,
                    onValueChange = { valueProp = it },
                    label = { Text("Value Proposition") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (idea.isNotBlank()) {
                        val computedEconomics = "$$sellingPrice/unit ($$contributionMargin margin, $marginPercent% gross margin)"
                        val computedBreakEven = "Break-even at $breakEvenUnits units/month ($$monthlyFixed monthly overhead)"
                        onSave(
                            BusinessPlanEntity(
                                businessIdea = idea,
                                targetCustomer = customer,
                                customerProblem = problem,
                                startingCapital = capital,
                                revenueObjective = revenueObj,
                                valueProposition = valueProp,
                                startupCosts = "$50 (Domain & infrastructure)",
                                recurringCosts = "$$monthlyFixed/mo",
                                unitEconomics = computedEconomics,
                                breakEvenAnalysis = computedBreakEven,
                                validationExperiments = experiments,
                                thirtyDayPlan = thirtyDayPlan
                            )
                        )
                    }
                },
                enabled = idea.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("Generate Business Plan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
