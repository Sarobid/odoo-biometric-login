package com.example.viseopos.ui.screen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
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
import androidx.navigation.NavHostController
import com.example.viseopos.ui.navigation.AppDestinations
import com.example.viseopos.utils.WebOdooUtils

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebOdooScreen(
    navController: NavHostController,
    modifier: Modifier
) {
    val url = stringResource(R.string.website_url)
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Client pour gérer les événements de la page
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            hasError = false
                            errorMessage = null
                            Log.d("WebOdooScreen", "Page started loading: $url")
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            Log.d("WebOdooScreen", "Page finished loading: $url")
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                isLoading = false
                                hasError = true
                                val errorDesc = "Error ${error?.errorCode}: ${error?.description}"
                                errorMessage = errorDesc
                                Log.e("WebOdooScreen", errorDesc)
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val requestedUrl = request?.url?.toString()
                            if (requestedUrl != null) {
                                Log.d("WebOdooScreen", "Loading new URL in WebView: $requestedUrl")
                                if (WebOdooUtils.isDeconnected(requestedUrl)){
                                    navController.navigate(AppDestinations.HOME_SCREEN_ROUTE)
                                }else{
                                    view?.loadUrl(requestedUrl)
                                }
                                return true // Nous avons géré l'URL
                            }
                            return false // Laisser le système gérer
                        }
                    }

                    // Activer JavaScript (souvent nécessaire pour les sites modernes comme Odoo)
                    settings.javaScriptEnabled = true

                    // Autres paramètres utiles pour la compatibilité
                    settings.domStorageEnabled = true
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