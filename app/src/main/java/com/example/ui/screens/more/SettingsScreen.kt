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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.ai.AiConnectionMode
import com.example.data.ai.AiEngineStatus
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.PriorityP0
import com.example.ui.theme.SkySecondary

@Composable
fun SettingsScreen(
    userProfile: UserProfileEntity?,
    engineStatus: AiEngineStatus,
    customBackendUrl: String = "",
    devApiKey: String = "",
    isTestingConnection: Boolean = false,
    onSaveCustomBackendUrl: (String) -> Unit = {},
    onSaveDevApiKey: (String) -> Unit = {},
    onTestConnection: ((Boolean) -> Unit) -> Unit = {},
    onSaveProfile: (UserProfileEntity) -> Unit,
    onClearChat: () -> Unit,
    onEmergencyReset: () -> Unit = {},
    onLoadDemoMode: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onClearAllData: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var location by remember { mutableStateOf(userProfile?.location ?: "") }
    var education by remember { mutableStateOf(userProfile?.educationField ?: "") }
    var currentSkills by remember { mutableStateOf(userProfile?.currentSkills ?: "") }
    var hoursPerDay by remember { mutableFloatStateOf(userProfile?.hoursPerDay ?: 2.5f) }
    var constraints by remember { mutableStateOf(userProfile?.biggestConstraints ?: "") }
    var savedMessage by remember { mutableStateOf(false) }

    var inputBackendUrl by remember(customBackendUrl) { mutableStateOf(customBackendUrl) }
    var inputDevApiKey by remember(devApiKey) { mutableStateOf(devApiKey) }
    var testResultFeedback by remember { mutableStateOf<String?>(null) }
    var keySavedNotice by remember { mutableStateOf(false) }
    var urlSavedNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("settings_screen"),
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
                    text = "Settings & AI Configuration",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Intelligence credentials, backend endpoint & profile calibration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // AI Engine Status & Diagnostics Card
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
                    Text(
                        text = "INTELLIGENCE ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                        color = AmberGoldPrimary
                    )
                    Surface(
                        color = when {
                            engineStatus.isGeminiLive -> EmeraldTertiary.copy(alpha = 0.2f)
                            engineStatus.customEndpointUrl?.isNotBlank() == true -> AmberGoldPrimary.copy(alpha = 0.2f)
                            else -> SkySecondary.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when {
                                engineStatus.isGeminiLive -> "LIVE VERIFIED"
                                engineStatus.customEndpointUrl?.isNotBlank() == true -> "CUSTOM ENDPOINT"
                                else -> "SMART OFFLINE"
                            },
                            color = when {
                                engineStatus.isGeminiLive -> EmeraldTertiary
                                engineStatus.customEndpointUrl?.isNotBlank() == true -> AmberGoldPrimary
                                else -> SkySecondary
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Active Engine: ${engineStatus.providerName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = engineStatus.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Test Connection Button & Result
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            testResultFeedback = null
                            onTestConnection { success ->
                                testResultFeedback = if (success) {
                                    "Success: Connection to live Gemini verified!"
                                } else {
                                    "Connection failed: Operating in offline fallback mode."
                                }
                            }
                        },
                        enabled = !isTestingConnection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("test_ai_connection_button")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pinging...", style = MaterialTheme.typography.labelSmall)
                        } else {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp), tint = AmberGoldPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Connection", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (engineStatus.lastTestedTime != null) {
                        Text(
                            text = "Last verified: Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldTertiary
                        )
                    }
                }

                if (testResultFeedback != null) {
                    val isSuccess = testResultFeedback?.startsWith("Success") == true
                    Surface(
                        color = if (isSuccess) EmeraldTertiary.copy(alpha = 0.15f) else PriorityP0.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (isSuccess) EmeraldTertiary else PriorityP0,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = testResultFeedback ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isSuccess) EmeraldTertiary else PriorityP0
                            )
                        }
                    }
                }
            }
        }

        // Live AI Configuration Card (Backend Endpoint & Dev Key)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "SECURE INTELLIGENCE CONFIGURATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = SkySecondary
                )

                Text(
                    text = "Configure a secure proxy endpoint (production) or test development key. If unconfigured, the app runs completely offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Custom Backend Endpoint
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Configurable Backend Endpoint (Cloud Function / Server):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputBackendUrl,
                            onValueChange = {
                                inputBackendUrl = it
                                urlSavedNotice = false
                            },
                            placeholder = { Text("https://my-backend.run.app/ai/gemini") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                onSaveCustomBackendUrl(inputBackendUrl.trim())
                                urlSavedNotice = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A))
                        ) {
                            Text(if (urlSavedNotice) "Saved" else "Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Development API Key Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Development Gemini API Key (Optional):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputDevApiKey,
                            onValueChange = {
                                inputDevApiKey = it
                                keySavedNotice = false
                            },
                            placeholder = { Text("Enter AI Studio API key...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                onSaveDevApiKey(inputDevApiKey.trim())
                                keySavedNotice = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A))
                        ) {
                            Text(if (keySavedNotice) "Saved" else "Save", fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Note: In AI Studio, keys set in the Secrets panel ('GEMINI_API_KEY') are automatically accessed at runtime.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Profile Editing Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "PROFILE CAPACITY & CONSTRAINTS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = SkySecondary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = education,
                    onValueChange = { education = it },
                    label = { Text("Education & Domain") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = currentSkills,
                    onValueChange = { currentSkills = it },
                    label = { Text("Current Skills") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Daily Time Commitment:", style = MaterialTheme.typography.bodySmall)
                    Text(text = "${hoursPerDay}h / day", fontWeight = FontWeight.Bold, color = AmberGoldPrimary)
                }
                androidx.compose.material3.Slider(
                    value = hoursPerDay,
                    onValueChange = { hoursPerDay = it },
                    valueRange = 0.5f..8f,
                    steps = 15
                )

                OutlinedTextField(
                    value = constraints,
                    onValueChange = { constraints = it },
                    label = { Text("Current Constraints") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        val updated = userProfile?.copy(
                            name = name,
                            location = location,
                            educationField = education,
                            currentSkills = currentSkills,
                            hoursPerDay = hoursPerDay,
                            biggestConstraints = constraints
                        ) ?: UserProfileEntity(
                            name = name,
                            location = location,
                            educationField = education,
                            currentSkills = currentSkills,
                            hoursPerDay = hoursPerDay,
                            biggestConstraints = constraints,
                            isOnboardingCompleted = true
                        )
                        onSaveProfile(updated)
                        savedMessage = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (savedMessage) "Profile Saved!" else "Save Profile Changes", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Account & Cloud Authentication Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ACCOUNT & CLOUD SYNCHRONIZATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = SkySecondary
                )
                Text(
                    text = "Sign in with Google or Email to link your workspace, sync progress, and enable cloud backup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onNavigateToAuth,
                    colors = ButtonDefaults.buttonColors(containerColor = SkySecondary, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Account & Cloud Sync", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Emergency Reset Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, PriorityP0.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "EMERGENCY RESET: RESET MY PLAN",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = PriorityP0
                )
                Text(
                    text = "Philosophy: When everything feels overwhelming, make the next step smaller. Automatically pares down today's workload to 3 small focus tasks totaling ~65 minutes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onEmergencyReset,
                    colors = ButtonDefaults.buttonColors(containerColor = PriorityP0.copy(alpha = 0.15f), contentColor = PriorityP0),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Activate Emergency Plan Reset", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Canonical Demo Mode Experience
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, AmberGoldPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "CANONICAL DEMO EXPERIENCE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = AmberGoldPrimary
                )
                Text(
                    text = "Instantly load the canonical demo journey for Alex (19, CS Student) aiming to Win a National Hackathon in 90 Days with pre-populated roadmap, tasks, and intelligence.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onLoadDemoMode,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGoldPrimary, contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Load Alex's 90-Day Hackathon Demo", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Privacy & Future Cloud Integrations Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DATA RESET & CLEAR HISTORY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Local-first architecture keeps your data on this device. You can clear chat memory or wipe local data if needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onClearChat,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = PriorityP0
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear AI Coach Conversation History")
                }

                Button(
                    onClick = onClearAllData,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset Journey (Restart Onboarding)")
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
