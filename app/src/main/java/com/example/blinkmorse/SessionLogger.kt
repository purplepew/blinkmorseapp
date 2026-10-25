package com.example.blinkmorse

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val characterGapMs: Long = 2500L,
    val symbol: String,
    val adaptationStatus: String,
    var stagedSequence: String,
    var decodedChar: String = "",
    val fixedSymbol: String = "",
    var fixedDecodedChar: String = ""
)

class SessionLogger {
    private val logs = mutableListOf<BlinkLogEntry>()
    private var sessionStartTimeMs: Long = 0
    private var firstBlinkTimestampMs: Long = 0L
    private var blinkCounter = 0
    private var fixedCalibratedThresholdMs: Float = 0f
    private var fixedCurrentSymbols: String = ""
    private var fixedDecodedString: String = ""

    fun startNewSession() {
        logs.clear()
        sessionStartTimeMs = System.currentTimeMillis()
        firstBlinkTimestampMs = 0L
        blinkCounter = 0
        fixedCalibratedThresholdMs = 0f
        fixedCurrentSymbols = ""
        fixedDecodedString = ""
    }

    fun logBlink(
        charAttemptIndex: Int = 0,
        targetChar: Char = ' ',
        durationMs: Long,
        shortCenter: Float,
        longCenter: Float,
        threshold: Float,
        characterGapMs: Long = 2500L,
        symbol: String,
        adaptation: String,
        stagedSequence: String = ""
    ) {
        blinkCounter++
        val timestamp = System.currentTimeMillis()
        if (logs.isEmpty()) {
            firstBlinkTimestampMs = timestamp
            fixedCalibratedThresholdMs = threshold
            fixedCurrentSymbols = ""
            fixedDecodedString = ""
        }

        val elapsedSec = (timestamp - firstBlinkTimestampMs) / 1000.0f
        val fixedSymbol = if (durationMs.toFloat() < fixedCalibratedThresholdMs) "." else "-"
        fixedCurrentSymbols += fixedSymbol

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
            characterGapMs = characterGapMs,
            symbol = symbol,
            adaptationStatus = adaptation,
            stagedSequence = stagedSequence,
            fixedSymbol = fixedSymbol
        )
        logs.add(entry)
    }

    fun attachDecodedChar(decodedChar: String) {
        if (logs.isNotEmpty()) {
            val lastEntry = logs.last()
            lastEntry.decodedChar = decodedChar

            val fixedLookup = MorseDecoder.MORSE_MAP[fixedCurrentSymbols]
            val fixedDecoded = if (fixedCurrentSymbols.length >= 7 || fixedLookup == null || fixedLookup.startsWith("<") || fixedLookup == " ") {
                "#"
            } else {
                fixedLookup
            }
            lastEntry.fixedDecodedChar = fixedDecoded
            fixedDecodedString += fixedDecoded
            fixedCurrentSymbols = ""
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
        testEndTimeMs: Long = 0L,
        finalThresholdMs: Float = 0f
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
                writer.append("BlinkIndex,TimestampMs,ElapsedTestSec,CharAttemptIndex,TargetChar,DurationMs,ShortCenterMs,LongCenterMs,ThresholdMs,CharacterGapMs,Symbol,AdaptationStatus,StagedSequence,DecodedChar,FixedSymbol,FixedDecodedChar\n")
                for (log in logs) {
                    writer.append("${log.blinkIndex},${log.timestampMs},${String.format(Locale.US, "%.2f", log.elapsedTestSec)},")
                    writer.append("${log.charAttemptIndex},${log.targetChar},${log.durationMs},")
                    writer.append("${String.format(Locale.US, "%.2f", log.shortCenterMs)},${String.format(Locale.US, "%.2f", log.longCenterMs)},${String.format(Locale.US, "%.2f", log.thresholdMs)},")
                    writer.append("${log.characterGapMs},${log.symbol},${log.adaptationStatus},${log.stagedSequence},${log.decodedChar},${log.fixedSymbol},${log.fixedDecodedChar}\n")
                }

                // Internalized Mathematical Computations
                val totalValidBlinks = logs.size
                val idealBlinks = 135 // For 15x SOS
                val bor = totalValidBlinks.toFloat() / idealBlinks.toFloat()

                val lastGap = logs.lastOrNull()?.characterGapMs ?: 2500L
                val startMs = logs.firstOrNull()?.timestampMs ?: 0L
                val endMs = if (testEndTimeMs > startMs) testEndTimeMs else ((logs.lastOrNull()?.timestampMs ?: startMs) + lastGap)
                val elapsedDurationSec = if (endMs > startMs && logs.isNotEmpty()) (endMs - startMs) / 1000.0f else 0.0f
                val elapsedMinutes = elapsedDurationSec / 60.0f

                val targetLength = targetString.length
                val wpm = if (elapsedMinutes > 0f) (targetLength / 5.0f) / elapsedMinutes else 0.0f

                val initialShortCenterMs = logs.firstOrNull()?.shortCenterMs ?: 0f
                val finalShortCenterMs = logs.lastOrNull()?.shortCenterMs ?: initialShortCenterMs
                val initialLongCenterMs = logs.firstOrNull()?.longCenterMs ?: 0f
                val finalLongCenterMs = logs.lastOrNull()?.longCenterMs ?: initialLongCenterMs
                val initialThresholdMs = logs.firstOrNull()?.thresholdMs ?: fixedCalibratedThresholdMs
                val effectiveFinalThreshold = if (finalThresholdMs > 0f) finalThresholdMs else (logs.lastOrNull()?.thresholdMs ?: initialThresholdMs)
                val thresholdDrift = effectiveFinalThreshold - initialThresholdMs
                val driftSign = if (thresholdDrift > 0) "+" else ""

                // Adaptive Metrics
                val levenshtein = calculateLevenshtein(targetString, decodedString)
                val cerOverall = if (targetLength > 0) (levenshtein.toFloat() / targetLength.toFloat()) * 100f else 0f

                val initTarget = if (targetString.length >= 15) targetString.substring(0, 15) else targetString
                val initDecoded = if (decodedString.length >= 15) decodedString.substring(0, 15) else decodedString
                val initLevenshtein = calculateLevenshtein(initTarget, initDecoded)
                val cerInit = if (initTarget.isNotEmpty()) (initLevenshtein.toFloat() / initTarget.length.toFloat()) * 100f else 0f

                val termTarget = if (targetString.length >= 45) targetString.substring(30, 45) else ""
                val termDecoded = if (decodedString.length >= 45) decodedString.substring(30, 45) else ""
                val termLevenshtein = calculateLevenshtein(termTarget, termDecoded)
                val cerTerm = if (termTarget.isNotEmpty()) (termLevenshtein.toFloat() / termTarget.length.toFloat()) * 100f else 0f

                // Shadow Fixed Baseline Metrics
                val fixedLevenshtein = calculateLevenshtein(targetString, fixedDecodedString)
                val fixedCerOverall = if (targetLength > 0) (fixedLevenshtein.toFloat() / targetLength.toFloat()) * 100f else 0f

                val initFixed = if (fixedDecodedString.length >= 15) fixedDecodedString.substring(0, 15) else fixedDecodedString
                val fixedCerInit = if (initTarget.isNotEmpty()) (calculateLevenshtein(initTarget, initFixed).toFloat() / initTarget.length.toFloat()) * 100f else 0f

                val termFixed = if (fixedDecodedString.length >= 45) fixedDecodedString.substring(30, 45) else ""
                val fixedCerTerm = if (termTarget.isNotEmpty()) (calculateLevenshtein(termTarget, termFixed).toFloat() / termTarget.length.toFloat()) * 100f else 0f

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
                writer.append("ConfiguredCharacterWindowMs,$lastGap\n")
                writer.append("ConfiguredCharacterWindowSec,${String.format(Locale.US, "%.2f", lastGap / 1000f)}\n")
                writer.append("InitialShortCenterMs,${String.format(Locale.US, "%.2f", initialShortCenterMs)}\n")
                writer.append("FinalShortCenterMs,${String.format(Locale.US, "%.2f", finalShortCenterMs)}\n")
                writer.append("InitialLongCenterMs,${String.format(Locale.US, "%.2f", initialLongCenterMs)}\n")
                writer.append("FinalLongCenterMs,${String.format(Locale.US, "%.2f", finalLongCenterMs)}\n")
                writer.append("InitialThresholdMs,${String.format(Locale.US, "%.2f", initialThresholdMs)}\n")
                writer.append("FinalThresholdMs,${String.format(Locale.US, "%.2f", effectiveFinalThreshold)}\n")
                writer.append("ThresholdDriftMs,$driftSign${String.format(Locale.US, "%.2f", thresholdDrift)}\n")
                
                // Fixed Shadow Metrics Section
                writer.append("FixedCalibratedThresholdMs,${String.format(Locale.US, "%.2f", fixedCalibratedThresholdMs)}\n")
                writer.append("FixedDecodedString,$fixedDecodedString\n")
                writer.append("Fixed_LevenshteinEditDistance,$fixedLevenshtein\n")
                writer.append("Fixed_CER_Overall_Percent,${String.format(Locale.US, "%.2f", fixedCerOverall)}\n")
                writer.append("Fixed_CER_Initial_1_15_Percent,${String.format(Locale.US, "%.2f", fixedCerInit)}\n")
                writer.append("Fixed_CER_Terminal_31_45_Percent,${String.format(Locale.US, "%.2f", fixedCerTerm)}\n")
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
                writer.append("Timestamp,DurationMs,ShortCenter,LongCenter,Threshold,CharacterGapMs,Symbol,Adaptation,DecodedChar\n")
                for (log in logs) {
                    writer.append("${log.timestampMs},${log.durationMs},${String.format(Locale.US, "%.2f", log.shortCenterMs)},${String.format(Locale.US, "%.2f", log.longCenterMs)},${String.format(Locale.US, "%.2f", log.thresholdMs)},${log.characterGapMs},${log.symbol},${log.adaptationStatus},${log.decodedChar}\n")
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
