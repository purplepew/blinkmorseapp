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

        private val MORSE_MAP = mapOf(
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
            "..-.." to "<HELP>",     // Custom Help command (overrides '?')
            ".--.-." to "<TEST1>",    // Custom Start/Cancel Test 1 command
            ".-...-" to "<DATALOGS>", // Custom Data Logs command
            "---..." to "<SIREN>",   // Custom Emergency Siren command
            "----.." to "<CARE_ALERT>", // Custom Clinical Care Request command
            ".-.-.-" to "<NIGHT_CALL>", // Custom Night Call Light command
            "-.-.-" to "<QUICK_SOS>", // Custom Quick SOS Message command
            "-...-" to "<STATUS>",   // Custom Battery & Time Status command
            ".-.-." to "<TOGGLE_KEYPAD>", // Custom Hands-Free Keypad Mode Toggle command
            "..-.-" to "<CLOSE>",    // Custom Close Modal command
            "-.-.--" to "<FATIGUE_TEST>" // Custom Fatigue Test command
        )
    }

    private var currentSymbols = ""
    private var timeoutJob: Job? = null
    
    var isStrictMode = false

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
            delay(MORSE_CHARACTER_GAP_MS)
            decodeCurrent()
        }
    }

    private fun decodeCurrent() {
        if (currentSymbols.isNotEmpty()) {
            val decodedChar = MORSE_MAP[currentSymbols]
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
