package com.example.viseopos.utils

import androidx.camera.core.ImageAnalysis
import java.util.concurrent.Executor

class ImageAnalysisConfigurator {
    fun build(executor: Executor, analyzer: ImageAnalysis.Analyzer): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor, analyzer)
            }
    }
}
