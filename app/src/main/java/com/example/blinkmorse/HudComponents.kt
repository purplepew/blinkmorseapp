package com.example.blinkmorse

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Theme & Colors (Solar Arcade) ---
val SolarBg = Color(0xFFF5E8CB)
val SolarInk = Color(0xFF3A2419)
val SolarMuted = Color(0xFF8E6B4C)
val SolarLine = Color(0xFFD5BB94)
val SolarKeyBg = Color(0x73FFFFFF) // ~45% white for translucent surfaces
val SolarDisplayBg = Color(0xFFF1D7A5)
val SolarAccent = Color(0xFFE85C37) // Orange-red (Active/Dash)
val SolarAccent2 = Color(0xFFEFA832) // Golden-yellow (Dot)

// --- Legacy Themes (Pending Migration) ---
val HudBg = Color(0xFF030A16)
val HudCardBg = Color(0xCC061426)
val HudAvatarBg = Color(0xFF081B30)
val HudCyan = Color(0xFF00E5FF)
val HudGreen = Color(0xFF00FF88)
val HudGreenDark = Color(0xCC07241C)
val Slate950 = Color(0xFF020617)
val HudRed = Color(0xFFFF2D55)
val HudRedDark = Color(0xCC280911)
val HudCardBgDark = Color(0xCC051324)

val HudMenuBoxBg = Color(0xF205152A)
val HudMenuBtnBg = Color(0xFF071D37)

@Composable
fun CalibrationTask(label: String, progress: String, isChecked: Boolean, labelColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = SolarInk,
                checkmarkColor = SolarBg,
                uncheckedColor = SolarInk,
            ),
            modifier = Modifier
                .size(16.dp)
                .border(1.dp, SolarLine, RoundedCornerShape(4.dp)),
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = labelColor, fontWeight = FontWeight.SemiBold)) {
                    append("$label ")
                }
                withStyle(style = SpanStyle(color = SolarLine, fontFamily = FontFamily.Monospace)) {
                    append(progress)
                }
            },
            fontSize = 11.sp,
            lineHeight = 14.sp,
        )
    }
}

@Composable
fun SimulatedAvatarSvg() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        drawCircle(
            color = Color(0xFFFDD8B5),
            radius = w * 0.26f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
        
        val bangsPath = Path().apply {
            moveTo(w * 0.28f, h * 0.38f)
            quadraticTo(w * 0.5f, h * 0.22f, w * 0.72f, h * 0.38f)
            quadraticTo(w * 0.62f, h * 0.32f, w * 0.5f, h * 0.32f)
            quadraticTo(w * 0.38f, h * 0.32f, w * 0.28f, h * 0.38f)
        }
        drawPath(bangsPath, color = Color(0xFF1E1822))

        val eyePath1 = Path().apply {
            moveTo(w * 0.38f, h * 0.48f)
            quadraticTo(w * 0.42f, h * 0.44f, w * 0.46f, h * 0.48f)
        }
        val eyePath2 = Path().apply {
            moveTo(w * 0.54f, h * 0.48f)
            quadraticTo(w * 0.58f, h * 0.44f, w * 0.62f, h * 0.48f)
        }
        drawPath(eyePath1, color = Color(0xFF333333), style = Stroke(width = 4f))
        drawPath(eyePath2, color = Color(0xFF333333), style = Stroke(width = 4f))

        val smilePath = Path().apply {
            moveTo(w * 0.44f, h * 0.61f)
            quadraticTo(w * 0.5f, h * 0.66f, w * 0.56f, h * 0.61f)
        }
        drawPath(smilePath, color = Color(0xFFE06666), style = Stroke(width = 3f))

        val suitPath = Path().apply {
            moveTo(w * 0.28f, h * 0.78f)
            lineTo(w * 0.5f, h * 0.88f)
            lineTo(w * 0.72f, h * 0.78f)
            lineTo(w * 0.78f, h * 1.0f)
            lineTo(w * 0.22f, h * 1.0f)
            close()
        }
        drawPath(suitPath, color = Color(0xFF2D3748))
    }
}

fun Modifier.drawCyberGrid(gridColor: Color = SolarLine.copy(alpha = 0.5f)) = this.drawBehind {
    val gridSize = 32.dp.toPx() // Slightly wider grid spacing for blueprint feel
    
    var x = 0f
    while (x < size.width) {
        drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
        x += gridSize
    }
    
    var y = 0f
    while (y < size.height) {
        drawLine(color = gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
        y += gridSize
    }
}

@Suppress("unused")
@Composable
fun CornerAccents(color: Color, length: Float, strokeWidth: Float, offset: Float = -4f) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        drawLine(color, Offset(offset, offset), Offset(length + offset, offset), strokeWidth)
        drawLine(color, Offset(offset, offset), Offset(offset, length + offset), strokeWidth)
        drawLine(color, Offset(w - offset, offset), Offset(w - length - offset, offset), strokeWidth)
        drawLine(color, Offset(w - offset, offset), Offset(w - offset, length + offset), strokeWidth)
        drawLine(color, Offset(offset, h - offset), Offset(length + offset, h - offset), strokeWidth)
        drawLine(color, Offset(offset, h - offset), Offset(offset, h - length - offset), strokeWidth)
        drawLine(color, Offset(w - offset, h - offset), Offset(w - length - offset, h - offset), strokeWidth)
        drawLine(color, Offset(w - offset, h - offset), Offset(w - offset, h - length - offset), strokeWidth)
    }
}

@Composable
fun MorseVisualizer(
    morseCode: String, 
    dotColor: Color = SolarAccent2,
    dashColor: Color = SolarAccent,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        morseCode.forEach { char ->
            if (char == '.') {
                Box(
                    modifier = Modifier
                        .size(6.dp) 
                        .clip(CircleShape)
                        .background(dotColor),
                )
            } else if (char == '-') {
                Box(
                    modifier = Modifier
                        .width(14.dp) 
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(dashColor),
                )
            }
        }
    }
}

@Composable
fun LargeMorseVisualizer(
    morseCode: String,
    modifier: Modifier = Modifier,
    dotColor: Color = SolarAccent2,
    dashColor: Color = SolarAccent,
    maxSymbols: Int = 10
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        morseCode.take(maxSymbols).forEach { char ->
            if (char == '.') {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            } else if (char == '-') {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(dashColor)
                )
            }
        }
    }
}

@Composable
fun MorseCheatSheetGrid() {
    val morseMap = listOf(
        "A" to ".-", "B" to "-...", "C" to "-.-.", "D" to "-..", "E" to ".", "F" to "..-.",
        "G" to "--.", "H" to "....", "I" to "..", "J" to ".---", "K" to "-.-", "L" to ".-..",
        "M" to "--", "N" to "-.", "O" to "---", "P" to ".--.", "Q" to "--.-", "R" to ".-.",
        "S" to "...", "T" to "-", "U" to "..-", "V" to "...-", "W" to ".--", "X" to "-..-",
        "Y" to "-.--", "Z" to "--.."
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        morseMap.chunked(5).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowItems.forEach { (letter, morse) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SolarKeyBg)
                            .border(1.5.dp, SolarLine, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = letter,
                                color = SolarInk, 
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            MorseVisualizer(morseCode = morse)
                        }
                    }
                }
                
                if (rowItems.size < 5) {
                    repeat(5 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpecialSignal("SPEAK", ".-.-", modifier = Modifier.weight(1f))
            SpecialSignal("SPACE", "..--", modifier = Modifier.weight(1f))
            SpecialSignal("CLEAR", "....-", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SpecialSignal(label: String, morse: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SolarKeyBg)
            .border(1.5.dp, SolarLine, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label, 
                color = SolarInk,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Spacer(modifier = Modifier.height(2.dp))
            MorseVisualizer(morseCode = morse) 
        }
    }
}

@Suppress("unused")
@Composable
fun ActiveAvatarSvg() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        drawCircle(
            color = Color(0xFFFDD8B5),
            radius = w * 0.26f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
        
        val bangsPath = Path().apply {
            moveTo(w * 0.28f, h * 0.38f)
            quadraticTo(w * 0.5f, h * 0.22f, w * 0.72f, h * 0.38f)
            quadraticTo(w * 0.62f, h * 0.32f, w * 0.5f, h * 0.32f)
            quadraticTo(w * 0.38f, h * 0.32f, w * 0.28f, h * 0.38f)
        }
        drawPath(bangsPath, color = Color(0xFF1E1822))

        drawOval(
            color = Color(0xFF222222),
            topLeft = Offset((w * 0.42f) - (w * 0.04f), (h * 0.48f) - (h * 0.025f)),
            size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.05f)
        )
        drawOval(
            color = Color(0xFF222222),
            topLeft = Offset((w * 0.58f) - (w * 0.04f), (h * 0.48f) - (h * 0.025f)),
            size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.05f)
        )

        val smilePath = Path().apply {
            moveTo(w * 0.44f, h * 0.62f)
            quadraticTo(w * 0.5f, h * 0.67f, w * 0.56f, h * 0.62f)
        }
        drawPath(smilePath, color = Color(0xFFE06666), style = Stroke(width = 2f))

        val suitPath = Path().apply {
            moveTo(w * 0.28f, h * 0.78f)
            lineTo(w * 0.5f, h * 0.88f)
            lineTo(w * 0.72f, h * 0.78f)
            lineTo(w * 0.78f, h * 1.0f)
            lineTo(w * 0.22f, h * 1.0f)
            close()
        }
        drawPath(suitPath, color = Color(0xFF2D3748))
    }
}

@Composable
fun DimmedTopHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .alpha(0.2f),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(0.66f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(SolarKeyBg)
                .border(1.5.dp, SolarLine, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            MorseCheatSheetGrid()
        }

        Box(
            modifier = Modifier
                .weight(0.34f)
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SolarDisplayBg)
                .border(2.dp, SolarLine.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
        )
    }
}

@Suppress("unused")
@Composable
fun ExcalidrawStyleTextBox(
    text: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 120.dp,
    textColor: Color = SolarInk,
    borderColor: Color = SolarLine,
    backgroundColor: Color = SolarKeyBg,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .wrapContentHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .drawCyberGrid()
            .border(1.5.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            letterSpacing = 1.5.sp,
            lineHeight = 32.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Suppress("unused")
@Composable
fun AutoScalingText(
    text: String,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 32.sp,
    minFontSize: TextUnit = 14.sp,
    textColor: Color = SolarInk,
) {
    var currentFontSize by remember(text) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        color = textColor,
        fontSize = currentFontSize,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        letterSpacing = 1.5.sp,
        lineHeight = (currentFontSize.value * 1.25f).sp,
        softWrap = true,
        overflow = TextOverflow.Clip,
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent {
                if (readyToDraw) {
                    drawContent()
                }
            },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && (currentFontSize > minFontSize)) {
                currentFontSize = (currentFontSize.value * 0.9f).sp
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
fun AutoScalingHolographicTextBox(
    text: String,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 32.sp,
    minFontSize: TextUnit = 12.sp,
    textColor: Color = SolarInk,
    borderColor: Color = SolarLine,
    backgroundColor: Color = SolarDisplayBg,
) {
    var currentFontSize by remember(text) { mutableStateOf(maxFontSize) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .drawCyberGrid()
            .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = currentFontSize,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            letterSpacing = 1.2.sp,
            lineHeight = (currentFontSize.value * 1.25f).sp,
            softWrap = true,
            overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.hasVisualOverflow && currentFontSize > minFontSize) {
                    currentFontSize = (currentFontSize.value * 0.9f).sp
                }
            }
        )
    }
}
