package com.example.viseopos.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity // Ajouté
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.viseopos.ui.viewModel.CameraPreviewViewModel
import com.example.viseopos.utils.CameraPreviewUtils
import com.google.common.util.concurrent.ListenableFuture

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CameraPreviewContent(navController: NavHostController,
                         lifecycleOwner: LifecycleOwner,
                         viewModelCamera: CameraPreviewViewModel = viewModel(),
                         cameraUtils : CameraPreviewUtils = CameraPreviewUtils()
) {
    val context = LocalContext.current
    val cameraProviderFutureFromState by viewModelCamera.cameraProviderFuture.collectAsState()

    LaunchedEffect(context) {
        viewModelCamera.initialisation(context)
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()){
        AndroidView(
            factory = { ctx ->
                val previewView = cameraUtils.createConfiguredPreviewView(ctx)
                previewView
            },
            update = { previewView ->
                cameraProviderFutureFromState?.let { cameraProviderFuture ->
                    viewModelCamera.configurationCamera(lifecycleOwner, previewView, cameraProviderFuture)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        FacePositionIndicator()
    }
}