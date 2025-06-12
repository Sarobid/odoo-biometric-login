// Dans CameraPreviewContent.kt
package com.example.viseopos.ui.components

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.BoxWithConstraints // Modifié
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset // Ajouté
import androidx.compose.ui.geometry.Rect // Ajouté
import androidx.compose.ui.geometry.Size // Modifié
import androidx.compose.ui.graphics.Color // Ajouté
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity // Ajouté
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.example.viseopos.utils.FaceAnalyzer
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.face.Face
import java.util.concurrent.Executors
import kotlin.math.min // Ajouté

@Composable
fun CameraPreviewContent(navController: NavHostController, lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
        remember { ProcessCameraProvider.getInstance(context) }
    var detectedFaces by remember { mutableStateOf<List<Face>>(emptyList()) }
    var imageAnalysisSize by remember { mutableStateOf<Size?>(null) }
    var isCameraReady by remember { mutableStateOf(false) }
    var isFaceInViewfinder by remember { mutableStateOf(false) }

    val faceAnalyzer = remember {
        FaceAnalyzer { faces, width, height ->
            detectedFaces = faces
            imageAnalysisSize = Size(width.toFloat(), height.toFloat())
            if (faces.isNotEmpty()) {
                Log.d("CameraPreviewContent", "Faces detected: ${faces.size}, Image: $width x $height")
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            faceAnalyzer.stop()
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val overlayWidthPx = with(density) { maxWidth.toPx() }
        val overlayHeightPx = with(density) { maxHeight.toPx() }
        val staticViewfinderRectPx: Rect = remember(overlayWidthPx, overlayHeightPx) {
            val minOverlayDimension = min(overlayWidthPx, overlayHeightPx)
            val viewfinderSizePx = Size(minOverlayDimension * 0.7f, minOverlayDimension * 0.7f)
            val topLeftOffsetPx = Offset(
                (overlayWidthPx - viewfinderSizePx.width) / 2,
                (overlayHeightPx - viewfinderSizePx.height) / 2
            )
            Rect(topLeftOffsetPx, viewfinderSizePx)
        }

        val currentImageSize = imageAnalysisSize
        isFaceInViewfinder = remember(detectedFaces, currentImageSize, staticViewfinderRectPx, overlayWidthPx, overlayHeightPx) {
            if (detectedFaces.isEmpty() || currentImageSize == null || currentImageSize.width == 0f || currentImageSize.height == 0f) {
                false
            } else {
                val scaleX = overlayWidthPx / currentImageSize.width
                val scaleY = overlayHeightPx / currentImageSize.height
                val isFrontCamera = true
                detectedFaces.any { face ->
                    val boundingBox = face.boundingBox
                    var faceCenterX = boundingBox.centerX() * scaleX
                    val faceCenterY = boundingBox.centerY() * scaleY

                    if (isFrontCamera) {
                        faceCenterX = overlayWidthPx - faceCenterX
                    }
                    staticViewfinderRectPx.contains(Offset(faceCenterX, faceCenterY))
                }
            }
        }
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val previewUseCase = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build()
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().also {
                            it.setAnalyzer(Executors.newSingleThreadExecutor(), faceAnalyzer)
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, previewUseCase, imageAnalysis
                        )
                        isCameraReady = true
                        Log.d("CameraPreviewContent", "Camera bound.")
                    } catch (exc: Exception) {
                        isCameraReady = false
                        Log.e("CameraPreviewContent", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        if (isCameraReady && currentImageSize != null && detectedFaces.isNotEmpty() && !isFaceInViewfinder) {
            FaceOverlay(
                faces = detectedFaces,
                imageWidth = currentImageSize.width.toInt(),
                imageHeight = currentImageSize.height.toInt(),
                overlayWidth = this.maxWidth,
                overlayHeight = this.maxHeight,
                isFrontCamera = true
                // Vous pouvez utiliser le style scanner de l'étape précédente ici
                // scannerColor = if (isFaceInViewfinder) Color.Blue else Color.Red // Ou ne pas l'afficher du tout
            )
        }
        StaticScannerViewfinder(
            modifier = Modifier.fillMaxSize(),
            viewfinderColor = if (isFaceInViewfinder) Color.Blue else Color.White.copy(alpha = 0.7f)
        )

        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Quitter l'aperçu caméra")
        }
    }
}