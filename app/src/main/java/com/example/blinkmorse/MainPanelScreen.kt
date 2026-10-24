package com.example.blinkmorse

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainPanelScreen(
    currentSymbols: String,
    decodedText: String,
    faceDetectedState: Boolean?,
    isTest1Active: Boolean,
    thresholdInfo: String = "",
    keypadMode: KeypadMode = KeypadMode.ABC,
    onModeChange: (KeypadMode) -> Unit = {},
    cameraView: @Composable () -> Unit = {},
    onNavigateToHelp: () -> Unit,
    onNavigateToMenu: () -> Unit,
) {
    // Hidden Camera Preview container to keep CameraX analyzer active in background
    Box(modifier = Modifier.size(1.dp).alpha(0f)) {
        cameraView()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SolarBg)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Top Section: Display Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .clip(RoundedCornerShape(24.dp))
                .background(SolarDisplayBg)
                .drawCyberGrid()
                .border(1.5.dp, SolarLine, RoundedCornerShape(24.dp))
                .padding(16.dp),
        ) {
            // Top Right: Help & Menu Buttons
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolarKeyBg)
                        .border(1.dp, SolarLine, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToHelp() }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("?", color = SolarInk, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    MorseVisualizer(morseCode = "..-..", dotColor = SolarAccent2, dashColor = SolarAccent)
                }
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolarKeyBg)
                        .border(1.dp, SolarLine, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToMenu() }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("MENU", color = SolarInk, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    MorseVisualizer(morseCode = "---.-", dotColor = SolarAccent2, dashColor = SolarAccent)
                }
            }

            // Center: Decoded Text (AutoScalingText)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isTest1Active) {
                    val annotatedString = buildAnnotatedString {
                        val visualTarget = "SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS"
                        var decodedIndex = 0

                        for (i in visualTarget.indices) {
                            val targetChar = visualTarget[i]
                            
                            if (targetChar == ' ') {
                                withStyle(style = SpanStyle(color = SolarMuted)) { append(" ") }
                            } else {
                                if (decodedIndex < decodedText.length) {
                                    val decodedChar = decodedText[decodedIndex]
                                    if (decodedChar == targetChar) {
                                        withStyle(style = SpanStyle(color = SolarAccent2)) { append(decodedChar) }
                                    } else {
                                        withStyle(style = SpanStyle(color = SolarAccent)) { append(decodedChar) }
                                    }
                                    decodedIndex++
                                } else {
                                    withStyle(style = SpanStyle(color = SolarMuted)) { append(targetChar) }
                                }
                            }
                        }
                    }
                    Text(
                        text = annotatedString,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.5.sp,
                        lineHeight = 30.sp,
                    )
                } else {
                    AutoScalingText(
                        text = decodedText.ifEmpty { "START BLINKING..." },
                        maxFontSize = 48.sp,
                        textColor = SolarInk,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    )
                }
            }

            // Bottom Left: Compact Calibration Telemetry Indicator
            Text(
                text = if (thresholdInfo.isNotEmpty() && !thresholdInfo.contains("---")) {
                    thresholdInfo
                        .replace("Threshold:", "T:")
                        .replace("Short:", "•")
                        .replace("Long:", "—")
                } else {
                    "T: --- | • --- | — ---"
                },
                color = SolarMuted.copy(alpha = 0.85f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomStart),
            )

            // Bottom Right: Face Tracking Indicator
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(animation = tween(600), repeatMode = RepeatMode.Reverse),
                    label = "alpha",
                )
                val dotColor = if (faceDetectedState == true) SolarAccent.copy(alpha = alpha) else Color.Red.copy(alpha = alpha)
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor),
                )
                Text(
                    text = if (faceDetectedState == true) "FACE ACTIVE" else "FACE LOST",
                    color = dotColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }
        }

        // Middle Section: Buffer Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SolarKeyBg)
                .border(1.5.dp, SolarLine, RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            LargeMorseVisualizer(
                morseCode = currentSymbols,
                maxSymbols = 6,
            )
        }

        // Bottom Section: Cheat Sheet Keypad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f),
        ) {
            MorseCheatSheetGrid(selectedMode = keypadMode, onModeChange = onModeChange)
        }
    }
}
