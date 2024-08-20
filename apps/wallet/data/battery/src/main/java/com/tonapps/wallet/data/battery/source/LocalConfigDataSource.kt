package com.tonapps.wallet.data.battery.source

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.security.Security
import com.tonapps.wallet.data.battery.entity.BatteryConfigEntity
import com.tonapps.wallet.data.battery.entity.BatterySupportedTransaction
import com.tonapps.wallet.data.core.BlobDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

internal class LocalConfigDataSource(context: Context) : BlobDataSource<BatteryConfigEntity>(
    context = context,
    path = "battery_config",
) {

    companion object {
        private const val NAME = "battery_config"
        private const val KEY_ALIAS = "_com_tonapps_battery_config_master_key_"
        private val DEFAULT_SUPPORTED_TRANSACTIONS = mapOf(
            BatterySupportedTransaction.SWAP to true,
            BatterySupportedTransaction.JETTON to true,
            BatterySupportedTransaction.NFT to true
        )
    }

    private val encryptedPrefs = Security.pref(context, KEY_ALIAS, NAME)

    private fun getSupportedTransactionsKey(walletId: String): String {
        return "supported_transactions_$walletId"
    }

    fun getSupportedTransactions(walletId: String): Map<BatterySupportedTransaction, Boolean> {
        val json = encryptedPrefs.getString(getSupportedTransactionsKey(walletId), null)
            ?: return DEFAULT_SUPPORTED_TRANSACTIONS

        val gson = Gson()
        val type = object : TypeToken<Map<BatterySupportedTransaction, Boolean>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveSupportedTransaction(walletId: String, supportedTransactions: Map<BatterySupportedTransaction, Boolean>) {
        val gson = Gson()
        val jsonString = gson.toJson(supportedTransactions)
        encryptedPrefs.edit {
            putString(getSupportedTransactionsKey(walletId), jsonString)
        }
    }

    override fun onMarshall(data: BatteryConfigEntity) = data.toByteArray()

    override fun onUnmarshall(bytes: ByteArray) = bytes.toParcel<BatteryConfigEntity>()
}

