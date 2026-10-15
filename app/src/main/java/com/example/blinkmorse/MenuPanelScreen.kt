package com.example.blinkmorse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuPanelScreen(
    currentSymbols: String = "",
    isTest1Active: Boolean = false,
    onClearClick: () -> Unit,
    onSpeakClick: () -> Unit,
    onTest1Click: () -> Unit,
    onDataManagerClick: () -> Unit,
    onRecalibrateClick: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SolarBg)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DimmedTopHeader()

        // Main Shell with Floating Menu Modal Window
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(SolarBg)
                .drawCyberGrid()
                .border(1.5.dp, SolarLine, RoundedCornerShape(24.dp))
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top Morse Indicator (capacity for 6 symbols)
                Box(
                    modifier = Modifier
                        .widthIn(min = 160.dp)
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(SolarKeyBg)
                        .border(1.5.dp, SolarLine, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LargeMorseVisualizer(
                        morseCode = currentSymbols,
                        maxSymbols = 6,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Floating High-Tech Menu Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolarKeyBg.copy(alpha = 0.9f))
                        .border(1.5.dp, SolarLine, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "MENU",
                        color = SolarInk,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    // Grid of Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MenuButton(
                            title = "CLEAR",
                            morse = "....-",
                            onClick = onClearClick,
                            modifier = Modifier.weight(1f),
                        )
                        MenuButton(
                            title = "SPEAK",
                            morse = ".-.-",
                            onClick = onSpeakClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MenuButton(
                            title = if (isTest1Active) "CANCEL TEST" else "START TEST 1",
                            morse = ".--.-.",
                            onClick = onTest1Click,
                            modifier = Modifier.weight(1f),
                        )
                        MenuButton(
                            title = "DATA LOGS",
                            morse = ".-...-",
                            onClick = onDataManagerClick,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Recalibrate Full-width Action
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isTest1Active) SolarAccent.copy(alpha = 0.2f) else SolarKeyBg)
                            .border(1.dp, if (isTest1Active) SolarAccent else SolarLine, RoundedCornerShape(12.dp))
                            .clickable { onRecalibrateClick() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "RECALIBRATE BLINKS",
                            color = if (isTest1Active) SolarAccent else SolarInk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SolarKeyBg)
                            .border(1.dp, SolarAccent, RoundedCornerShape(12.dp))
                            .clickable { onClose() }
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "CLOSE",
                            color = SolarAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuButton(title: String, morse: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SolarKeyBg)
            .border(1.dp, SolarLine, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            color = SolarInk,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = morse,
            color = SolarMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
