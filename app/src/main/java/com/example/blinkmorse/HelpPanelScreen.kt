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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
        contentAlignment = Alignment.BottomCenter,
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
                .padding(20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Quick Assist Frame
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SolarKeyBg)
                        .border(1.5.dp, SolarLine, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "ACTION MORSE COMMANDS",
                        color = SolarInk,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )

                    ActionCommandItem(
                        icon = "🚨",
                        name = "Emergency Siren",
                        morse = "---...",
                        description = "Play siren alarm & flash screen",
                    )
                    ActionCommandItem(
                        icon = "🎵",
                        name = "Music Playlist",
                        morse = "----..",
                        description = "Play synth audio melody track",
                    )
                    ActionCommandItem(
                        icon = "🔦",
                        name = "Torch / Flashlight",
                        morse = ".-.-.-",
                        description = "Toggle camera LED flashlight",
                    )
                    ActionCommandItem(
                        icon = "💬",
                        name = "Quick SOS Text",
                        morse = "-.-.-",
                        description = "Insert emergency help sentence",
                    )
                    ActionCommandItem(
                        icon = "🔋",
                        name = "System Status",
                        morse = "-...-",
                        description = "Speak current battery & time",
                    )
                    ActionCommandItem(
                        icon = "🧹",
                        name = "Clear Board",
                        morse = "....-",
                        description = "Erase all typed text from screen",
                    )

                    Text(
                        text = "* Blink custom action Morse codes to execute instant controls.",
                        color = SolarMuted.copy(alpha = 0.85f),
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

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
fun ActionCommandItem(icon: String, name: String, morse: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = SolarLine.copy(alpha = 0.4f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$icon $name",
                color = SolarInk,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                color = SolarMuted,
                fontSize = 9.sp,
                lineHeight = 11.sp,
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        MorseVisualizer(morseCode = morse, dotColor = SolarAccent2, dashColor = SolarAccent)
    }
}
