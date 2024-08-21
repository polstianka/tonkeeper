package com.tonapps.wallet.data.settings.folder

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.settings.BatteryTransaction.Companion.toIntArray
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.settings.SpamTransactionState
import com.tonapps.wallet.data.settings.entities.WalletPrefsEntity
import kotlinx.coroutines.CoroutineScope

internal class WalletPrefsFolder(context: Context, scope: CoroutineScope): BaseSettingsFolder(context, scope, "wallet_prefs") {

    private companion object {
        private const val SORT_PREFIX = "sort_"
        private const val PUSH_PREFIX = "push_"
        private const val PURCHASE_PREFIX = "purchase_"
        private const val SETUP_HIDDEN_PREFIX = "setup_hidden_"
        private const val LAST_UPDATED_PREFIX = "last_updated_"
        private const val TELEGRAM_CHANNEL_PREFIX = "telegram_channel_"
        private const val SPAM_STATE_TRANSACTION_PREFIX = "spam_state_transaction_"
        private const val BATTERY_TX_ENABLED_PREFIX = "batter_tx_enabled_"
    }

    fun getBatteryTxEnabled(accountId: String): Array<BatteryTransaction> {
        val key = keyBatteryTxEnabled(accountId)
        return getIntArray(key).map { BatteryTransaction(it) }.toTypedArray()
    }

    fun setBatteryTxEnabled(accountId: String, types: Array<BatteryTransaction>) {
        val key = keyBatteryTxEnabled(accountId)
        putIntArray(key, types.distinct().toIntArray())
    }

    fun getSpamStateTransaction(
        walletId: String,
        id: String
    ): SpamTransactionState {
        val key = keySpamStateTransaction(walletId, id)
        val value = getInt(key, 0)
        return SpamTransactionState.entries.firstOrNull { it.state == value } ?: SpamTransactionState.UNKNOWN
    }

    fun setSpamStateTransaction(
        walletId: String,
        id: String,
        state: SpamTransactionState
    ) {
        val key = keySpamStateTransaction(walletId, id)
        putInt(key, state.state)
    }

    fun setLastUpdated(walletId: String) {
        putLong(key(LAST_UPDATED_PREFIX, walletId), System.currentTimeMillis() / 1000, false)
    }

    fun getLastUpdated(walletId: String): Long {
        return getLong(key(LAST_UPDATED_PREFIX, walletId))
    }

    fun isSetupHidden(walletId: String): Boolean {
        return getBoolean(key(SETUP_HIDDEN_PREFIX, walletId), false)
    }

    fun isTelegramChannel(walletId: String): Boolean {
        return getBoolean(keyTelegramChannel(walletId), false)
    }

    fun setupHide(walletId: String) {
        putBoolean(key(SETUP_HIDDEN_PREFIX, walletId), true)
    }

    fun setTelegramChannel(walletId: String) {
        putBoolean(keyTelegramChannel(walletId), true)
    }

    fun isPurchaseOpenConfirm(walletId: String, id: String): Boolean {
        val key = keyPurchaseOpenConfirm(walletId, id)
        return getInt(key, 0) == 0
    }

    fun disablePurchaseOpenConfirm(walletId: String, id: String) {
        putInt(keyPurchaseOpenConfirm(walletId, id), 1)
    }

    fun isPushEnabled(walletId: String): Boolean {
        return getBoolean(keyPush(walletId), true)
    }

    fun setPushEnabled(walletId: String, enabled: Boolean) {
        putBoolean(keyPush(walletId), enabled)
    }

    fun get(walletId: String): WalletPrefsEntity {
        val index = getInt(keySort(walletId))
        return WalletPrefsEntity(
            index = index
        )
    }

    fun setSort(walletIds: List<String>) {
        edit {
            for ((index, walletId) in walletIds.withIndex()) {
                putInt(keySort(walletId), index)
            }
        }
    }

    private fun keyBatteryTxEnabled(accountId: String): String {
        return key(BATTERY_TX_ENABLED_PREFIX, accountId)
    }

    private fun keySpamStateTransaction(walletId: String, id: String): String {
        val key = key(SPAM_STATE_TRANSACTION_PREFIX, walletId)
        return "$key$id"
    }

    private fun keyPurchaseOpenConfirm(walletId: String, id: String): String {
        val key = key(PURCHASE_PREFIX, walletId)
        return "$key$id"
    }

    private fun keySort(walletId: String): String {
        return key(SORT_PREFIX, walletId)
    }

    private fun keyPush(walletId: String): String {
        return key(PUSH_PREFIX, walletId)
    }

    private fun keyTelegramChannel(walletId: String): String {
        return key(TELEGRAM_CHANNEL_PREFIX, walletId)
    }

    private fun key(prefix: String, walletId: String): String {
        return "$prefix$walletId"
    }
}