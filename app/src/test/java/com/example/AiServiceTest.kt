package com.example

import com.example.data.ai.AIResponse
import com.example.data.ai.GeminiAiService
import com.example.data.ai.SmartRuleBasedAiService
import com.example.data.local.entity.DailyCheckInEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.runBlocking
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
class AiServiceTest {

    @Test
    fun testParseGoalAnalysisValidJson() {
        val service = GeminiAiService()
        val json = """
            {
              "intendedOutcome": "Build and release MVP Android app",
              "startingPointSummary": "Intermediate Kotlin proficiency",
              "missingSkillsAndResources": ["Advanced Compose Navigation", "CI/CD setup"],
              "estimatedWorkHours": 85,
              "feasibilityRating": "Realistic",
              "feasibilityReasoning": "14h/week allocation over 6 weeks provides sufficient margin.",
              "assumptionsAndRisks": ["API quota limitations"],
              "recommendedMilestones": ["Foundations complete", "Feature complete", "Release to Play"],
              "highestValueFirstAction": "Initialize database entities and repository",
              "firstActionWhy": "Establishes data model contract before building UI.",
              "firstActionDurationMinutes": 45
            }
        """.trimIndent()

        val parsed = service.parseGoalAnalysis(json)
        assertNotNull(parsed)
        assertTrue(parsed!!.isValid())
        assertEquals("Build and release MVP Android app", parsed.intendedOutcome)
        assertEquals("Realistic", parsed.feasibilityRating)
        assertEquals(85, parsed.estimatedWorkHours)
        assertEquals(2, parsed.missingSkillsAndResources.size)
    }

    @Test
    fun testParseRoadmapPlanValidJson() {
        val service = GeminiAiService()
        val json = """
            {
              "totalEstimatedWeeks": 6,
              "phases": [
                {
                  "phaseIndex": 1,
                  "name": "Phase 1: Foundations",
                  "purpose": "Establish architecture and data layer",
                  "estimatedWeeks": 2,
                  "requiredSkills": ["Kotlin", "Room"],
                  "completionCriteria": "Working database and entities",
                  "milestones": [
                    {
                      "title": "M1.1: Core Database",
                      "description": "Room entities and DAOs",
                      "targetWeeksFromStart": 1,
                      "tasks": [
                        {
                          "title": "Define Room entities",
                          "description": "Data models with primary keys",
                          "instructions": "Implement entity data classes",
                          "priority": "P0",
                          "estimatedMinutes": 45,
                          "category": "Storage",
                          "prerequisites": []
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val parsed = service.parseRoadmapPlan(json)
        assertNotNull(parsed)
        assertTrue(parsed!!.isValid())
        assertEquals(6, parsed.totalEstimatedWeeks)
        assertEquals(1, parsed.phases.size)
        assertEquals("Phase 1: Foundations", parsed.phases[0].name)
        assertEquals(1, parsed.phases[0].milestones.size)
        assertEquals(1, parsed.phases[0].milestones[0].tasks.size)
    }

    @Test
    fun testParseAdaptationResultValidJson() {
        val service = GeminiAiService()
        val json = """
            {
              "strategySummary": "Pacing adjusted after reported friction.",
              "adjustmentReason": "Recent check-in reported blocker on API setup.",
              "recommendedAction": "Isolate API mock and verify credentials.",
              "suggestedTaskSizeAdjustment": "Downsize to 25m focus sprint",
              "reasonForChange": "Restore forward velocity before building next feature.",
              "newPrerequisiteTask": {
                "title": "Isolate API key configuration",
                "description": "Verify environment credentials",
                "instructions": "Check BuildConfig and secrets panel",
                "priority": "P0",
                "estimatedMinutes": 20,
                "category": "Unblocking"
              }
            }
        """.trimIndent()

        val parsed = service.parseAdaptationResult(json)
        assertNotNull(parsed)
        assertEquals("Pacing adjusted after reported friction.", parsed!!.strategySummary)
        assertNotNull(parsed.newPrerequisiteTask)
        assertEquals("Isolate API key configuration", parsed.newPrerequisiteTask!!.title)
    }

    @Test
    fun testSmartRuleBasedFallbackGoalAnalysis() = runBlocking {
        val service = SmartRuleBasedAiService()
        val goal = GoalEntity(
            title = "Launch Mobile Startup",
            description = "Build a cross-border remittance app",
            category = "Business",
            desiredOutcome = "First 100 paying customers",
            deadline = "3 months",
            hoursPerWeek = 20f
        )
        val profile = UserProfileEntity(
            name = "Alex",
            hoursPerDay = 3f,
            currentSkills = "Product design, sales"
        )

        val analysis = service.generateInternalGoalAnalysis(goal, profile)
        assertNotNull(analysis)
        assertTrue(analysis.isValid())
        assertFalse(analysis.feasibilityRating.isBlank())
        assertTrue(analysis.estimatedWorkHours > 0)
        assertFalse(analysis.highestValueFirstAction.isBlank())
    }

    @Test
    fun testDetermineNextMoveWithBlocker() {
        val service = SmartRuleBasedAiService()
        val goal = GoalEntity(
            title = "Master Jetpack Compose",
            description = "Complete complex UI library",
            category = "Programming"
        )
        val pendingTask = TaskEntity(
            id = 42L,
            goalId = goal.id,
            title = "Build Custom Canvas Layout",
            priority = "P0",
            estimatedMinutes = 60,
            status = "PENDING"
        )
        val checkIn = DailyCheckInEntity(
            goalId = goal.id,
            date = "2026-10-10",
            rating = "Struggled",
            actualHoursInvested = 1.5f,
            blockers = "Layout measurement loop crash"
        )

        val rec = service.determineNextMove(
            goal = goal,
            pendingTasks = listOf(pendingTask),
            completedTasks = emptyList(),
            checkIns = listOf(checkIn),
            memories = emptyList()
        )

        assertNotNull(rec)
        assertTrue(rec.actionTitle.contains("Isolate Blocker") || rec.actionTitle.contains("Layout measurement"))
        assertEquals("P0", rec.priority)
        assertTrue(rec.estimatedMinutes <= 25)
    }
}
