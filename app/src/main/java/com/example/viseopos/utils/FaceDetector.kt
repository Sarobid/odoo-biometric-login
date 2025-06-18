package com.example.viseopos.utils

import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

class FaceDetector {
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setMinFaceSize(50f)
        .build()
    private val detector = FaceDetection.getClient(options)

    fun detectFaces(image: InputImage,imageProxy: ImageProxy) {
        val result = detector.process(image)
            .addOnSuccessListener { faces ->
                Log.d("FaceDetector", "Faces detected: ${faces.size}")
            }
            .addOnFailureListener { e ->
                Log.e("FaceDetector", "Face detection failed", e)
            }
            .addOnCompleteListener { e->
                Log.d("FaceDetector", "Face detection complete")
                imageProxy.close()
            }
    }
    fun close() {
        detector.close()
    }

}