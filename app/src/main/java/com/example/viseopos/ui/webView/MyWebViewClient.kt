package com.example.viseopos.ui.webView

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.viseopos.utils.WebOdooUtils

class MyWebViewClient(
    val isLoadingLambda: (Boolean) -> Unit,
    val hasErrorLambda: (Boolean) -> Unit,
    val errorMessageLambda: (String?) -> Unit,
    val onDeconnectedNavigate: () -> Unit
) : WebViewClient() {
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
            val errorDesc = "Error ${error?.errorCode}: ${error?.description}"
            errorMessageLambda(errorDesc)
            Log.e("MyWebViewClient", errorDesc)
        }
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val requestedUrl = request?.url?.toString()
        if (requestedUrl != null) {
            Log.d("MyWebViewClient", "Requested URL: $requestedUrl")
            if (WebOdooUtils.isDeconnected(requestedUrl)) {
                Log.d("MyWebViewClient", "Deconnected state detected. Navigating.")
                onDeconnectedNavigate()
            } else {
                Log.d("MyWebViewClient", "Loading URL in WebView: $requestedUrl")
                view?.loadUrl(requestedUrl)
            }
            return true
        }
        return false
    }
}