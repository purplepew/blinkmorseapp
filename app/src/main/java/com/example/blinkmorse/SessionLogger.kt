package com.example.blinkmorse

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter

data class BlinkLogEntry(
    val sessionId: String,
    val blinkIndex: Int,
    val timestampMs: Long,
    val durationMs: Long,
    val shortCenterMs: Float,
    val longCenterMs: Float,
    val currentThresholdMs: Float,
    val predictedSymbol: String,
    val adaptationResult: String,
    var decodedChar: String = ""
)

class SessionLogger {
    private val logs = mutableListOf<BlinkLogEntry>()
    private var sessionId: String = System.currentTimeMillis().toString()
    private var blinkIndex = 0

    fun startNewSession() {
        logs.clear()
        sessionId = System.currentTimeMillis().toString()
        blinkIndex = 0
    }

    fun logBlink(
        durationMs: Long, shortCenter: Float, longCenter: Float,
        threshold: Float, symbol: String, adaptation: String
    ) {
        logs.add(BlinkLogEntry(
            sessionId, blinkIndex++, System.currentTimeMillis(),
            durationMs, shortCenter, longCenter, threshold, symbol, adaptation, ""
        ))
    }

    fun attachDecodedChar(char: String) {
        if (logs.isNotEmpty()) {
            // Attach the decoded character to the most recent blink row
            logs.last().decodedChar = char
        }
    }

    fun exportSession(context: Context): String? {
        if (logs.isEmpty()) return null

        var index = 1
        var file = File(context.filesDir, "Testing_data$index.csv")
        while (file.exists()) {
            index++
            file = File(context.filesDir, "Testing_data$index.csv")
        }

        FileWriter(file).use { writer ->
            writer.append("session_id,blink_index,timestamp_ms,duration_ms,short_center_ms,long_center_ms,current_threshold_ms,predicted_symbol,adaptation_result,decoded_char\n")
            for (log in logs) {
                writer.append("${log.sessionId},${log.blinkIndex},${log.timestampMs},${log.durationMs},${log.shortCenterMs},${log.longCenterMs},${log.currentThresholdMs},${log.predictedSymbol},${log.adaptationResult},${log.decodedChar}\n")
            }
        }

        return file.name
    }
}
