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
    isFrontCamera: Boolean = true // Important pour la mise en miroir des coordonnées
) {
    val density = LocalDensity.current

    // Convertir les dimensions de l'overlay Dp en Px pour le calcul
    val overlayWidthPx = with(density) { overlayWidth.toPx() }
    val overlayHeightPx = with(density) { overlayHeight.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (imageWidth == 0 || imageHeight == 0) {
            // Éviter la division par zéro si les dimensions de l'image ne sont pas encore prêtes
            return@Canvas
        }

        // Calculer les facteurs d'échelle pour convertir les coordonnées de l'image
        // de la caméra aux coordonnées de l'overlay (Canvas).
        // L'aperçu de la caméra peut être mis à l'échelle (scaleType FIT_CENTER, FILL_CENTER, etc.)
        // et peut avoir un ratio d'aspect différent de celui de l'image d'analyse.

        val scaleX = overlayWidthPx / imageWidth.toFloat()
        val scaleY = overlayHeightPx / imageHeight.toFloat()

        // Déterminer le facteur d'échelle à utiliser. Cela dépend de la façon dont
        // PreviewView affiche l'image (ScaleType).
        // Pour ScaleType.FILL_CENTER (le plus courant avec CameraX pour remplir la vue),
        // l'image est recadrée pour remplir la vue tout en maintenant le ratio d'aspect.
        // L'image d'analyse (provenant d'ImageAnalysis) a généralement ses propres dimensions.

        // Scénario simplifié : si l'analyse est faite sur l'image ayant les mêmes dimensions que
        // ce que `PreviewView` essaierait d'afficher sans recadrage.
        // Une approche plus robuste nécessiterait de connaître le ScaleType exact
        // et les transformations appliquées par PreviewView.

        // Pour cet exemple, nous allons supposer que l'imageWidth/imageHeight sont les dimensions
        // de l'image sur laquelle l'analyse a été faite, et que nous devons mapper cela
        // à la taille de notre overlay.

        faces.forEach { face ->
            val boundingBox = face.boundingBox // android.graphics.Rect

            // Convertir les coordonnées du Rect de ML Kit aux coordonnées du Canvas.
            // Le Rect de ML Kit est dans le système de coordonnées de l'image analysée.

            var left = boundingBox.left * scaleX
            var top = boundingBox.top * scaleY
            var right = boundingBox.right * scaleX
            var bottom = boundingBox.bottom * scaleY

            // Gestion de la caméra frontale : les coordonnées x sont généralement inversées (miroir).
            if (isFrontCamera) {
                val mirroredLeft = overlayWidthPx - right
                val mirroredRight = overlayWidthPx - left
                left = mirroredLeft
                right = mirroredRight
            }

            // Dessiner le rectangle
            drawRect(
                color = faceBoxColor,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = strokeWidth)
            )

            // Optionnel: Dessiner les points de repère (landmarks) si disponibles
            // face.getLandmark(FaceLandmark.LEFT_EYE)?.position?.let { pos ->
            //     val x = (if (isFrontCamera) imageWidth - pos.x else pos.x) * scaleX
            //     val y = pos.y * scaleY
            //     drawCircle(Color.Yellow, radius = 5f, center = Offset(x, y))
            // }
        }
    }
}