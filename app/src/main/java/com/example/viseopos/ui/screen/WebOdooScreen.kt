package com.example.viseopos.ui.screen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import com.example.viseopos.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator // Ajouté
import androidx.compose.material3.Text // Ajouté
import androidx.compose.runtime.* // Ajouté pour by, remember, mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment // Ajouté
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.viseopos.ui.navigation.AppDestinations
import com.example.viseopos.ui.viewModel.OdooAuthViewModel
import com.example.viseopos.ui.webView.MyWebViewClient
import com.example.viseopos.utils.WebOdooUtils

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebOdooScreen(
    navController: NavHostController,
    modifier: Modifier,
    token: String,
    hostname: String
) {
    val url = hostname.trimEnd('/')+stringResource(R.string.endpoint_url_connect) + token
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val myWebViewClient = remember(navController) {
        MyWebViewClient(
            isLoadingLambda = { isLoading = it },
            hasErrorLambda = { hasError = it },
            errorMessageLambda = { errorMessage = it },
            onDeconnectedNavigate = {
                navController.navigate(AppDestinations.HOME_SCREEN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    this.clearCache(true)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = myWebViewClient
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true


                    Log.d("WebOdooScreen", "Attempting to load URL: $url")
                    loadUrl(url)
                }
            },
            update = { webView ->

            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (hasError && errorMessage != null) {
            Text(
                text = "Failed to load page.\n$errorMessage",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}