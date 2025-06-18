package com.example.viseopos.utils

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.view.PreviewView

class CameraPreviewUtils {

     fun createConfiguredPreviewView(context: Context): PreviewView {
        Log.d("CameraViewUtils", "Creating and configuring PreviewView.")
        return PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

}