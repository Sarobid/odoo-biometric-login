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
    private val onResult: (faces: List<Face>, imageWidth: Int, imageHeight: Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setMinFaceSize(50f)
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
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val imageWidth: Int
            val imageHeight: Int

            if (rotationDegrees == 90 || rotationDegrees == 270) {
                imageWidth = imageProxy.height
                imageHeight = imageProxy.width
            } else {
                imageWidth = imageProxy.width
                imageHeight = imageProxy.height
            }
            detector.process(image)
                .addOnSuccessListener { detectedFaces ->
                    onResult(detectedFaces, imageWidth, imageHeight)
                    if (detectedFaces.isEmpty()) {
                        Log.d("FaceAnalyzer", "No faces detected. Image dimensions: $imageWidth x $imageHeight")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FaceAnalyzer", "Face detection failed.", e)
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