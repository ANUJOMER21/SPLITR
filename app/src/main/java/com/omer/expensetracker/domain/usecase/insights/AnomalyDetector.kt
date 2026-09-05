package com.omer.expensetracker.domain.usecase.insights

/** An expense is "significantly above" its category's recent average once it's more than
 * [THRESHOLD_MULTIPLIER]x that average. A category needs at least [MIN_SAMPLE_SIZE] prior
 * expenses before an average is trusted enough to flag anything against it. */
object AnomalyDetector {
    const val THRESHOLD_MULTIPLIER = 2.0
    const val MIN_SAMPLE_SIZE = 3

    fun isAnomaly(amountMinor: Long, categoryAverageMinor: Long, sampleSize: Int): Boolean {
        if (sampleSize < MIN_SAMPLE_SIZE || categoryAverageMinor <= 0L) return false
        return amountMinor > categoryAverageMinor * THRESHOLD_MULTIPLIER
    }
}
