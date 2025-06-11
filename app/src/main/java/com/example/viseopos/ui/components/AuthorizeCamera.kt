package com.example.viseopos.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AuthorizeCamera(
    launcher: ActivityResultLauncher<String>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Permission caméra requise pour cette fonctionnalité.")
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                Text("Accorder la permission")
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun AuthorizeCameraPreview() {
    val fakeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    MaterialTheme {
        AuthorizeCamera(launcher = fakeLauncher)
    }
}