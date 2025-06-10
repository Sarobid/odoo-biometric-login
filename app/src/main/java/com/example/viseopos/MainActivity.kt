package com.example.viseopos

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text // Keep if Greeting is used, otherwise remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // For isLoading example
import androidx.compose.runtime.mutableStateOf // For isLoading example
import androidx.compose.runtime.setValue // For isLoading example
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview // Keep if GreetingPreview is used
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // <-- IMPORT THIS
import androidx.core.util.remove
import androidx.lifecycle.lifecycleScope // For isLoading example
import com.example.viseopos.ui.navigation.AppNavHost
import com.example.viseopos.ui.theme.ViseoPosTheme
import kotlinx.coroutines.delay // For isLoading example
import kotlinx.coroutines.launch // For isLoading example
import kotlin.text.toFloat

class MainActivity : ComponentActivity() {
    private var isLoading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition { isLoading }
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashScreenView = splashScreenViewProvider.view
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.height.toFloat()
            ).apply {
                interpolator = AnticipateInterpolator()
                duration = 300L
                doOnEnd { splashScreenViewProvider.remove() }
            }
            slideUp.start()
        }
        lifecycleScope.launch {
            delay(1500)
            isLoading = false
        }
        setContent {
            ViseoPosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
