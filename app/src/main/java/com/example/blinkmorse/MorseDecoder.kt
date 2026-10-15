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
    private val onCharacterDecoded: (String) -> Unit
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
            "....." to "5", "-...." to "6", "--..." to "7",
            "---.." to "<MENU>", // Custom Menu command (overrides '8')
            "----." to "9",
            "..--" to " ",       // Custom dedicated space command
            "....-" to "<BS>",   // Custom backspace (delete last character) - overrides '4'
            ".-.-" to "<SPEAK>", // Custom Speak & Clear command
            "..-.." to "<HELP>",  // Custom Help command (overrides '?')
            ".--.-." to "<TEST1>", // Custom Start/Cancel Test 1 command
            ".-...-" to "<DATALOGS>" // Custom Data Logs command
        )
    }

    private var currentSymbols = ""
    private var timeoutJob: Job? = null
    
    var isStrictMode = false

    fun addSymbol(symbol: String): Boolean {
        currentSymbols += symbol

        // INSTANT ABORT CHECK (Disabled in Strict Evaluation Mode)
        if (!isStrictMode && (currentSymbols.length >= 7)) {
            timeoutJob?.cancel()
            currentSymbols = ""
            notifySymbolUpdated()
            onAbort()
            return false // Returns false to indicate the blink was rejected/aborted
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
            // STRICT EXACT MATCH: No fallbacks, no guesses.
            val decodedChar = MORSE_MAP[currentSymbols]
            
            if (decodedChar != null) {
                Log.d("BlinkMorse", "Character Decoded: '$decodedChar' (from symbols: $currentSymbols)")
                onCharacterDecoded(decodedChar)
            } else {
                Log.d("BlinkMorse", "Unrecognized sequence: '$currentSymbols'. Sending empty string.")
                onCharacterDecoded("") // Empty string signifies invalid sequence
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
