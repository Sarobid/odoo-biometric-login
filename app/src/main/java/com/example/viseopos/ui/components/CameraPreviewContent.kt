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
import androidx.compose.runtime.getValue // Modifié
import androidx.compose.runtime.mutableStateOf // Modifié
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue // Modifié
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size // Modifié
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.example.viseopos.utils.FaceAnalyzer // Assurez-vous que c'est le bon import
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.face.Face // Modifié
import java.util.concurrent.Executors

@Composable
fun CameraPreviewContent(navController: NavHostController, lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
        remember { ProcessCameraProvider.getInstance(context) }

    // États pour stocker les visages détectés et les dimensions de l'image d'analyse
    var detectedFaces by remember { mutableStateOf<List<Face>>(emptyList()) }
    var imageAnalysisSize by remember { mutableStateOf<Size?>(null) }
    var isCameraReady by remember { mutableStateOf(false) } // Pour savoir quand afficher l'overlay

    val faceAnalyzer = remember {
        FaceAnalyzer { faces, width, height -> // Mise à jour de la lambda pour accepter width et height
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
            // Potentiellement, délier la caméra aussi si cameraProviderFuture est complété
            // cameraProviderFuture.get()?.unbindAll() // Attention au blocage et aux exceptions
        }
    }

    // BoxWithConstraints pour obtenir les dimensions de la zone d'aperçu pour l'overlay
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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
                    val previewUseCase = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build()

                    // Définir une résolution cible pour ImageAnalysis si nécessaire
                    // Cela aide à connaître les dimensions de l'image analysée.
                    val imageAnalysis = ImageAnalysis.Builder()
                        // .setTargetResolution(android.util.Size(640, 480)) // Optionnel, mais peut aider
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(Executors.newSingleThreadExecutor(), faceAnalyzer)
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            previewUseCase,
                            imageAnalysis // S'assurer que imageAnalysis est lié
                        )
                        isCameraReady = true // La caméra est liée et prête
                        Log.d("CameraPreviewContent", "Camera bound with Preview and ImageAnalysis.")
                    } catch (exc: Exception) {
                        isCameraReady = false
                        Log.e("CameraPreviewContent", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Afficher FaceOverlay par-dessus l'AndroidView
        if (isCameraReady && imageAnalysisSize != null && detectedFaces.isNotEmpty()) {
            val currentImageSize = imageAnalysisSize // Copie pour la stabilité dans le lambda
            if (currentImageSize != null) {
                FaceOverlay(
                    faces = detectedFaces,
                    imageWidth = currentImageSize.width.toInt(),
                    imageHeight = currentImageSize.height.toInt(),
                    overlayWidth = this.maxWidth, // Largeur du BoxWithConstraints
                    overlayHeight = this.maxHeight, // Hauteur du BoxWithConstraints
                    isFrontCamera = true // Ajustez si nécessaire (par ex., basé sur CameraSelector)
                )
            }
        }

        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp), // Padding pour le FAB lui-même, pas pour le Box principal
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Quitter l'aperçu caméra")
        }
    }
}