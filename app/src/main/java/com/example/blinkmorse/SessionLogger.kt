package com.example.blinkmorse

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

data class BlinkLogEntry(
    val blinkIndex: Int,
    val timestampMs: Long,
    val elapsedTestSec: Float,
    val charAttemptIndex: Int,
    val targetChar: Char,
    val durationMs: Long,
    val shortCenterMs: Float,
    val longCenterMs: Float,
    val thresholdMs: Float,
    val symbol: String,
    val adaptationStatus: String,
    var stagedSequence: String,
    var decodedChar: String = ""
)

class SessionLogger {
    private val logs = mutableListOf<BlinkLogEntry>()
    private var sessionStartTimeMs: Long = 0
    private var blinkCounter = 0

    fun startNewSession() {
        logs.clear()
        sessionStartTimeMs = System.currentTimeMillis()
        blinkCounter = 0
    }

    fun logBlink(
        testStartTimeMs: Long = 0L,
        charAttemptIndex: Int = 0,
        targetChar: Char = ' ',
        durationMs: Long,
        shortCenter: Float,
        longCenter: Float,
        threshold: Float,
        symbol: String,
        adaptation: String,
        stagedSequence: String = ""
    ) {
        blinkCounter++
        val timestamp = System.currentTimeMillis()
        val elapsedSec = if (testStartTimeMs > 0) (timestamp - testStartTimeMs) / 1000f else 0f

        val entry = BlinkLogEntry(
            blinkIndex = blinkCounter,
            timestampMs = timestamp,
            elapsedTestSec = elapsedSec,
            charAttemptIndex = charAttemptIndex,
            targetChar = targetChar,
            durationMs = durationMs,
            shortCenterMs = shortCenter,
            longCenterMs = longCenter,
            thresholdMs = threshold,
            symbol = symbol,
            adaptationStatus = adaptation,
            stagedSequence = stagedSequence
        )
        logs.add(entry)
    }

    fun attachDecodedChar(decodedChar: String) {
        if (logs.isNotEmpty()) {
            logs.last().decodedChar = decodedChar
        }
    }

    // Levenshtein Edit Distance Calculation
    private fun calculateLevenshtein(target: String, decoded: String): Int {
        val dp = Array(target.length + 1) { IntArray(decoded.length + 1) }

        for (i in 0..target.length) {
            for (j in 0..decoded.length) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else if (target[i - 1] == decoded[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    dp[i][j] = 1 + minOf(
                        dp[i][j - 1],    // Insert
                        dp[i - 1][j],    // Remove
                        dp[i - 1][j - 1] // Replace
                    )
                }
            }
        }
        return dp[target.length][decoded.length]
    }

    fun exportTest1Session(
        context: Context,
        testMode: String,
        targetString: String,
        decodedString: String,
        testStartTimeMs: Long,
        testEndTimeMs: Long,
        totalValidBlinks: Int,
        initialThresholdMs: Float,
        finalThresholdMs: Float
    ): String? {
        if (logs.isEmpty()) return null

        val timeFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val timeStr = timeFormatter.format(Date(sessionStartTimeMs))
        val fileName = "blink_test1_${testMode}_$timeStr.csv"

        val dir = File(context.filesDir, "logs")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName)
        try {
            FileWriter(file).use { writer ->
                // Section 1: Time-series rows
                writer.append("BlinkIndex,TimestampMs,ElapsedTestSec,CharAttemptIndex,TargetChar,DurationMs,ShortCenterMs,LongCenterMs,ThresholdMs,Symbol,AdaptationStatus,StagedSequence,DecodedChar\n")
                for (log in logs) {
                    writer.append("${log.blinkIndex},${log.timestampMs},${String.format(Locale.US, "%.2f", log.elapsedTestSec)},")
                    writer.append("${log.charAttemptIndex},${log.targetChar},${log.durationMs},")
                    writer.append("${log.shortCenterMs},${log.longCenterMs},${log.thresholdMs},")
                    writer.append("${log.symbol},${log.adaptationStatus},${log.stagedSequence},${log.decodedChar}\n")
                }

                // Mathematical Computations
                val elapsedDurationSec = if (testStartTimeMs > 0 && testEndTimeMs > testStartTimeMs) {
                    (testEndTimeMs - testStartTimeMs) / 1000f
                } else {
                    0f
                }
                val elapsedMinutes = elapsedDurationSec / 60.0f
                val targetLength = targetString.length
                
                val levenshtein = calculateLevenshtein(targetString, decodedString)
                val cerOverall = if (targetLength > 0) (levenshtein.toFloat() / targetLength.toFloat()) * 100f else 0f
                
                val wpm = if (elapsedMinutes > 0) (targetLength / 5.0f) / elapsedMinutes else 0f
                val idealBlinks = 135 // For 15x SOS
                val bor = if (idealBlinks > 0) totalValidBlinks.toFloat() / idealBlinks.toFloat() else 0f

                // Epoch calculations (Initial 1-15 vs Terminal 31-45)
                val initTarget = if (targetString.length >= 15) targetString.substring(0, 15) else targetString
                val initDecoded = if (decodedString.length >= 15) decodedString.substring(0, 15) else decodedString
                val initLevenshtein = calculateLevenshtein(initTarget, initDecoded)
                val cerInit = if (initTarget.isNotEmpty()) (initLevenshtein.toFloat() / initTarget.length.toFloat()) * 100f else 0f

                val termTarget = if (targetString.length >= 45) targetString.substring(30, 45) else ""
                val termDecoded = if (decodedString.length >= 45) decodedString.substring(30, 45) else ""
                val termLevenshtein = calculateLevenshtein(termTarget, termDecoded)
                val cerTerm = if (termTarget.isNotEmpty()) (termLevenshtein.toFloat() / termTarget.length.toFloat()) * 100f else 0f
                
                val thresholdDrift = finalThresholdMs - initialThresholdMs
                val driftSign = if (thresholdDrift > 0) "+" else ""

                writer.append("\n# SUMMARY_METRICS\n")
                writer.append("Metric,Value\n")
                writer.append("TestMode,$testMode\n")
                writer.append("TargetString,$targetString\n")
                writer.append("DecodedString,$decodedString\n")
                writer.append("TargetLength,$targetLength\n")
                writer.append("DecodedLength,${decodedString.length}\n")
                writer.append("LevenshteinEditDistance,$levenshtein\n")
                writer.append("CER_Overall_Percent,${String.format(Locale.US, "%.2f", cerOverall)}\n")
                writer.append("CER_Initial_1_15_Percent,${String.format(Locale.US, "%.2f", cerInit)}\n")
                writer.append("CER_Terminal_31_45_Percent,${String.format(Locale.US, "%.2f", cerTerm)}\n")
                writer.append("ElapsedDurationSec,${String.format(Locale.US, "%.2f", elapsedDurationSec)}\n")
                writer.append("WPM,${String.format(Locale.US, "%.2f", wpm)}\n")
                writer.append("TotalValidBlinks,$totalValidBlinks\n")
                writer.append("IdealBlinks,$idealBlinks\n")
                writer.append("BlinkOverheadRatio,${String.format(Locale.US, "%.2f", bor)}\n")
                writer.append("InitialThresholdMs,${String.format(Locale.US, "%.2f", initialThresholdMs)}\n")
                writer.append("FinalThresholdMs,${String.format(Locale.US, "%.2f", finalThresholdMs)}\n")
                writer.append("ThresholdDriftMs,$driftSign${String.format(Locale.US, "%.2f", thresholdDrift)}\n")
            }
            Log.d("SessionLogger", "Test 1 Session exported to: ${file.absolutePath}")
            return file.name
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Keep the simple export for normal typing mode
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
                    writer.append("${log.timestampMs},${log.durationMs},${log.shortCenterMs},${log.longCenterMs},${log.thresholdMs},${log.symbol},${log.adaptationStatus},${log.decodedChar}\n")
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
