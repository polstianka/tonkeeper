package uikit.widget.webview.bridge

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import uikit.widget.webview.WebViewFixed
import uikit.widget.webview.bridge.message.BridgeMessage
import uikit.widget.webview.bridge.message.FunctionInvokeBridgeMessage
import uikit.widget.webview.bridge.message.FunctionResponseBridgeMessage
import java.util.LinkedList

open class BridgeWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.webViewStyle,
) : WebViewFixed(context, attrs, defStyle) {

    var jsBridge: JsBridge? = null
        set(value) {
            field = value
            value?.let {
                executeJS(value.jsInjection())
            }
        }

    private var isPageLoaded = false
        set(value) {
            field = value
            if (value) {
                executeJsQueue()
            }
        }

    private val clientCallbacks = mutableListOf<WebViewClient>()
    private val jsExecuteQueue = LinkedList<String>()
    private val scope: CoroutineScope
        get() = findViewTreeLifecycleOwner()?.lifecycleScope ?: throw IllegalStateException("No lifecycle owner")

    init {
        addJavascriptInterface(this, "ReactNativeWebView")
        webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isPageLoaded = true
                clientCallbacks.forEach { it.onPageStarted(view, url, favicon) }
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return clientCallbacks.map { it.shouldOverrideUrlLoading(view, request) }.firstOrNull() ?: false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                clientCallbacks.forEach { it.onPageFinished(view, url) }
            }
        }
    }

    fun addClientCallback(callback: WebViewClient) {
        clientCallbacks.add(callback)
    }

    fun removeClientCallback(callback: WebViewClient) {
        clientCallbacks.remove(callback)
    }

    fun executeJS(code: String) {
        if (isPageLoaded) {
            evaluateJavascript(code)
        } else {
            jsExecuteQueue.add(code)
        }
    }

    private fun executeJsQueue() {
        while (jsExecuteQueue.isNotEmpty()) {
            jsExecuteQueue.poll()?.let { evaluateJavascript(it) }
        }
    }

    private fun evaluateJavascript(code: String) {
        evaluateJavascript(code, null)
    }

    private suspend fun postMessage(
        message: FunctionResponseBridgeMessage
    ) = withContext(Dispatchers.Main) {
        val code = """
            (function() {
                window.dispatchEvent(new MessageEvent('message', {
                    data: ${message.createJSON()}
                }));
            })();
        """
        executeJS(code)
    }

    @JavascriptInterface
    fun postMessage(message: String) {
        Log.d("DAppBridgeLog", "postMessage: $message")
        val json = JSONObject(message)
        val type = json.getString("type")
        if (type == BridgeMessage.Type.InvokeRnFunc.value) {
            scope.launch {
                invokeFunction(FunctionInvokeBridgeMessage(json))
            }
        }
    }

    private suspend fun invokeFunction(message: FunctionInvokeBridgeMessage) {
        val bridge = jsBridge ?: return

        try {
            val data = bridge.invokeFunction(message.name, message.args) ?: return
            //val data = bridge.invokeFunction(message.name, JSONArray("[{\"source\":\"UQD2NmD_lH5f5u1Kj3KfGyTvhZSX0Eg6qp2a5IQUKXxOGzCi\",\"valid_until\":1716924003449,\"messages\":[{\"address\":\"EQARULUYsmJq1RiZ-YiH-IJLcAZUVkVff-KBPwEmmaQGH6aC\",\"amount\":\"1215000000\",\"payload\":\"te6ccsEBAgEAhQAAOgFtD4p+pQAAAAAAADA5Q7msoAgA7zuZAqJxsqAciTilI8/iTnGEeq62piAAHtRKd6wOcJwQM0KPAwEAkSWThWGAEPckSDVNSvvmJOKMGCE4qb+zE0NdQGXsIPpYy63RDznCAwA9jZg/5R+X+btSo9ynxsk74WUl9BIOqqdmuSEFCl8ThtBeoaGW\"}]}]")) ?: return
            postMessage(FunctionResponseBridgeMessage(
                invocationId = message.invocationId,
                status = "fulfilled",
                data = data,
            ))
        } catch (e: Throwable) {
            postMessage(FunctionResponseBridgeMessage(
                invocationId = message.invocationId,
                error = e,
            ))
        }
    }
}