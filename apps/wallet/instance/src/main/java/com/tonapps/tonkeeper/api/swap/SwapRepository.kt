package com.tonapps.tonkeeper.api.swap

import android.content.Context
import com.tonapps.blockchain.Coin
import com.tonapps.extensions.prefs
import com.tonapps.network.Network
import com.tonapps.tonkeeper.api.withRetry
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*

class SwapRepository(
    context: Context
) {
    companion object {
        val TON_ADDRESS = "EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM9c"
        val USDT_ADDRESS = "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs"
        val PROVIDER_NAME = "STON.fi"
    }

    private val BASE_URL = "https://api.ston.fi"
    private val localDataSource = SwapLocalDataSource(context.prefs("swap"))
    private var cached = ConcurrentHashMap<String, List<StonfiSwapAsset>>()
    private val assetByAddress = HashMap<String, StonfiSwapAsset>()
    private val pairs = HashMap<String, MutableList<StonfiSwapAsset>>()

    fun getPairedAssets(stonfiSwapAsset: StonfiSwapAsset): List<StonfiSwapAsset>? {
        return pairs[stonfiSwapAsset.contractAddress]
    }

    suspend fun getAssets(walletAddress: String): List<StonfiSwapAsset> {
        return withContext(Dispatchers.Default) {
            if (cached[walletAddress]?.isNotEmpty() == true) {
                return@withContext (cached[walletAddress] ?: emptyList())
            }
            val assetsD = async { loadAssets(walletAddress) }
            val pairsD = async { loadPairs() }

            val assets = assetsD.await()
            val pair = pairsD.await()
            pair?.let {
                for (i in 0 until it.length()) {
                    val item = it.getJSONArray(i)
                    val adr1 = item.optString(0)
                    val adr2 = item.optString(1)
                    val item1 = assetByAddress[adr1]
                    val item2 = assetByAddress[adr2]
                    if (item1 != null && item2 != null) {
                        if (pairs.contains(adr1)) {
                            pairs[adr1]?.add(item2)
                        } else {
                            pairs[adr1] = mutableListOf(item2)
                        }
                        if (pairs.contains(adr2)) {
                            pairs[adr2]?.add(item1)
                        } else {
                            pairs[adr2] = mutableListOf(item1)
                        }
                    }
                }
            }

            if (assets.isNotEmpty()) {
                cached[walletAddress] = assets
            }
            assets
        }
    }

    suspend fun getSuggestedAddresses(walletAddress: String): List<String> {
        return localDataSource.getSuggested(walletAddress)
    }

    suspend fun addSuggested(walletAddress: String, asset: List<String>) {
        localDataSource.saveSuggested(walletAddress, asset)
    }

    suspend fun getSlippage(walletAddress: String): Float {
        return localDataSource.getSlippage(walletAddress)
    }

    suspend fun setSlippage(walletAddress: String, slippage: Float) {
        localDataSource.saveSlippage(walletAddress, slippage)
    }

    suspend fun getExpertMode(walletAddress: String): Boolean {
        return localDataSource.getExpertMode(walletAddress)
    }

    suspend fun setExpertMode(walletAddress: String, expert: Boolean) {
        localDataSource.saveExpertMode(walletAddress, expert)
    }

    private suspend fun loadAssets(walletAddress: String): List<StonfiSwapAsset> {
        val result = mutableListOf<StonfiSwapAsset>()
        val url = "${BASE_URL}/v1/wallets/$walletAddress/assets"
        val response = withRetry { Network.get(url) } ?: return result
        try {
            val data = JSONObject(response).getJSONArray("asset_list")
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                val asset = StonfiSwapAsset(item)
                if (asset.defaultSymbol) {
                    result.add(asset)
                    assetByAddress[asset.contractAddress] = asset
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return result
    }

    private suspend fun loadPairs(): JSONArray? {
        val url = "${BASE_URL}/v1/markets"
        val response = withRetry { Network.get(url) } ?: return null
        try {
            val data = JSONObject(response).getJSONArray("pairs")
            return data
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun simulate(from: StonfiSwapAsset, to: StonfiSwapAsset, amountRaw: String, slippage: String = "0.001", reverse: Boolean = false): SwapSimulateData? {
        val u = if (reverse) "${BASE_URL}/v1/reverse_swap/simulate" else "${BASE_URL}/v1/swap/simulate"
        val url = u.toHttpUrl().newBuilder()
            .addQueryParameter("offer_address", from.contractAddress)
            .addQueryParameter("ask_address", to.contractAddress)
            .addQueryParameter("units", Coin.toNano(amountRaw.toFloat(), decimals = from.decimals).toString())
            .addQueryParameter("slippage_tolerance", slippage)
            .build().toString()
        val response = withRetry(times = 3) {
            Network.post(url, "".toRequestBody())
        } ?: return null
        try {
            val data = JSONObject(response)
            return SwapSimulateData(data)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }
}