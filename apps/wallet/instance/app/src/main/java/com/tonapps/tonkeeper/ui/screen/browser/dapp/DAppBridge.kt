package com.tonapps.tonkeeper.ui.screen.browser.dapp

import com.tonapps.wallet.data.tonconnect.entities.DAppPayloadEntity
import com.tonapps.wallet.data.tonconnect.entities.reply.DAppDeviceEntity
import org.json.JSONArray
import org.json.JSONObject
import uikit.widget.webview.bridge.JsBridge
import uikit.widget.webview.bridge.message.BridgeMessage

class DAppBridge(
    val deviceInfo: DAppDeviceEntity = DAppDeviceEntity(),
    val isWalletBrowser: Boolean = true,
    val protocolVersion: Int = 2,
    val send: suspend (array: JSONArray) -> String?,
    val connect: suspend (protocolVersion: Int, request: DAppPayloadEntity) -> String?,
    val restoreConnection: suspend () -> String?,
    val disconnect: suspend () -> Unit,
): JsBridge("tonkeeper") {

    override val availableFunctions = arrayOf("send", "connect", "restoreConnection", "disconnect")

    init {
        keys["deviceInfo"] = deviceInfo.toJSON()
        keys["protocolVersion"] = protocolVersion
        keys["isWalletBrowser"] = isWalletBrowser
    }

    override suspend fun invokeFunction(name: String, args: JSONArray): Any? {
        return when (name) {
            "connect" -> connect(protocolVersion, DAppPayloadEntity(args.getJSONObject(1)))
            "send" -> send(args)
            "restoreConnection" -> restoreConnection()
            "disconnect" -> disconnect()
            else -> null
        }
    }

    override fun jsInjection(): String {
        val funcs = availableFunctions.joinToString(",") {"""
            $it: (...args) => {
                return new Promise((resolve, reject) => window.invokeRnFunc('${it}', args, resolve, reject))
            }
        """}.replace("\n", "").replace("  ", "")

        return """
            (() => {
                if (!window.tonkeeper) {
                    window.rnPromises = {};
                    window.rnEventListeners = [];
                    window.invokeRnFunc = (name, args, resolve, reject) => {
                        const invocationId = btoa(Math.random()).substring(0, 12);
                        const timeoutMs = ${timeout};
                        const timeoutId = timeoutMs ? setTimeout(() => reject(new Error('bridge timeout for function with name: '+name+'')), timeoutMs) : null;
                        window.rnPromises[invocationId] = { resolve, reject, timeoutId }
                        window.ReactNativeWebView.postMessage(JSON.stringify({
                            type: '${BridgeMessage.Type.InvokeRnFunc.value}',
                            invocationId: invocationId,
                            name,
                            args,
                        }));
                    };
                    
                    window.addEventListener('message', ({ data }) => {
                        try {
                            const message = data;
                            console.log('message bridge', JSON.stringify(message));
                            if (message.type === '${BridgeMessage.Type.FunctionResponse.value}') {
                                const promise = window.rnPromises[message.invocationId];
                                
                                if (!promise) {
                                    return;
                                }
                                
                                if (promise.timeoutId) {
                                    clearTimeout(promise.timeoutId);
                                }
                                
                                if (message.status === 'fulfilled') {
                                    promise.resolve(JSON.parse(message.data));
                                } else {
                                    promise.reject(new Error(message.data));
                                }
                                
                                delete window.rnPromises[message.invocationId];
                            }
                            
                            if (message.type === '${BridgeMessage.Type.Event.value}') {
                                window.rnEventListeners.forEach((listener) => listener(message.event));
                            }
                        } catch { }
                    });
                }
                
                const listen = (cb) => {
                    window.rnEventListeners.push(cb);
                    return () => {
                        const index = window.rnEventListeners.indexOf(cb);
                        if (index > -1) {
                            window.rnEventListeners.splice(index, 1);
                        }
                    };
                };
                
                window.${windowKey} = {
                    tonconnect: Object.assign(${JSONObject(keys)},{ $funcs },{ listen }),
                }

                window.tonapi = {
                    fetch: async (url, options) => {
                        return new Promise((resolve, reject) => {
                            options.headers = {
                                ...options.headers,
                                'Authorization': 'Bearer ' + ${tonkeeperTonApiToken},
                                'X-Authorization': options.headers.Authorization,
                            };
                            fetch(url, options)
                                .then((response) => response.json())
                                .then((data) => resolve(data))
                                .catch((error) => reject(error));
                        });
                    }
                }
            })();
        """
    }

}