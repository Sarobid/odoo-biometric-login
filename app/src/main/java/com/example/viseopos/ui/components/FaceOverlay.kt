package com.example.viseopos.ui.components // Ou où vous souhaitez le placer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.google.mlkit.vision.face.Face // Assurez-vous d'importer la bonne classe Face
import com.google.mlkit.vision.face.FaceLandmark

/**
 * Un composant qui dessine des rectangles autour des visages détectés.
 *
 * @param faces La liste des objets Face détectés.
 * @param imageWidth La largeur de l'image source (de la caméra) sur laquelle les visages ont été détectés.
 * @param imageHeight La hauteur de l'image source (de la caméra) sur laquelle les visages ont été détectés.
 * @param overlayWidth La largeur du composant Canvas sur lequel dessiner (généralement la largeur de la PreviewView).
 * @param overlayHeight La hauteur du composant Canvas sur lequel dessiner (généralement la hauteur de la PreviewView).
 * @param strokeWidth L'épaisseur du trait du rectangle.
 * @param faceBoxColor La couleur du rectangle.
 * @param isFrontCamera Indique si la caméra frontale est utilisée (pour la mise en miroir horizontale si nécessaire).
 */
@Composable
fun FaceOverlay(
    faces: List<Face>,
    imageWidth: Int,
    imageHeight: Int,
    overlayWidth: Dp,
    overlayHeight: Dp,
    strokeWidth: Float = 5f,
    faceBoxColor: Color = Color.Red,
    isFrontCamera: Boolean = true
) {
    val density = LocalDensity.current
    val overlayWidthPx = with(density) { overlayWidth.toPx() }
    val overlayHeightPx = with(density) { overlayHeight.toPx() }

    /*Canvas(modifier = Modifier.fillMaxSize()) {
        if (imageWidth == 0 || imageHeight == 0) {
            return@Canvas
        }
        val scaleX = overlayWidthPx / imageWidth.toFloat()
        val scaleY = overlayHeightPx / imageHeight.toFloat()

        faces.forEach { face ->
            val boundingBox = face.boundingBox
            var left = boundingBox.left * scaleX
            var top = boundingBox.top * scaleY
            var right = boundingBox.right * scaleX
            var bottom = boundingBox.bottom * scaleY


            if (isFrontCamera) {
                val mirroredLeft = overlayWidthPx - right
                val mirroredRight = overlayWidthPx - left
                left = mirroredLeft
                right = mirroredRight
            }
            drawRect(
                color = faceBoxColor,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = strokeWidth)
            )

            // Optionnel: Dessiner les points de repère (landmarks) si disponibles
             face.getLandmark(FaceLandmark.LEFT_EYE)?.position?.let { pos ->
                 val x = (if (isFrontCamera) imageWidth - pos.x else pos.x) * scaleX
                 val y = pos.y * scaleY
                 drawCircle(Color.Yellow, radius = 5f, center = Offset(x, y))
             }
        }
    }*/
}