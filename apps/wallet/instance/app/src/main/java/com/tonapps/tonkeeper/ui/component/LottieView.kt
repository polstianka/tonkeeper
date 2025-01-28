package com.tonapps.tonkeeper.ui.component

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tonapps.extensions.rawText
import com.tonapps.tonkeeperx.R

class LottieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : WebView(context, attrs, defStyle) {

    var doOnReady: (() -> Unit)? = null

    init {
        settings.apply {
            javaScriptEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            displayZoomControls = false
            builtInZoomControls = false
            setSupportZoom(false)
            setEnableSmoothTransition(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setRendererPriorityPolicy(RENDERER_PRIORITY_IMPORTANT, true)
            }
        }

        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER

        setBackgroundColor(0x00000000)
        setLayerType(LAYER_TYPE_HARDWARE, null)

        setInitialScale(100)

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                doOnReady?.invoke()
            }
        }
    }

    fun setUri(uri: Uri) {
        val data = loadHtml(context).replace("{lottieUrl}", uri.toString())
        loadDataWithBaseURL(null, data, "text/html", "utf-8", null)
    }

    override fun onDetachedFromWindow() {
        clearCache(true)
        clearHistory()
        super.onDetachedFromWindow()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    private companion object {

        @Volatile
        private var cachedLottie: String? = null

        private fun loadHtml(context: Context): String {
            return cachedLottie ?: synchronized(this) {
                cachedLottie ?: context.rawText(R.raw.lottie_webview).also {
                    cachedLottie = it
                }
            }
        }
    }

}