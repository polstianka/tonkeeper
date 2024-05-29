package com.tonapps.tonkeeper.api.swap

import android.content.SharedPreferences
import com.tonapps.extensions.bool
import com.tonapps.extensions.float
import com.tonapps.extensions.string
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SwapLocalDataSource(private val preferences: SharedPreferences) {

    suspend fun getSuggested(wallet: String): List<String> = withContext(Dispatchers.IO) {
        val result = mutableListOf<String>()
        val savedAsString = preferences.string("swap_suggested_$wallet")
        if (savedAsString != null) {
            val saved = savedAsString.split(";")
            if (saved.isNotEmpty()) {
                result.addAll(saved)
            }
        } else {
            result.add(SwapRepository.TON_ADDRESS)
            result.add(SwapRepository.USDT_ADDRESS)
        }

        return@withContext result
    }

    suspend fun saveSuggested(wallet:String, suggested: List<String>)= withContext(Dispatchers.IO) {
        preferences.string("swap_suggested_$wallet", suggested.joinToString(separator = ";"))
    }

    suspend fun saveSlippage(wallet:String, slippage: Float)= withContext(Dispatchers.IO) {
        preferences.float("swap_slippage_$wallet", slippage)
    }

    suspend fun getSlippage(wallet: String): Float = withContext(Dispatchers.IO) {
        val saved = preferences.float("swap_slippage_$wallet")
        return@withContext if (saved == 0f) 0.01f else saved
    }

    suspend fun saveExpertMode(wallet:String, expert: Boolean)= withContext(Dispatchers.IO) {
        preferences.bool("swap_expert_$wallet", expert)
    }

    suspend fun getExpertMode(wallet: String): Boolean = withContext(Dispatchers.IO) {
        val saved = preferences.bool("swap_expert_$wallet")
        return@withContext saved
    }

}