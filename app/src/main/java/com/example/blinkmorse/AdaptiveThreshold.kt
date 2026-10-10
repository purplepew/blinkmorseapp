package com.example.blinkmorse

class AdaptiveThreshold(
    private val windowSize: Int = WINDOW_SIZE,
    private val minimumSeparationMs: Float = MINIMUM_SEPARATION_MS,
    private val adaptationMargin: Float = ADAPTATION_MARGIN,
    private val weightedBoundary: Float = WEIGHTED_BOUNDARY
) {
    companion object {
        const val WINDOW_SIZE = 10
        const val MINIMUM_SEPARATION_MS = 50.0f
        const val ADAPTATION_MARGIN = 0.20f
        const val WEIGHTED_BOUNDARY = 0.40f
    }

    private val shortSamples = mutableListOf<Float>()
    private val longSamples = mutableListOf<Float>()

    var shortCenter: Float = 0f
        private set
    var longCenter: Float = 0f
        private set
    var threshold: Float = 0f
        private set

    fun seedShortSample(duration: Float) {
        shortSamples.add(duration)
        if (shortSamples.size > windowSize) {
            shortSamples.removeAt(0)
        }
        update()
    }

    fun seedLongSample(duration: Float) {
        longSamples.add(duration)
        if (longSamples.size > windowSize) {
            longSamples.removeAt(0)
        }
        update()
    }

    fun classifyAndAdapt(durationMs: Long): String {
        val duration = durationMs.toFloat()

        // 1. Classification based on current threshold
        val symbol = if (duration < threshold) "." else "-"

        // 2. Gatekeeping check for adaptation using 20% margin
        val separation = longCenter - shortCenter
        val margin = separation * adaptationMargin
        val lowerBoundary = threshold - margin
        val upperBoundary = threshold + margin

        if (duration < lowerBoundary) {
            seedShortSample(duration)
        } else if (duration > upperBoundary) {
            seedLongSample(duration)
        }
        // If duration falls inside [lowerBoundary, upperBoundary],
        // we do NOT adapt either sample list to prevent error reinforcement.

        return symbol
    }

    private fun update() {
        if (shortSamples.isEmpty() || longSamples.isEmpty()) return

        val newShortCenter = calculateMedian(shortSamples)
        val newLongCenter = calculateMedian(longSamples)
        val separation = newLongCenter - newShortCenter

        if (separation < minimumSeparationMs) return

        shortCenter = newShortCenter
        longCenter = newLongCenter
        threshold = shortCenter + (separation * weightedBoundary)
    }

    private fun calculateMedian(list: List<Float>): Float {
        if (list.isEmpty()) return 0f
        val sorted = list.sorted()
        val size = sorted.size
        return if (size % 2 == 1) {
            sorted[size / 2]
        } else {
            (sorted[size / 2 - 1] + sorted[size / 2]) / 2.0f
        }
    }

    fun reset() {
        shortSamples.clear()
        longSamples.clear()
        shortCenter = 0f
        longCenter = 0f
        threshold = 0f
    }
}
