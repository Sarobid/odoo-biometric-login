package com.example.viseopos.ui.screen

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.viseopos.R
import com.example.viseopos.ui.navigation.AppDestinations
import com.example.viseopos.ui.webView.MyWebViewClient
import com.example.viseopos.ui.webView.WebAppInterface
import com.example.viseopos.viewmodel.ManageWarehouseViewModel
import com.example.viseopos.viewmodel.ManageWarehouseViewModelFactory

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebOdooScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    token: String,
    hostname: String,
    dbName: String,
    manageWarehouseViewModel: ManageWarehouseViewModel = viewModel(
        factory = ManageWarehouseViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val initialLoadUrl = "${hostname.trimEnd('/')}?db=$dbName"
    val odooSessionUrlWithToken = "${hostname.trimEnd('/')}${stringResource(R.string.endpoint_url_connect)}$token&dbname=$dbName"
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var webViewInstance: WebView? by remember { mutableStateOf(null) }
    val webViewWarehouses = manageWarehouseViewModel.warehouses

    val myWebViewClient = remember(navController, odooSessionUrlWithToken) {
        MyWebViewClient(
            isLoadingLambda = { isLoading = it },
            hasErrorLambda = {
                hasError = it
                if (!it) errorMessage = null
            },
            errorMessageLambda = { errorMessage = it },
            onDeconnectedNavigate = {
                navController.navigate(AppDestinations.HOME_SCREEN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            },
            urlToLoadAfterInitialDeconnected = odooSessionUrlWithToken
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                Log.d("WebOdooScreen", "AndroidView factory: Creating WebView instance.")
                WebView(context).apply {
                    webViewInstance = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    this.webViewClient = myWebViewClient

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.                                                                                                                            databaseEnabled = true
                    settings.allowFileAccess = true
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    Log.d("WebOdooScreen", "WebView cacheMode set to LOAD_DEFAULT.")

                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    WebView.setWebContentsDebuggingEnabled(true)
                    addJavascriptInterface(
                        WebAppInterface(context, webViewWarehouses),
                        "AndroidInterface"
                    )

                    Log.d("WebOdooScreen", "Attempting to load initial URL in factory: $initialLoadUrl")
                    loadUrl(initialLoadUrl)
                }
            },
            update = { webView ->
                Log.d("WebOdooScreen", "AndroidView update block. Ensuring WebView client is current.")
                webView.webViewClient = myWebViewClient
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (hasError && errorMessage != null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)) {
                Text(
                    text = "Failed to load page.\nError: $errorMessage",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("WebOdooScreen", "WebOdooScreen Composable is being disposed.")
        }
    }
}