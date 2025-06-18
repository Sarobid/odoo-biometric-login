package com.example.viseopos.ui.viewModel

import android.content.Context
import android.util.Log
// import android.view.ViewGroup // Semble inutilisé
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
// import androidx.compose.ui.platform.LocalContext // Semble inutilisé ici directement
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.viseopos.utils.ImageAnalysisConfigurator
import com.example.viseopos.utils.VisageAnalyzer
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ExecutorService // Changé en ExecutorService pour pouvoir le fermer
import java.util.concurrent.Executors

class CameraPreviewViewModel : ViewModel() {
    private val _cameraProviderFuture = MutableStateFlow<ListenableFuture<ProcessCameraProvider>?>(null)
    val cameraProviderFuture: StateFlow<ListenableFuture<ProcessCameraProvider>?> = _cameraProviderFuture

    private val _cameraActive = MutableStateFlow(false)
    val cameraActive: StateFlow<Boolean> = _cameraActive

    private var visageAnalyzer: VisageAnalyzer? = null
    private val imageAnalysisConfigurator = ImageAnalysisConfigurator()
    private val imageAnalysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun initialisation(context: Context) {
        visageAnalyzer = VisageAnalyzer()
        requestCameraProvider(context)
    }
    fun getVisageAnalyzer(): VisageAnalyzer? {
        if (visageAnalyzer == null) {
            visageAnalyzer = VisageAnalyzer()
        }
        return visageAnalyzer
    }

    fun requestCameraProvider(context: Context) {
        _cameraProviderFuture.value = ProcessCameraProvider.getInstance(context)
    }

    fun configurationCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        cameraProviderFutureInstance: ListenableFuture<ProcessCameraProvider>
    ) {
        val currentVisageAnalyzer = getVisageAnalyzer()
        if (currentVisageAnalyzer == null) {
            _cameraActive.value = false
            return
        }

        cameraProviderFutureInstance.addListener({
            val cameraProvider = cameraProviderFutureInstance.get()
            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            val imageAnalysisUseCase = imageAnalysisConfigurator.build(
                imageAnalysisExecutor,
                currentVisageAnalyzer
            )
            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysisUseCase
                )
                _cameraActive.value = true
                Log.d("CameraPreview", "Camera bound successfully with Preview and ImageAnalysis.")
            } catch (exc: Exception) {
                _cameraActive.value = false
                Log.e("CameraPreview", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(previewView.context))
    }

    override fun onCleared() {
        super.onCleared()
        if (!imageAnalysisExecutor.isShutdown) {
            imageAnalysisExecutor.shutdown()
            Log.d("CameraPreview", "ImageAnalysisExecutor shut down.")
        }
    }
}