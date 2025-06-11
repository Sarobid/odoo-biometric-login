package com.example.viseopos.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.viseopos.ui.components.AuthorizeCamera
import com.example.viseopos.ui.components.CameraPreviewContent
import com.example.viseopos.utils.FaceAnalyzer

@Composable
fun FacialRecognitionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Log.e("FacialRecognitionScreen", "Camera permission denied by user.")
            }
        }
    )
    LaunchedEffect(key1 = true) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                Log.d("FacialRecognitionScreen", "Camera permission already granted.")
                hasCameraPermission = true
            }
            else -> {
                Log.d("FacialRecognitionScreen", "Requesting camera permission.")
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreviewContent(navController=navController,lifecycleOwner = lifecycleOwner)
        } else {
            AuthorizeCamera(launcher = permissionLauncher)
        }
    }
}

