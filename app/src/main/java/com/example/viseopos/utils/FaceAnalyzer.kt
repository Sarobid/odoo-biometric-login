package com.example.viseopos.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(
    // MODIFIÉ : Le callback prend maintenant aussi la largeur et la hauteur de l'image
    private val onResult: (faces: List<Face>, imageWidth: Int, imageHeight: Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(options)
    private var isAnalyzing = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (isAnalyzing) {
            imageProxy.close()
            return
        }

        isAnalyzing = true
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // *** RÉCUPÉRER LES DIMENSIONS DE L'IMAGE ANALYSÉE ***
            // Les dimensions de l'ImageProxy sont celles de l'image avant toute rotation
            // appliquée pour l'analyse par ML Kit.
            // Si l'imageInfo.rotationDegrees est 90 ou 270, les largeur et hauteur effectives
            // pour ML Kit sont inversées.
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val imageWidth: Int
            val imageHeight: Int

            if (rotationDegrees == 90 || rotationDegrees == 270) {
                imageWidth = imageProxy.height // Hauteur devient largeur
                imageHeight = imageProxy.width  // Largeur devient hauteur
            } else {
                imageWidth = imageProxy.width
                imageHeight = imageProxy.height
            }
            // Alternative: Utiliser les dimensions de l'InputImage après sa création
            // val imageWidth = image.width
            // val imageHeight = image.height
            // Cependant, imageProxy.width/height sont souvent plus fiables pour ce qui est passé à l'analyseur.
            // Il est bon de vérifier ce qui fonctionne le mieux avec votre configuration de PreviewView.

            detector.process(image)
                .addOnSuccessListener { detectedFaces ->
                    // MODIFIÉ : Appeler le callback avec les faces ET les dimensions
                    // On appelle onResult même si faces est vide, pour que l'UI puisse se réinitialiser si besoin.
                    // Si vous voulez appeler onResult que s'il y a des visages, remettez la condition.
                    onResult(detectedFaces, imageWidth, imageHeight)

                    if (detectedFaces.isEmpty()) {
                        Log.d("FaceAnalyzer", "No faces detected. Image dimensions: $imageWidth x $imageHeight")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FaceAnalyzer", "Face detection failed.", e)
                    // Optionnel : remonter une liste vide en cas d'erreur pour nettoyer l'UI
                    // onResult(emptyList(), imageWidth, imageHeight)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                    isAnalyzing = false
                }
        } else {
            Log.d("FaceAnalyzer", "MediaImage is null")
            imageProxy.close()
            isAnalyzing = false
        }
    }

    fun stop() {
        Log.d("FaceAnalyzer", "Stopping face detector.")
        detector.close()
    }
}