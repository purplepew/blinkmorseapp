package com.example.blinkmorse

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BlinkLogEntry(
    val timestampMs: Long,
    val durationMs: Long,
    val shortCenter: Float,
    val longCenter: Float,
    val threshold: Float,
    val symbol: String,
    val adaptation: String,
    var decodedChar: String = ""
)

class SessionLogger {
    private val logs = mutableListOf<BlinkLogEntry>()
    private var sessionStartTimeMs: Long = 0

    fun startNewSession() {
        logs.clear()
        sessionStartTimeMs = System.currentTimeMillis()
    }

    fun logBlink(
        durationMs: Long,
        shortCenter: Float,
        longCenter: Float,
        threshold: Float,
        symbol: String,
        adaptation: String
    ) {
        val entry = BlinkLogEntry(
            timestampMs = System.currentTimeMillis(),
            durationMs = durationMs,
            shortCenter = shortCenter,
            longCenter = longCenter,
            threshold = threshold,
            symbol = symbol,
            adaptation = adaptation
        )
        logs.add(entry)
    }

    fun attachDecodedChar(decodedChar: String) {
        if (logs.isNotEmpty()) {
            logs.last().decodedChar = decodedChar
        }
    }

    fun exportSession(context: Context): String? {
        if (logs.isEmpty()) return null

        val timeFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val timeStr = timeFormatter.format(Date(sessionStartTimeMs))
        val fileName = "blink_session_$timeStr.csv"

        val dir = File(context.filesDir, "logs")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName)
        try {
            FileWriter(file).use { writer ->
                writer.append("Timestamp,DurationMs,ShortCenter,LongCenter,Threshold,Symbol,Adaptation,DecodedChar\n")
                for (log in logs) {
                    writer.append("${log.timestampMs},${log.durationMs},${log.shortCenter},${log.longCenter},${log.threshold},${log.symbol},${log.adaptation},${log.decodedChar}\n")
                }
            }
            Log.d("SessionLogger", "Session exported to: ${file.absolutePath}")
            return file.name
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
