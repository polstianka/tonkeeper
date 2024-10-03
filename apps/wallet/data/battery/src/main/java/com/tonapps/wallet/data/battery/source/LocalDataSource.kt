package com.tonapps.wallet.data.battery.source

import android.content.Context
import androidx.core.content.edit
import com.tonapps.blockchain.ton.extensions.hex
import com.tonapps.extensions.prefs
import com.tonapps.security.Security
import com.tonapps.wallet.data.battery.entity.BatteryBalanceEntity
import com.tonapps.wallet.data.battery.entity.BatteryConfigEntity
import com.tonapps.wallet.data.core.BlobDataSource
import org.ton.api.pub.PublicKeyEd25519

internal class LocalDataSource(
    context: Context
) {

    companion object {
        private const val NAME = "battery"
    }

    private val balance = BlobDataSource.simple<BatteryBalanceEntity>(context, "battery_balance")
    private val configStore = BlobDataSource.simple<BatteryConfigEntity>(context, "battery_config")

    private val prefs = context.prefs(NAME)

    fun setConfig(testnet: Boolean, entity: BatteryConfigEntity) {
        configStore.setCache(configCacheKey(testnet), entity)
    }

    fun getConfig(testnet: Boolean): BatteryConfigEntity? {
        return configStore.getCache(configCacheKey(testnet))
    }

    private fun configCacheKey(testnet: Boolean): String {
        return if (testnet) "testnet" else "mainnet"
    }

    fun setBalance(publicKey: PublicKeyEd25519, testnet: Boolean, entity: BatteryBalanceEntity) {
        balance.setCache(balanceCacheKey(publicKey, testnet), entity)
    }

    fun getBalance(publicKey: PublicKeyEd25519, testnet: Boolean): BatteryBalanceEntity? {
        return balance.getCache(balanceCacheKey(publicKey, testnet))
    }

    private fun balanceCacheKey(publicKey: PublicKeyEd25519, testnet: Boolean): String {
        val prefix = if (testnet) "testnet" else "mainnet"
        return "$prefix:${publicKey.hex()}"
    }

    fun getAppliedPromo(testnet: Boolean): String? {
        return prefs.getString(promoKey(testnet), null)
    }

    fun setAppliedPromo(testnet: Boolean, promo: String) {
        prefs.edit {
            putString(promoKey(testnet), promo)
        }
    }

    fun getPreferGasless(testnet: Boolean): Boolean {
        return prefs.getBoolean(gaslessKey(testnet), false)
    }

    fun setPreferGasless(testnet: Boolean, value: Boolean) {
        prefs.edit {
            putBoolean(gaslessKey(testnet), value)
        }
    }

    private fun gaslessKey(testnet: Boolean): String {
        return "prefer_gasless_${if (testnet) "testnet" else "mainnet"}"
    }

    private fun promoKey(testnet: Boolean): String {
        return "promo_${if (testnet) "testnet" else "mainnet"}"
    }
}

