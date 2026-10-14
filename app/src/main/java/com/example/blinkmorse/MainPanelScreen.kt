package com.example.blinkmorse

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
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
    isFlashing: Boolean,
    cameraView: @Composable () -> Unit = {},
    onTextChange: (String) -> Unit = {},
    onNavigateToHelp: () -> Unit,
    onNavigateToMenu: () -> Unit,
) {
    // Hidden Camera Preview container to keep CameraX ImageAnalysis analyzer active in background
    Box(modifier = Modifier.size(1.dp).alpha(0f)) {
        cameraView()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top Section: Full-Width Morse Cheat Sheet Matrix
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.38f)
                .clip(RoundedCornerShape(16.dp))
                .background(HudCardBgDark)
                .border(1.5.dp, HudCyan, RoundedCornerShape(16.dp))
                .padding(10.dp),
        ) {
            CornerAccents(color = HudCyan, length = 12f, strokeWidth = 4f)
            MorseCheatSheetGrid()
        }

        // Main Live Display Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.62f)
                .clip(RoundedCornerShape(24.dp))
                .background(HudBg)
                .drawCyberGrid()
                .border(1.5.dp, HudCyan, RoundedCornerShape(24.dp))
                .padding(16.dp),
        ) {
            CornerAccents(color = HudCyan, length = 24f, strokeWidth = 5f, offset = 12f)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top Buffer Bar Row with Compact Face Status Indicator
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    // Centered Live Morse Buffer Capsule (capacity for 6 symbols)
                    Box(
                        modifier = Modifier
                            .widthIn(min = 160.dp)
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(if (isFlashing) HudGreen else HudGreenDark)
                            .border(1.5.dp, HudGreen, CircleShape)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LargeMorseVisualizer(
                            morseCode = currentSymbols,
                            maxSymbols = 6,
                        )
                    }

                    // Compact Face Tracking Indicator on the Right
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (faceDetectedState == true) Color(0x2200FF88) else Color(0x33FF2D55))
                            .border(
                                1.dp,
                                if (faceDetectedState == true) HudGreen.copy(alpha = 0.6f) else HudRed.copy(alpha = 0.6f),
                                RoundedCornerShape(12.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 0.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "alpha",
                        )
                        Text(
                            text = if (faceDetectedState == true) "● Face Active" else "⚠ Face Lost",
                            color = if (faceDetectedState == true) HudGreen.copy(alpha = alpha) else HudRed.copy(alpha = alpha),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }

                // Sentence Display Area with Auto-Scaling Holographic Text
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (isTest1Active) {
                        val annotatedString = buildAnnotatedString {
                            val target = "SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS SOS"
                            for (i in target.indices) {
                                if (i < decodedText.length) {
                                    if (decodedText[i] == target[i]) {
                                        withStyle(style = SpanStyle(color = HudGreen)) {
                                            append(decodedText[i])
                                        }
                                    } else {
                                        withStyle(style = SpanStyle(color = HudRed)) {
                                            append(decodedText[i])
                                        }
                                    }
                                } else {
                                    withStyle(style = SpanStyle(color = Color(0x66A5F3FC))) {
                                        append(target[i])
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
                        AutoScalingHolographicTextBox(
                            text = decodedText.ifEmpty { "START BLINKING..." },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                        )
                    }
                }

                // Bottom Controls: Keyboard Input Bar + Help (?) & Menu Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Keyboard Test Input Bar
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HudCardBgDark)
                            .border(1.dp, HudCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "TYPE:",
                            color = Color(0xFF67E8F9),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        BasicTextField(
                            value = decodedText,
                            onValueChange = onTextChange,
                            textStyle = TextStyle(
                                color = Color(0xFFA5F3FC),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(HudCyan),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // (?) Help Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HudRedDark)
                            .border(1.5.dp, HudRed, RoundedCornerShape(12.dp))
                            .clickable { onNavigateToHelp() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Menu Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xCC08291C))
                            .border(1.5.dp, HudGreen, RoundedCornerShape(12.dp))
                            .clickable { onNavigateToMenu() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Menu",
                            color = Color(0xFF6EE7B7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
