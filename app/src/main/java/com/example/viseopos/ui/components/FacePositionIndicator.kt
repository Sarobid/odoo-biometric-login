package com.example.viseopos.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind // This import seems unused
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke // Import Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FacePositionIndicator(
    modifier: Modifier = Modifier,
    frameRatio: Float = 0.7f,
    color: Color = Color.Cyan,
    showText: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = 4.dp.toPx() // Renamed for clarity
            val cornerLength = 32.dp.toPx()

            val viewfinderSize = Size(
                size.minDimension * frameRatio,
                size.minDimension * frameRatio * 1.2f
            )
            val topLeft = Offset(
                (size.width - viewfinderSize.width) / 2,
                (size.height - viewfinderSize.height) / 2.5f
            )
            val rect = Rect(topLeft, viewfinderSize)

            // Masquage autour du rectangle (effet HUD)
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = size,
                blendMode = BlendMode.SrcOver
            )
            drawRect(
                color = Color.Transparent,
                topLeft = rect.topLeft,
                size = viewfinderSize,
                blendMode = BlendMode.Clear
            )

            // Define these properties once
            val lineBrush = SolidColor(color.copy(alpha = glowAlpha))
            val lineStroke = Stroke(
                width = strokeWidthPx,
                pathEffect = PathEffect.cornerPathEffect(8.dp.toPx())
            )

            // Coins lumineux futuristes
            fun drawCorner(start: Offset, end: Offset) {
                drawLine(
                    brush = lineBrush,
                    start = start,
                    end = end,
                    strokeWidth = lineStroke.width, // Use the width from lineStroke
                    pathEffect = lineStroke.pathEffect // Use the pathEffect from lineStroke
                )
            }

            // top-left
            drawCorner(Offset(rect.left, rect.top + cornerLength), Offset(rect.left, rect.top))
            drawCorner(Offset(rect.left, rect.top), Offset(rect.left + cornerLength, rect.top))
            // top-right
            drawCorner(Offset(rect.right - cornerLength, rect.top), Offset(rect.right, rect.top))
            drawCorner(Offset(rect.right, rect.top), Offset(rect.right, rect.top + cornerLength))
            // bottom-left
            drawCorner(Offset(rect.left, rect.bottom - cornerLength), Offset(rect.left, rect.bottom))
            drawCorner(Offset(rect.left, rect.bottom), Offset(rect.left + cornerLength, rect.bottom))
            // bottom-right
            drawCorner(Offset(rect.right - cornerLength, rect.bottom), Offset(rect.right, rect.bottom))
            drawCorner(Offset(rect.right, rect.bottom), Offset(rect.right, rect.bottom - cornerLength))
        }

        if (showText) {
            Text(
                text = "Placez votre visage dans le cadre",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            )
        }
    }
}