package com.mac.macwebview

import android.net.http.SslError
import android.os.Build
import android.support.annotation.RequiresApi
import android.webkit.*
import java.net.URLDecoder

class MacWebViewClient : WebViewClient() {
    private var handleError: Boolean = false
    private var isLoadError: Boolean = false

    fun setLoadError(value: Boolean) {
        isLoadError = value
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        if (request!!.isForMainFrame) {
            if (view != null && handleError) {
                view.loadUrl("about:blank")
            }
            isLoadError = true
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        onReceivedError(view, error.errorCode, error.description.toString(), request.url.toString())
    }

    override fun onReceivedError(view: WebView?, errorCode: Int, description: String, failingUrl: String) {
        isLoadError = if (errorCode == -10) {
            //ERR_UNKNOWN_URL_SCHEME
            false
        } else {
            if (view != null && handleError) {
                view.loadUrl("about:blank")
            }
            true
        }
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        handler.proceed() // proceed ignoring ssl error.
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return super.shouldOverrideUrlLoading(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        var url = URLDecoder.decode(request?.url?.path, "UTF-8")
        return super.shouldOverrideUrlLoading(view, request)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return shouldInterceptRequest(view, request.url.toString())
    }

    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        return null
    }
}