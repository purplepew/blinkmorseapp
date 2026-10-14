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
    cameraView: @Composable () -> Unit,
    onTextChange: (String) -> Unit = {},
    onNavigateToHelp: () -> Unit,
    onNavigateToMenu: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top Section: Morse Cheat Sheet & Live Face Tracker
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Left: Morse Reference Cheat Sheet Matrix
            Box(
                modifier = Modifier
                    .weight(0.66f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HudCardBgDark)
                    .border(1.5.dp, HudCyan, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    MorseCheatSheetGrid()
                }
            }

            // Right: Active Face Tracking Camera Widget
            Column(
                modifier = Modifier
                    .weight(0.34f)
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(HudAvatarBg)
                    .border(2.dp, HudCyan.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Camera Feed / Avatar Frame - fills available vertical space!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF082F49))
                        .border(1.dp, HudCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    cameraView()
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Face Active Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (faceDetectedState == true) HudGreen.copy(alpha = alpha) else HudRed.copy(alpha = alpha)
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (faceDetectedState == true) "Face Active" else "No Face",
                        color = if (faceDetectedState == true) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Main Live Display Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(HudBg)
                .drawCyberGrid()
                .border(1.5.dp, HudCyan, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            CornerAccents(color = HudCyan, length = 24f, strokeWidth = 5f, offset = 12f)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Fixed Width Top Morse Code Buffer Indicator (capacity for 6 symbols)
                Box(
                    modifier = Modifier
                        .widthIn(min = 160.dp)
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(if (isFlashing) HudGreen else HudGreenDark)
                        .border(1.5.dp, HudGreen, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LargeMorseVisualizer(
                        morseCode = currentSymbols,
                        maxSymbols = 6
                    )
                }

                // Sentence Display Area with Auto-Scaling Holographic Text
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                            lineHeight = 30.sp
                        )
                    } else {
                        AutoScalingText(
                            text = decodedText.ifEmpty { "START BLINKING..." },
                            maxFontSize = 32.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        verticalAlignment = Alignment.CenterVertically
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
