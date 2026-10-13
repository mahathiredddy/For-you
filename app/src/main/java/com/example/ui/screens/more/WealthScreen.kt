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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.PriorityP0
import com.example.ui.theme.PriorityP1
import com.example.ui.theme.SkySecondary

@Composable
fun WealthScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val scrollState = rememberScrollState()

    var targetMonthlyIncomeStr by remember { mutableStateOf("100000") }
    var currentMonthlyIncomeStr by remember { mutableStateOf("25000") }
    var monthlySavingsRatePercentStr by remember { mutableStateOf("30") }
    var startingCapitalStr by remember { mutableStateOf("50000") }

    val targetIncome = targetMonthlyIncomeStr.toFloatOrNull() ?: 0f
    val currentIncome = currentMonthlyIncomeStr.toFloatOrNull() ?: 0f
    val savingsRate = (monthlySavingsRatePercentStr.toFloatOrNull() ?: 0f).coerceIn(0f, 100f)
    val startingCapital = startingCapitalStr.toFloatOrNull() ?: 0f

    // Defensive monthly calculations
    val monthlySavings = if (currentIncome > 0f) currentIncome * (savingsRate / 100f) else 0f
    val incomeGap = (targetIncome - currentIncome).coerceAtLeast(0f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("wealth_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("wealth_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Wealth & Financial Architecture",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Income acceleration, capital preservation, and asset engines",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Regulatory & Safety Disclaimer
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PriorityP1.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, PriorityP1.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = PriorityP1,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "RISK MANAGEMENT & EDUCATIONAL NOTICE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = PriorityP1
                    )
                    Text(
                        text = "This mode is strictly for goal planning and educational modeling. It does not provide certified financial, tax, or investment advice. High-risk speculation and trading without capital preservation are strongly discouraged.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Input & Engine Calibration Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "FINANCIAL TARGET CALIBRATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = AmberGoldPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentMonthlyIncomeStr,
                        onValueChange = { currentMonthlyIncomeStr = it },
                        label = { Text("Current Monthly Income") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetMonthlyIncomeStr,
                        onValueChange = { targetMonthlyIncomeStr = it },
                        label = { Text("Target Income") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = monthlySavingsRatePercentStr,
                        onValueChange = { monthlySavingsRatePercentStr = it },
                        label = { Text("Target Savings Rate %") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = startingCapitalStr,
                        onValueChange = { startingCapitalStr = it },
                        label = { Text("Base Capital") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }
        }

        // Three Core Engines Breakdown
        Text(
            text = "THE THREE ACCELERATION ENGINES",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp),
            color = SkySecondary
        )

        // Engine 1: Skill & Career Engine
        EngineCard(
            title = "1. Skill & High-Income Career Engine",
            tag = "PRIMARY CASH FLOW",
            tagColor = EmeraldTertiary,
            metric = "Income Gap: ₹${incomeGap.toInt()} / month [ESTIMATE]",
            description = "High-income skills (Systems Engineering, AI Architecture, Product Management) create asymmetrical cash flow without financial risk.",
            actionAdvice = "Dedicate 80% of execution time to mastering top-tier technical craft."
        )

        // Engine 2: Commercial Business Engine
        EngineCard(
            title = "2. Commercial Product Engine",
            tag = "EQUITY & VALUE CREATION",
            tagColor = AmberGoldPrimary,
            metric = "Initial Budget Target: ₹${startingCapital.toInt()} [FACT]",
            description = "Micro-SaaS, specialized consulting, or validated digital products. Reinvest cash flow from the Skill Engine into verifiable customer problems.",
            actionAdvice = "Validate with 5 paying clients before investing substantial capital."
        )

        // Engine 3: Asset Building & Capital Preservation
        EngineCard(
            title = "3. Capital Preservation Engine",
            tag = "COMPOUNDING & DEFENSE",
            tagColor = SkySecondary,
            metric = "Monthly Retained Capital: ₹${monthlySavings.toInt()} [ESTIMATE]",
            description = "Index funds, low-cost debt retirement, emergency runway (6 months of basic expenses). Paper trading for learning, never live capital on untested speculation.",
            actionAdvice = "Lock 6 months emergency liquidity before initiating discretionary investments."
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun EngineCard(
    title: String,
    tag: String,
    tagColor: Color,
    metric: String,
    description: String,
    actionAdvice: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tagColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = tag,
                        color = tagColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = metric,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = AmberGoldPrimary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = EmeraldTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = actionAdvice,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = EmeraldTertiary
                )
            }
        }
    }
}
