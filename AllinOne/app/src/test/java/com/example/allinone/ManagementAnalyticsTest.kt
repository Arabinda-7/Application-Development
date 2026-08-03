package com.example.allinone

import org.junit.Test
import org.junit.Assert.*
import com.example.allinone.core.utils.IntelligenceEngine

class ManagementAnalyticsTest {

    @Test
    fun calculateTaskVelocity_perfectExecution_returnsCorrectValue() {
        val now = System.currentTimeMillis()
        val timestamps = listOf(
            now, 
            now - (1 * 24 * 60 * 60 * 1000L),
            now - (2 * 24 * 60 * 60 * 1000L)
        )
        // 3 tasks in 14 days
        val velocity = IntelligenceEngine.calculateTaskVelocity(timestamps, 14)
        assertEquals(3.0 / 14.0, velocity, 0.001)
    }

    @Test
    fun calculatePriorityScore_highPriorityUrgent_scoresHigh() {
        // Priority 2 (High), Age 1 day, Deadline in 1 day, 5 subtasks
        val score = IntelligenceEngine.calculatePriorityScore(2, 1, 1, 5)
        
        // Priority: (2+1)*20 = 60
        // Urgency: (14-1)*5 = 65
        // Complexity: 5*5 = 25 (capped at 20)
        // Total: 60 + 65 + 20 = 145
        assertEquals(145.0, score, 0.1)
    }

    @Test
    fun calculatePriorityScore_lowPriorityNoDeadline_scoresLow() {
        // Priority 0 (Low), Age 1 day, No deadline, 0 subtasks
        val score = IntelligenceEngine.calculatePriorityScore(0, 1, null, 0)
        
        // Priority: (0+1)*20 = 20
        // Urgency: 1 (capped at 20)
        // Complexity: 0
        // Total: 21.0
        assertEquals(21.0, score, 0.1)
    }
}
