package com.mac.macwebview

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ZoomButtonsController
import com.mac.macwebview.utils.MacConstant
import java.lang.reflect.InvocationTargetException
import java.util.*

class MacWebView : WebView {
    private var mContext: Context
    private val macWebChromeClient: MacWebChromeClient
    private val macWebViewClient: MacWebViewClient
    private var specialModel = ArrayList<String>()
    companion object {
        const val APP_CACHE_DIRNAME = "mac_dir"
    }

    init {
        specialModel.add("MT870")
        specialModel.add("XT910")
        specialModel.add("XT928")
        specialModel.add("MT917")
        specialModel.add("vivo")
        specialModel.add("Lenovo A60")
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        macWebChromeClient = MacWebChromeClient(mContext)
        macWebViewClient = MacWebViewClient()
        webChromeClient = macWebChromeClient
        webViewClient = macWebViewClient
        initView()
    }

    private fun initView() {
        WebView.setWebContentsDebuggingEnabled(true)
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        val webSettings = settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.safeBrowsingEnabled = false
        }
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        val ua = webSettings.userAgentString
        webSettings.userAgentString = ua + "/" + MacConstant.MAC_AGENT
        // Set cache size to 8 mb by default. should be more than enough
        webSettings.setAppCacheMaxSize((1024 * 1024 * 8).toLong())
        val appCachePath = mContext.cacheDir.absolutePath
        webSettings.setAppCachePath(appCachePath)
        webSettings.allowFileAccess = true
        webSettings.setAppCacheEnabled(true)

        //T
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.allowUniversalAccessFromFileURLs = true
        } else {
            try {
                webSettings::setAllowUniversalAccessFromFileURLs.invoke(true)
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

        }

        ////存储H5
        val appCacheDir = mContext.applicationContext.getDir("cache", Context.MODE_PRIVATE).path + APP_CACHE_DIRNAME
        webSettings.setAppCachePath(appCacheDir)
        webSettings.setAppCacheMaxSize(5242880)
        webSettings.databasePath = appCacheDir
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.databaseEnabled = true
        webSettings.setAppCacheEnabled(true)
        webSettings.textSize = WebSettings.TextSize.NORMAL
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.mixedContentMode = 0
        }

        try {
            webSettings::setDomStorageEnabled.invoke(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (supportWebViewFullScreen()) {
            webSettings.useWideViewPort = true
            if (Build.VERSION.SDK_INT >= 7) {
                try {
                    webSettings::setLoadWithOverviewMode.invoke(true)
                } catch (e7: Exception) {
                }

            }

            if (isSupportMultiTouch()) {
                if (Build.VERSION.SDK_INT < 11) {
                    try {
                        val declaredField = WebView::class.java.getDeclaredField("mZoomButtonsController")
                        declaredField.setAccessible(true)
                        val zoomButtonsController = ZoomButtonsController(this)
                        zoomButtonsController.zoomControls.visibility = View.GONE
                        declaredField.set(this, zoomButtonsController)
                    } catch (e8: Exception) {
                    }

                } else {
                    try {
                        webSettings::setDisplayZoomControls.invoke(false)
                    } catch (e9: Exception) {
                    }

                }
            }
        }
        removeJavascriptInterface("searchBoxJavaBridge_")
        if (Build.VERSION.SDK_INT >= 11) {
            removeJavascriptInterface("accessibilityTraversal")
            removeJavascriptInterface("accessibility")
        }

        //设置Cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    /**
     * 是否支持全屏
     *
     * @return
     */
    private fun supportWebViewFullScreen(): Boolean {
        val str = Build.MODEL
        return !specialModel.contains(str)
    }

    /**
     * 是否支持多指操作
     *
     * @return
     */
    fun isSupportMultiTouch(): Boolean {
        var z = false
        var z2 = false
        for (method in MotionEvent::getDeclaredMethods) {
            if (method.getName() == "getPointerCount") {
                MotionEvent::getPointerCount
                z2 = true
            }
            if (method.getName() == "getPointerId") {
                z = true
            }
        }
        return if (Build.VERSION.SDK_INT >= 7) {
            true
        } else z && z2
    }
}