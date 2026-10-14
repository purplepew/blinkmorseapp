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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IntroPanelScreen(
    shortCount: Int = 0,
    longCount: Int = 0,
    currentBuffer: String = "",
    promptMessage: String = "",
    isFlashing: Boolean = false,
    faceDetectedState: Boolean? = null,
    cameraView: (@Composable () -> Unit)? = null,
    canSkip: Boolean = false,
    onNavigateToMain: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top Section: Calibration & Avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Left: Animated Avatar Preview Demo & Live Camera Feed
            Column(
                modifier = Modifier
                    .weight(0.66f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HudAvatarBg)
                    .border(2.dp, HudCyan.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x66164E63))
                        .border(1.dp, HudCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (cameraView != null) {
                        cameraView()
                    } else {
                        SimulatedAvatarSvg()
                    }
                    
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (faceDetectedState) {
                                    true -> HudGreen.copy(alpha = alpha)
                                    false -> HudRed.copy(alpha = alpha)
                                    else -> HudGreen.copy(alpha = alpha)
                                }
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = if (faceDetectedState == true) "Face Active" else "No Face",
                        color = if (faceDetectedState == true) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Right: Calibration Checklist
            Box(
                modifier = Modifier
                    .weight(0.34f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HudCardBg)
                    .border(1.5.dp, HudCyan, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                CornerAccents(color = HudCyan, length = 12f, strokeWidth = 4f)
                
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    CalibrationTask(
                        label = "10 short blinks",
                        progress = "($shortCount/10)",
                        isChecked = shortCount >= 10,
                        labelColor = Color(0xFFA5F3FC)
                    )
                    CalibrationTask(
                        label = "10 long blinks",
                        progress = "($longCount/10)",
                        isChecked = longCount >= 10,
                        labelColor = Color(0xFF67E8F9)
                    )
                }
            }
        }

        // Main Neon HUD Board Container
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
                // Fixed Width Top Morse Code Current Buffer Capsule (capacity for 10 calibration blinks)
                Box(
                    modifier = Modifier
                        .widthIn(min = 180.dp)
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(if (isFlashing) HudGreen else HudGreenDark)
                        .border(1.5.dp, HudGreen, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LargeMorseVisualizer(
                        morseCode = currentBuffer,
                        maxSymbols = 10
                    )
                }

                // Central High-Tech Text Display Area
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = promptMessage.ifEmpty { "Perform 10 short blinks (dots) [$shortCount/10]" },
                        color = Color(0xFFA5F3FC),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.2.sp,
                        lineHeight = 28.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.alpha(0.7f)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(HudCyan))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.width(48.dp).height(1.dp).background(HudCyan))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if ((shortCount >= 10) && (longCount >= 10)) "CALIBRATION PASS" else "CALIBRATING...",
                            color = if ((shortCount >= 10) && (longCount >= 10)) HudGreen else Color(0xFF67E8F9),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.width(48.dp).height(1.dp).background(HudCyan))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(HudCyan))
                    }
                }

                // Bottom Action CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    val isComplete = (shortCount >= 10) && (longCount >= 10)
                    if (isComplete || canSkip) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isComplete) Color(0xCC083344) else Color(0x44083344))
                                .border(
                                    1.5.dp,
                                    if (isComplete) HudGreen else HudCyan.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable { onNavigateToMain() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = if (isComplete) "PROCEED TO MAIN →" else "SKIP TO MAIN →",
                                color = if (isComplete) HudGreen else Color(0xFF67E8F9),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x22083344))
                                .border(
                                    1.dp,
                                    Color.Gray.copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp),
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "COMPLETE CALIBRATION",
                                color = Color.Gray.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
