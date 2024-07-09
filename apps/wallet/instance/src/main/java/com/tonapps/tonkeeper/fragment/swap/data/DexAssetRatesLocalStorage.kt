package com.tonapps.tonkeeper.fragment.swap.data

import android.content.Context
import com.tonapps.extensions.prefs
import com.tonapps.extensions.string
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetRate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DexAssetRatesLocalStorage(
    context: Context
) {

    companion object {
        private const val NAME_PREFS = "DexAssetsLocalStorage"
        private const val KEY_RATES = "KEY_RATES "
    }

    private val prefs = context.prefs(NAME_PREFS)

    suspend fun getRates(): List<DexAssetRate> {
        return prefs.string(KEY_RATES)
            ?.let { parse(it) }
            ?: emptyList()
    }

    suspend fun setRates(list: List<DexAssetRate>) {
        prefs.string(KEY_RATES, Json.encodeToString(list))
    }

    private fun parse(string: String): List<DexAssetRate>? {
        return try {
            Json.decodeFromString<List<DexAssetRate>>(string)
        } catch (any: Throwable) {
            null
        }
    }
}