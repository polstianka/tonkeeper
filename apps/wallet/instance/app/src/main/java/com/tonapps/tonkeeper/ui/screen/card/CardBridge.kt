package com.tonapps.tonkeeper.ui.screen.card

import com.tonapps.tonkeeper.ui.screen.card.entity.CardBridgeEvent
import com.tonapps.tonkeeper.ui.screen.card.entity.CardsStateEntity
import org.json.JSONArray
import org.json.JSONObject
import uikit.widget.webview.bridge.JsBridge
import uikit.widget.webview.bridge.message.BridgeMessage

class CardBridge(
    val deviceInfo: String,
    private val cardsState: CardsStateEntity,
    val send: suspend (array: JSONArray) -> JSONObject,
    val restoreConnection: suspend () -> JSONObject,
    val onEvent: (event: CardBridgeEvent) -> Unit,
): JsBridge("tonkeeper") {

    override val availableFunctions = arrayOf("send", "restoreConnection")

    init {
        keys["deviceInfo"] = deviceInfo
        keys["protocolVersion"] = 2
        keys["isWalletBrowser"] = true
    }

    override suspend fun invokeFunction(name: String, args: JSONArray): Any? {
        return when (name) {
            "send" -> send(args).toString()
            "restoreConnection" -> restoreConnection().toString()
            "closeApp" -> onEvent(CardBridgeEvent.CloseApp)
            "openUrl" -> onEvent(CardBridgeEvent.OpenUrl(args.getString(0)))
            else -> null
        }
    }

    override fun jsInjection(): String {
        val funcs = availableFunctions.joinToString(",") {"""
            $it: (...args) => {
                return new Promise((resolve, reject) => window.invokeRnFunc('${it}', args, resolve, reject))
            }
        """}.replace("\n", "").replace("  ", "")

        val accountsList = cardsState.data.accounts.joinToString(",") { it.toJSON() }
        val prepaidCards = cardsState.data.prepaidCards.joinToString(",") { it.toJSON() }

        return """
            (() => {
                if (!window.${windowKey}) {
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
                    tonconnect: Object.assign(${JSONObject(keys)},{ $funcs },{ listen })
                };
            
                if (!window.initialState) {
                    window.initialState = {
                        user: {
                            token: "${cardsState.token}",
                            status: JSON.parse('${cardsState.data.state}')
                        },
                        accountsList: JSON.parse('[$accountsList]'),
                        prepaidCards: JSON.parse('[$prepaidCards]'),
                    };
                };
                
                if (!window["dapp-client"]) {
                    window["dapp-client"] = {
                        closeApp: (...args) => {
                            return new Promise((resolve, reject) => window.invokeRnFunc("closeApp", args, resolve, reject))
                        },
                        openUrl: (...args) => {
                            return new Promise((resolve, reject) => window.invokeRnFunc("openUrl", args, resolve, reject))
                        }
                    };
                };
            })();
        """
    }

}