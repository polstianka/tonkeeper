package com.tonapps.tonkeeper.fragment.fiat.web

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.launch
import uikit.base.BaseFragment
import uikit.widget.HeaderView
import uikit.widget.LoaderView
import uikit.widget.webview.WebViewFixed

class FiatWebFragment : BaseFragment(R.layout.fragment_web_fiat) {

    companion object {
        private const val URL = "url"
        private const val SUCCESS_URL_PATTERN = "success_url_pattern"

        fun newInstance(
            url: String,
            pattern: FiatSuccessUrlPattern?
        ): FiatWebFragment {
            val fragment = FiatWebFragment()
            fragment.arguments = Bundle().apply {
                putString(URL, url)
                putString(SUCCESS_URL_PATTERN, pattern?.toJSON().toString())
            }
            return fragment
        }
    }

    private val url: String by lazy { arguments?.getString(URL) ?: "" }

    private val successUrlPattern: FiatSuccessUrlPattern? by lazy {
        val value = arguments?.getString(SUCCESS_URL_PATTERN)
        if (value.isNullOrBlank() || value == "null") {
            return@lazy null
        }
        FiatSuccessUrlPattern(value)
    }

    private lateinit var headerView: HeaderView
    private lateinit var loaderView: LoaderView
    private lateinit var webView: WebViewFixed

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        loaderView = view.findViewById(R.id.loader)

        webView = view.findViewById(R.id.web)
        webView.webChromeClient = object : android.webkit.WebChromeClient() {

        }
        webView.webViewClient = object : android.webkit.WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.visibility = View.VISIBLE
                loaderView.visibility = View.GONE
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url?.matches(".*#endsession".toRegex()) == true) {
                    finish()
                    return
                }

                val successUrlPattern = successUrlPattern?.pattern ?: return
                val regexp = Regex(successUrlPattern, RegexOption.IGNORE_CASE)

                regexp.find(url ?: "")?.groupValues ?: return
                finish()
            }
        }

        loadUrl()
    }

    private fun loadUrl() {
        lifecycleScope.launch {
            loadUrl(url)
        }
    }

    private fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.destroy()
    }
}