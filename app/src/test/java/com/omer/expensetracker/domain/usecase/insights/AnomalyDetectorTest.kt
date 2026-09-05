package com.omer.expensetracker.domain.usecase.insights

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnomalyDetectorTest {

    @Test
    fun `not an anomaly when below the threshold multiplier`() {
        assertFalse(AnomalyDetector.isAnomaly(amountMinor = 1900L, categoryAverageMinor = 1000L, sampleSize = 5))
    }

    @Test
    fun `is an anomaly once strictly above the threshold multiplier`() {
        assertTrue(AnomalyDetector.isAnomaly(amountMinor = 2100L, categoryAverageMinor = 1000L, sampleSize = 5))
    }

    @Test
    fun `exactly at the threshold multiplier is not flagged`() {
        assertFalse(AnomalyDetector.isAnomaly(amountMinor = 2000L, categoryAverageMinor = 1000L, sampleSize = 5))
    }

    @Test
    fun `too few prior samples never flags regardless of amount`() {
        assertFalse(AnomalyDetector.isAnomaly(amountMinor = 100_000L, categoryAverageMinor = 100L, sampleSize = 1))
        assertFalse(AnomalyDetector.isAnomaly(amountMinor = 100_000L, categoryAverageMinor = 100L, sampleSize = AnomalyDetector.MIN_SAMPLE_SIZE - 1))
    }

    @Test
    fun `zero average never flags`() {
        assertFalse(AnomalyDetector.isAnomaly(amountMinor = 5000L, categoryAverageMinor = 0L, sampleSize = 10))
    }
}
