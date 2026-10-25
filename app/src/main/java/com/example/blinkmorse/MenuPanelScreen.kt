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
import java.util.Locale

@Composable
fun MenuPanelScreen(
    currentSymbols: String = "",
    isTest1Active: Boolean = false,
    characterGapMs: Long = 2500L,
    thresholdInfo: String = "",
    lastAdaptation: String = "",
    currentFps: Int = 0,
    onCharacterGapChange: (Long) -> Unit = {},
    onTest1Click: () -> Unit,
    onDataManagerClick: () -> Unit,
    onRecalibrateClick: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolarBg.copy(alpha = 0.85f))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Floating Menu Modal Window
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(SolarBg)
                .drawCyberGrid()
                .border(1.5.dp, SolarLine, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(24.dp))

                // Floating High-Tech Menu Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolarKeyBg)
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
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    // Calibration Telemetry Details Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SolarDisplayBg.copy(alpha = 0.5f))
                            .border(1.dp, SolarLine.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "CALIBRATION METRICS",
                                color = SolarMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = thresholdInfo.ifEmpty { "Threshold: N/A | Dot: N/A | Dash: N/A" },
                                color = SolarInk,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                            )
                            if (lastAdaptation.isNotEmpty() && (lastAdaptation != "N/A")) {
                                Text(
                                    text = "Status: $lastAdaptation | FPS: $currentFps",
                                    color = SolarMuted,
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Inter-Character Gap (Pause Timeout) Setting Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SolarDisplayBg.copy(alpha = 0.5f))
                            .border(1.dp, SolarLine.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CHARACTER PAUSE GAP",
                                    color = SolarMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                )
                                Text(
                                    text = String.format(Locale.US, "%.2fs", characterGapMs / 1000f),
                                    color = SolarInk,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            // Preset Pills Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // -0.25s Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SolarKeyBg)
                                        .border(1.dp, SolarLine, RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (characterGapMs > 1000L) {
                                                onCharacterGapChange(characterGapMs - 250L)
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = SolarInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                val presets = listOf(1500L, 2000L, 2500L, 3000L, 3500L)
                                presets.forEach { preset ->
                                    val isSelected = (characterGapMs == preset)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) SolarAccent else SolarKeyBg)
                                            .border(1.dp, if (isSelected) SolarAccent else SolarLine, RoundedCornerShape(6.dp))
                                            .clickable { onCharacterGapChange(preset) }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.1fs", preset / 1000f),
                                            color = if (isSelected) SolarBg else SolarInk,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                // +0.25s Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SolarKeyBg)
                                        .border(1.dp, SolarLine, RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (characterGapMs < 5000L) {
                                                onCharacterGapChange(characterGapMs + 250L)
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = SolarInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Grid of Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MenuButton(
                            title = if (isTest1Active) "CANCEL TESTING" else "START TESTING",
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

                Spacer(modifier = Modifier.height(16.dp))

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
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "CLOSE",
                                color = SolarAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            MorseVisualizer(morseCode = "..-.-", dotColor = SolarAccent2, dashColor = SolarAccent)
                        }
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
        Spacer(modifier = Modifier.height(4.dp))
        MorseVisualizer(morseCode = morse)
    }
}
