package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SalimOrange
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SalimAnalogClock(
    hour: Int,
    minute: Int,
    second: Int,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    showSeconds: Boolean = true
) {
    val dialColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val majorTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    val hourHandColor = MaterialTheme.colorScheme.onSurface
    val minuteHandColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    val secondHandColor = SalimOrange
    val pivotColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f - 8.dp.toPx()

            // Draw 12 dial ticks
            for (i in 0 until 12) {
                val angleRad = Math.toRadians((i * 30 - 90).toDouble())
                val isMajor = (i % 3 == 0)
                val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                val tickStroke = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                val tickColor = if (isMajor) majorTickColor else dialColor

                val startX = (center.x + (radius - tickLength) * cos(angleRad)).toFloat()
                val startY = (center.y + (radius - tickLength) * sin(angleRad)).toFloat()
                val endX = (center.x + radius * cos(angleRad)).toFloat()
                val endY = (center.y + radius * sin(angleRad)).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Round
                )
            }

            // Calculate hand angles
            val hourAngle = Math.toRadians(((hour % 12 + minute / 60f) * 30 - 90).toDouble())
            val minuteAngle = Math.toRadians(((minute + second / 60f) * 6 - 90).toDouble())
            val secondAngle = Math.toRadians((second * 6 - 90).toDouble())

            // Draw Hour Hand
            val hourLength = radius * 0.52f
            val hourEndX = (center.x + hourLength * cos(hourAngle)).toFloat()
            val hourEndY = (center.y + hourLength * sin(hourAngle)).toFloat()
            val hourTailX = (center.x - 12.dp.toPx() * cos(hourAngle)).toFloat()
            val hourTailY = (center.y - 12.dp.toPx() * sin(hourAngle)).toFloat()

            drawLine(
                color = hourHandColor,
                start = Offset(hourTailX, hourTailY),
                end = Offset(hourEndX, hourEndY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw Minute Hand
            val minLength = radius * 0.74f
            val minEndX = (center.x + minLength * cos(minuteAngle)).toFloat()
            val minEndY = (center.y + minLength * sin(minuteAngle)).toFloat()
            val minTailX = (center.x - 16.dp.toPx() * cos(minuteAngle)).toFloat()
            val minTailY = (center.y - 16.dp.toPx() * sin(minuteAngle)).toFloat()

            drawLine(
                color = minuteHandColor,
                start = Offset(minTailX, minTailY),
                end = Offset(minEndX, minEndY),
                strokeWidth = 2.5f.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw Second Hand if enabled
            if (showSeconds) {
                val secLength = radius * 0.86f
                val secEndX = (center.x + secLength * cos(secondAngle)).toFloat()
                val secEndY = (center.y + secLength * sin(secondAngle)).toFloat()
                val secTailX = (center.x - 20.dp.toPx() * cos(secondAngle)).toFloat()
                val secTailY = (center.y - 20.dp.toPx() * sin(secondAngle)).toFloat()

                drawLine(
                    color = secondHandColor,
                    start = Offset(secTailX, secTailY),
                    end = Offset(secEndX, secEndY),
                    strokeWidth = 1.5f.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Second hand center dot
                drawCircle(
                    color = secondHandColor,
                    radius = 3.5f.dp.toPx(),
                    center = center
                )
            }

            // Center pivot cap
            drawCircle(
                color = pivotColor,
                radius = 2.dp.toPx(),
                center = center
            )
        }
    }
}
