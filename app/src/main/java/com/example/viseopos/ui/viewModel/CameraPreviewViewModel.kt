package com.example.viseopos.ui.viewModel

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executor

class CameraPreviewViewModel(
) : ViewModel() {
    private val _cameraProviderFuture = MutableStateFlow<ListenableFuture<ProcessCameraProvider>?>(null)
    val cameraProviderFuture: StateFlow<ListenableFuture<ProcessCameraProvider>?> = _cameraProviderFuture

    //demander un CameraProvider
    fun requestCameraProvider(context: Context){
        _cameraProviderFuture.value = ProcessCameraProvider.getInstance(context)
    }
}