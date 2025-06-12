// Dans StaticScannerViewfinder.kt
package com.example.viseopos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color // Importation correcte
import androidx.compose.ui.unit.dp

@Composable
fun StaticScannerViewfinder(
    modifier: Modifier = Modifier,
    viewfinderColor: Color = Color.White.copy(alpha = 0.7f)
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 5.dp.toPx()
        val color = viewfinderColor
        val length = 30.dp.toPx()

        val viewfinderSize =
            Size(size.minDimension * 0.7f, size.minDimension * 0.7f)
        val topLeft = Offset(
            (size.width - viewfinderSize.width) / 2,
            (size.height - viewfinderSize.height) / 2
        )
        val rect = Rect(topLeft, viewfinderSize)

        // Haut-gauche
        drawLine(color, Offset(rect.left, rect.top + length), Offset(rect.left, rect.top), strokeWidth)
        drawLine(color, Offset(rect.left, rect.top), Offset(rect.left + length, rect.top), strokeWidth)
        // Haut-droit
        drawLine(color, Offset(rect.right - length, rect.top), Offset(rect.right, rect.top), strokeWidth)
        drawLine(color, Offset(rect.right, rect.top), Offset(rect.right, rect.top + length), strokeWidth)
        // Bas-gauche
        drawLine(color, Offset(rect.left, rect.bottom - length), Offset(rect.left, rect.bottom), strokeWidth)
        drawLine(color, Offset(rect.left, rect.bottom), Offset(rect.left + length, rect.bottom), strokeWidth)
        // Bas-droit
        drawLine(color, Offset(rect.right - length, rect.bottom), Offset(rect.right, rect.bottom), strokeWidth)
        drawLine(color, Offset(rect.right, rect.bottom), Offset(rect.right, rect.bottom - length), strokeWidth)
    }
}