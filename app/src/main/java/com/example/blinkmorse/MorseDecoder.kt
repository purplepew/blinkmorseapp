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
            "---.." to "8", "----." to "9",
            "..--" to " ",      // Custom dedicated space command
            "....-" to "<BS>",  // Custom backspace (delete last character) - overrides '4'
            ".-.-" to "<SPEAK>" // Custom Speak & Clear command
        )
    }

    private var currentSymbols = ""
    private var timeoutJob: Job? = null

    fun addSymbol(symbol: String) {
        currentSymbols += symbol
        notifySymbolUpdated()
        resetTimeout()
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
            val decodedChar = MORSE_MAP[currentSymbols] ?: "?"
            Log.d("BlinkMorse", "Character Decoded: '$decodedChar' (from symbols: $currentSymbols)")
            onCharacterDecoded(decodedChar)
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
