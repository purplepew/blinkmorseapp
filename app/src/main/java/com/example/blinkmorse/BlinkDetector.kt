package com.example.blinkmorse

import android.os.SystemClock

class BlinkDetector(
    private val threshold: Float = STATIC_BLINK_THRESHOLD,
    private val minBlinkDurationMs: Long = MIN_BLINK_DURATION_MS,
    private val maxBlinkDurationMs: Long = MAX_BLINK_DURATION_MS
) {
    companion object {
        const val STATIC_BLINK_THRESHOLD = 0.22f
        const val MIN_BLINK_DURATION_MS = 80L
        const val MAX_BLINK_DURATION_MS = 2000L
    }

    data class BlinkResult(
        val isClosed: Boolean,
        val blinkStarted: Boolean,
        val blinkEnded: Boolean,
        val durationMs: Long?
    )

    var isClosed: Boolean = false
        private set

    private var blinkStartTimeMs: Long? = null
    var lastBlinkDurationMs: Long? = null
        private set

    fun update(
        ear: Float,
        timestampMs: Long = SystemClock.elapsedRealtime()
    ): BlinkResult {
        var blinkStarted = false
        var blinkEnded = false
        var durationMs: Long? = null

        if (ear < threshold) {
            if (!isClosed) {
                isClosed = true
                blinkStartTimeMs = timestampMs
                blinkStarted = true
            }
        } else {
            if (isClosed) {
                isClosed = false
                val startTime = blinkStartTimeMs
                if (startTime != null) {
                    val duration = timestampMs - startTime
                    if (duration in minBlinkDurationMs..maxBlinkDurationMs) {
                        lastBlinkDurationMs = duration
                        durationMs = duration
                        blinkEnded = true
                    }
                }
                blinkStartTimeMs = null
            }
        }

        return BlinkResult(
            isClosed = isClosed,
            blinkStarted = blinkStarted,
            blinkEnded = blinkEnded,
            durationMs = durationMs
        )
    }

    fun reset() {
        isClosed = false
        blinkStartTimeMs = null
        lastBlinkDurationMs = null
    }
}
