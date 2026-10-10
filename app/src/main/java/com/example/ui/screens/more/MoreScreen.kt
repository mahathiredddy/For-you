package com.example.ui.screens.more

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.PriorityP0
import com.example.ui.theme.PriorityP1
import com.example.ui.theme.PriorityP2
import com.example.ui.theme.SkySecondary

@Composable
fun MoreScreen(
    onNavigateToDestination: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("more_screen_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Specialist Hub",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Specialized intelligence, memory, notes, and strategic planners",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section: Knowledge & Notes
        Text(
            text = "KNOWLEDGE & EXECUTION",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp),
            color = SkySecondary
        )

        MoreHubItem(
            title = "Learning Hub",
            description = "Track skill competencies (1-5), target levels, practice hours, and projects.",
            icon = Icons.Default.School,
            iconTint = SkySecondary,
            onClick = { onNavigateToDestination(Screen.LearningHub) }
        )

        MoreHubItem(
            title = "Notes",
            description = "Capture research notes, pin important guides, and convert notes into tasks.",
            icon = Icons.Default.Description,
            iconTint = AmberGoldPrimary,
            onClick = { onNavigateToDestination(Screen.Notes) }
        )

        MoreHubItem(
            title = "Progress & Weekly Reviews",
            description = "Comprehensive retrospectives, consistency streaks, and weekly calibration.",
            icon = Icons.Default.Timeline,
            iconTint = EmeraldTertiary,
            onClick = { onNavigateToDestination(Screen.Progress) }
        )

        // Section: Specialist Modules
        Text(
            text = "SPECIALIST INTELLIGENCE",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp),
            color = AmberGoldPrimary
        )

        MoreHubItem(
            title = "Hackathon Intelligence",
            description = "7-dimension idea scorecard, 48h MVP scoping, and pitch checklist.",
            icon = Icons.Default.Code,
            iconTint = PriorityP0,
            onClick = { onNavigateToDestination(Screen.Hackathon) }
        )

        MoreHubItem(
            title = "Business Intelligence",
            description = "Customer validation, unit economics, break-even analysis, and 30-day plan.",
            icon = Icons.Default.Business,
            iconTint = PriorityP1,
            onClick = { onNavigateToDestination(Screen.Business) }
        )

        MoreHubItem(
            title = "Resource Discovery Engine",
            description = "Curated hackathons, courses, tools, grants, and datasets with verification status.",
            icon = androidx.compose.material.icons.Icons.Default.AutoAwesome,
            iconTint = EmeraldTertiary,
            onClick = { onNavigateToDestination(Screen.Explore) }
        )

        // Section: System & Preferences
        Text(
            text = "ACCOUNT, MEMORY & PLATFORM",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        MoreHubItem(
            title = "Account & Cloud Sync",
            description = "Google Sign-In, Firebase session persistence, and cloud synchronization.",
            icon = androidx.compose.material.icons.Icons.Default.Settings,
            iconTint = SkySecondary,
            onClick = { onNavigateToDestination(Screen.Auth) }
        )

        MoreHubItem(
            title = "Personal Memory",
            description = "Inspect local database memories, lessons learned, and AI adaptation logs.",
            icon = Icons.Default.Memory,
            iconTint = PriorityP2,
            onClick = { onNavigateToDestination(Screen.Memory) }
        )

        MoreHubItem(
            title = "Settings & AI Configuration",
            description = "Profile editor, emergency reset, demo mode loader, Gemini API status.",
            icon = Icons.Default.Settings,
            iconTint = MaterialTheme.colorScheme.onSurface,
            onClick = { onNavigateToDestination(Screen.Settings) }
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun MoreHubItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
