package com.example.blinkmorse

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MorseDecoder(
    private val coroutineScope: CoroutineScope,
    private val onSymbolUpdated: (String) -> Unit,
    private val onAbort: () -> Unit,
    private val onCharacterDecoded: (decodedChar: String, currentSymbols: String, isError: Boolean) -> Unit
) {
    companion object {
        const val MORSE_CHARACTER_GAP_MS = 2500L

        val MORSE_MAP = mapOf(
            ".-" to "A", "-..." to "B", "-.-." to "C", "-.." to "D", "." to "E",
            "..-." to "F", "--." to "G", "...." to "H", ".." to "I", ".---" to "J",
            "-.-" to "K", ".-.." to "L", "--" to "M", "-." to "N", "---" to "O",
            ".--." to "P", "--.-" to "Q", ".-." to "R", "..." to "S", "-" to "T",
            "..-" to "U", "...-" to "V", ".--" to "W", "-..-" to "X", "-.--" to "Y",
            "--.." to "Z",
            "-----" to "0", ".----" to "1", "..---" to "2", "...--" to "3",
            "....-" to "4", "....." to "5", "-...." to "6", "--..." to "7",
            "---.." to "8", "----." to "9",
            "---.-" to "<MENU>",    // Custom Menu command
            "..--" to " ",          // Custom dedicated space command
            "...-." to "<BS>",      // Custom backspace command
            ".-.-" to "<SPEAK>",    // Custom Speak command
            "...---" to "<CLEAR>",  // Custom Clear All Board command
            "..-.." to "<HELP>",     // Custom Help command
            ".--.-." to "<TEST1>",    // Custom Start/Cancel Test 1 command
            ".-...-" to "<DATALOGS>", // Custom Data Logs command
            "---..." to "<PAIN>",     // Pain Alert command
            "----.." to "<REPOSITION>", // Reposition Request command
            ".-.-.-" to "<NIGHT_CALL>", // Night Call Light command
            "-.-.-" to "<QUICK_SOS>", // Quick SOS Message command
            "-...-" to "<STATUS>",   // Battery & Time Status command
            ".-.-." to "<TOGGLE_KEYPAD>", // Keypad Mode Toggle command
            "..-.-" to "<CLOSE>",    // Close Modal command
            "-.-.--" to "<WATER>",   // Need Water command
            "...--." to "<TEMP>",    // Temperature Adjustment command
            "......" to "<YES>",     // Instant Yes
            "------" to "<NO>",      // Instant No
            "..--.." to "<WAIT>",    // Give Me A Moment / Wait
            "--..--" to "<PRIVACY>", // Privacy Request
            ".-..-." to "<FAMILY>"   // Family Quick Message
        )

        // Special Quick-Response Mode dictionary used when Help Panel is open
        val QUICK_MODE_MAP = mapOf(
            "." to "<YES>",
            ".." to "<NO>",
            "-" to "<WATER>",
            "--" to "<PAIN>",
            "..." to "<REPOSITION>",
            "---" to "<TEMP>",
            ".-" to "<WAIT>",
            "-." to "<PRIVACY>",
            ".--" to "<FAMILY>",
            "---.-" to "<MENU>",
            "..-.." to "<HELP>",
            "..-.-" to "<CLOSE>"
        )
    }

    private var currentSymbols = ""
    val currentSequence: String get() = currentSymbols
    private var timeoutJob: Job? = null
    
    var isStrictMode = false
    var isQuickModeActive = false
    var characterGapMs: Long = MORSE_CHARACTER_GAP_MS

    fun addSymbol(symbol: String): Boolean {
        currentSymbols += symbol

        if (currentSymbols.length >= 7) {
            timeoutJob?.cancel()
            val overflowSeq = currentSymbols
            currentSymbols = ""
            notifySymbolUpdated()

            if (isStrictMode) {
                // Strict Evaluation Mode: Treat >= 7 blinks as a definitive error (#)
                onCharacterDecoded("#", overflowSeq, true)
                return true // Accept the blink (no manual abort buzz inside addSymbol)
            } else {
                // Standard Mode: Wipe buffer and rollback
                onAbort()
                return false
            }
        }

        notifySymbolUpdated()
        resetTimeout()
        return true // Returns true to indicate normal blink accepted
    }

    private fun resetTimeout() {
        timeoutJob?.cancel()
        timeoutJob = coroutineScope.launch(Dispatchers.Main) {
            delay(characterGapMs)
            decodeCurrent()
        }
    }

    private fun decodeCurrent() {
        if (currentSymbols.isNotEmpty()) {
            val decodedChar = if (isQuickModeActive) {
                QUICK_MODE_MAP[currentSymbols] ?: MORSE_MAP[currentSymbols]
            } else {
                MORSE_MAP[currentSymbols]
            }
            val seq = currentSymbols

            if (isStrictMode) {
                // In Strict Mode, unmapped strings OR any control prosigns (starting with '<' or equals ' ') 
                // are illegal and result in the error token '#'
                if (decodedChar == null || decodedChar.startsWith("<") || decodedChar == " ") {
                    Log.d("BlinkMorse", "Strict Mode Reject: '$seq' -> mapped to '$decodedChar'. Emitting '#'.")
                    onCharacterDecoded("#", seq, true)
                } else {
                    Log.d("BlinkMorse", "Character Decoded: '$decodedChar' (from symbols: $seq)")
                    onCharacterDecoded(decodedChar, seq, false)
                }
            } else {
                // Normal Mode
                if (decodedChar != null) {
                    Log.d("BlinkMorse", "Character Decoded: '$decodedChar' (from symbols: $seq)")
                    onCharacterDecoded(decodedChar, seq, false)
                } else {
                    Log.d("BlinkMorse", "Unrecognized sequence: '$seq'. Sending empty string.")
                    onCharacterDecoded("", seq, false) // Empty string signifies invalid sequence
                }
            }
            
            currentSymbols = ""
            notifySymbolUpdated()
        }
    }

    private fun notifySymbolUpdated() {
        coroutineScope.launch(Dispatchers.Main) {
            onSymbolUpdated(currentSymbols)
        }
    }

    fun clear() {
        timeoutJob?.cancel()
        currentSymbols = ""
        notifySymbolUpdated()
    }
}
