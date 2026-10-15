package com.example.blinkmorse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpPanelScreen(currentSymbols: String = "", onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolarBg.copy(alpha = 0.85f))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Help Modal Window
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quick Assist Frame
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolarKeyBg)
                        .border(1.5.dp, SolarLine, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "CHEAT SHEET & COMMANDS",
                        color = SolarInk,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = SolarLine.copy(alpha = 0.6f),
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = SolarAccent)) { append("SPACE (..--)") }
                                append(" - Insert space between words")
                            },
                            color = SolarMuted,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = SolarLine.copy(alpha = 0.6f),
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = SolarAccent)) { append("SPEAK (.-.-)") }
                                append(" - Read text aloud & clear")
                            },
                            color = SolarMuted,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = SolarLine.copy(alpha = 0.6f),
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = SolarAccent)) { append("BACKSPACE (....-)") }
                                append(" - Delete last typed character")
                            },
                            color = SolarMuted,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "* Blink custom Morse abbreviation codes to execute instant speech & controls.",
                        color = SolarMuted.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SolarKeyBg)
                            .border(1.dp, SolarAccent, RoundedCornerShape(12.dp))
                            .clickable { onClose() }
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "CLOSE",
                            color = SolarAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
