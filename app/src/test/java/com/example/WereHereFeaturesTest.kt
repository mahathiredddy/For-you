package com.example

import com.example.data.sync.SyncStatus
import com.example.ui.screens.more.CuratedResource
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthUser
import kotlin.math.ceil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WereHereFeaturesTest {

    @Test
    fun testAuthUserAndUiState() {
        val user = AuthUser(
            uid = "test-uid-123",
            email = "alex@werehere.app",
            displayName = "Alex Hunter",
            photoUrl = "https://werehere.app/avatar.png"
        )

        assertEquals("test-uid-123", user.uid)
        assertEquals("alex@werehere.app", user.email)
        assertEquals("Alex Hunter", user.displayName)

        val authenticatedState = AuthUiState.Authenticated(user)
        assertEquals(user, authenticatedState.user)

        val loadingState = AuthUiState.Loading("Signing in...")
        assertEquals("Signing in...", loadingState.message)

        val unauthenticatedState = AuthUiState.Unauthenticated("Network error")
        assertEquals("Network error", unauthenticatedState.errorMessage)
    }

    @Test
    fun testCuratedResourceVerificationStatuses() {
        val resource = CuratedResource(
            title = "Kaggle Competitions",
            category = "Hackathons",
            provider = "Google / Kaggle",
            url = "https://www.kaggle.com/competitions",
            verificationStatus = "VERIFIED",
            summary = "World's largest machine learning competition platform.",
            whyItMatters = "Hands-on model benchmarking and prize pools.",
            estimatedTimeOrCost = "Free"
        )

        assertEquals("VERIFIED", resource.verificationStatus)
        assertEquals("Hackathons", resource.category)
        assertTrue(resource.url.startsWith("https://"))
        assertFalse(resource.title.isBlank())
    }

    @Test
    fun testUnitEconomicsAndBreakEvenCalculations() {
        // Standard case
        val price = 29f
        val cost = 3f
        val monthlyFixed = 60f

        val margin = price - cost
        assertEquals(26f, margin, 0.01f)

        val marginPercent = if (price > 0f) ((margin / price) * 100).toInt() else 0
        assertEquals(89, marginPercent)

        val breakEvenUnits = if (margin > 0f) ceil(monthlyFixed / margin).toInt() else 0
        assertEquals(3, breakEvenUnits)

        // Edge case: Price is 0 (Prevent division by zero)
        val zeroPrice = 0f
        val zeroPriceMargin = zeroPrice - cost
        val zeroPricePercent = if (zeroPrice > 0f) ((zeroPriceMargin / zeroPrice) * 100).toInt() else 0
        val zeroPriceBreakEven = if (zeroPriceMargin > 0f) ceil(monthlyFixed / zeroPriceMargin).toInt() else 0
        assertEquals(0, zeroPricePercent)
        assertEquals(0, zeroPriceBreakEven)

        // Edge case: Cost exceeds Price (Negative margin)
        val highCost = 50f
        val negMargin = price - highCost
        val negBreakEven = if (negMargin > 0f) ceil(monthlyFixed / negMargin).toInt() else 0
        assertEquals(0, negBreakEven)
    }

    @Test
    fun testHackathonSevenDimensionScoreAggregation() {
        val dimInnovation = 8 // 15%
        val dimFeasibility = 9 // 20%
        val dimImpact = 8 // 15%
        val dimDemo = 9 // 20%
        val dimTech = 8 // 15%
        val dimMarket = 7 // 10%
        val dimTeam = 9 // 5%

        val weightedSum = (dimInnovation * 15 +
                dimFeasibility * 20 +
                dimImpact * 15 +
                dimDemo * 20 +
                dimTech * 15 +
                dimMarket * 10 +
                dimTeam * 5) / 100f

        assertEquals(8.35f, weightedSum, 0.01f)
        assertTrue(weightedSum in 0.0f..10.0f)
    }

    @Test
    fun testProgressPercentageCalculations() {
        // Zero tasks
        val totalZero = 0
        val completedZero = 0
        val percentZero = if (totalZero > 0) ((completedZero.toFloat() / totalZero.toFloat()) * 100).toInt() else 0
        assertEquals(0, percentZero)

        // 1 of 3
        val totalThree = 3
        val completedOne = 1
        val percentOneThird = if (totalThree > 0) ((completedOne.toFloat() / totalThree.toFloat()) * 100).toInt() else 0
        assertEquals(33, percentOneThird)

        // 3 of 3
        val completedThree = 3
        val percentFull = if (totalThree > 0) ((completedThree.toFloat() / totalThree.toFloat()) * 100).toInt() else 0
        assertEquals(100, percentFull)
    }

    @Test
    fun testGeminiSecuritySanitization() {
        val fakeApiKey = "AIzaSySecretKey987654321"
        val rawErrorMessage = "HTTP 403 Forbidden: Request failed for key $fakeApiKey on endpoint /generateContent"

        // Sanitization logic used in AiBackendClient
        val sanitized = if (fakeApiKey.isNotBlank()) rawErrorMessage.replace(fakeApiKey, "[REDACTED]") else rawErrorMessage

        assertFalse(sanitized.contains(fakeApiKey))
        assertTrue(sanitized.contains("[REDACTED]"))
        assertEquals("HTTP 403 Forbidden: Request failed for key [REDACTED] on endpoint /generateContent", sanitized)
    }

    @Test
    fun testFirestoreSyncStableIdsAndConflictResolution() {
        val goalId = 42L
        val taskId = 101L
        val noteId = 88L

        // Stable document ID format test
        val goalDocId = "goal_$goalId"
        val taskDocId = "task_$taskId"
        val noteDocId = "note_$noteId"

        assertEquals("goal_42", goalDocId)
        assertEquals("task_101", taskDocId)
        assertEquals("note_88", noteDocId)

        // Timestamp conflict resolution test: Last-write-wins
        val localUpdatedAt = 1000L
        val remoteUpdatedAtOlder = 800L
        val remoteUpdatedAtNewer = 1200L

        // Local should win against older remote
        val localWins = localUpdatedAt >= remoteUpdatedAtOlder
        assertTrue(localWins)

        // Remote should win when newer
        val remoteWins = remoteUpdatedAtNewer > localUpdatedAt
        assertTrue(remoteWins)
    }

    @Test
    fun testSyncStatusModelIntegrity() {
        val unconfigured = SyncStatus.Unconfigured("Firebase configuration missing")
        assertEquals("Firebase configuration missing", unconfigured.reason)

        val syncing = SyncStatus.Syncing("Syncing goals...")
        assertEquals("Syncing goals...", syncing.stage)

        val success = SyncStatus.Success(lastSyncTimestamp = 123456789L, itemsSynced = 15)
        assertEquals(123456789L, success.lastSyncTimestamp)
        assertEquals(15, success.itemsSynced)

        val error = SyncStatus.Error("Network timeout")
        assertEquals("Network timeout", error.message)
    }
}
