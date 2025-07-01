package com.example.viseopos.ui.webView

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.viseopos.utils.WebOdooUtils

class MyWebViewClient(
    private val isLoadingLambda: (Boolean) -> Unit,
    private val hasErrorLambda: (Boolean) -> Unit,
    private val errorMessageLambda: (String?) -> Unit,
    private val onDeconnectedNavigate: () -> Unit,
    private val urlToLoadAfterInitialDeconnected: String
) : WebViewClient() {

    private var connectionAttemptMadeWithTokenUrl = false

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        isLoadingLambda(true)
        hasErrorLambda(false)
        errorMessageLambda(null)
        Log.d("MyWebViewClient", "Page started loading: $url")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        isLoadingLambda(false)
        Log.d("MyWebViewClient", "Page finished loading: $url")

        if (url == urlToLoadAfterInitialDeconnected && !WebOdooUtils.isDeconnected(url.toString())) {
            Log.d("MyWebViewClient", "Successfully loaded or navigated to token URL: $url")
            if (!connectionAttemptMadeWithTokenUrl) {
                connectionAttemptMadeWithTokenUrl = true
            }
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            isLoadingLambda(false)
            hasErrorLambda(true)
            val errorDesc = "Error ${error?.errorCode}: ${error?.description} on URL ${request.url}"
            errorMessageLambda(errorDesc)
            Log.e("MyWebViewClient", errorDesc)
            if (request.url.toString() == urlToLoadAfterInitialDeconnected) {
                Log.e("MyWebViewClient", "Error occurred while loading the token URL.")
            }
        }
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val requestedUrl = request?.url?.toString()

        if (view == null || requestedUrl == null) {
            return false
        }

        Log.d("MyWebViewClient", "Requested URL: $requestedUrl")

        if (WebOdooUtils.isDeconnected(requestedUrl)) {
            Log.d("MyWebViewClient", "Deconnected state detected for URL: $requestedUrl")
            if (connectionAttemptMadeWithTokenUrl) {
                Log.d("MyWebViewClient", "Already attempted token auth. Navigating to home.")
                onDeconnectedNavigate()
                return true
            } else {
                Log.d("MyWebViewClient", "First deconnected detection. Attempting to load token URL: $urlToLoadAfterInitialDeconnected")
                connectionAttemptMadeWithTokenUrl = true
                view.loadUrl(urlToLoadAfterInitialDeconnected)
                return true
            }
        } else {
            if (requestedUrl == urlToLoadAfterInitialDeconnected) {
                connectionAttemptMadeWithTokenUrl = true
            }
            Log.d("MyWebViewClient", "URL not deconnected. Allowing WebView to handle: $requestedUrl")
            return false
        }
    }
}