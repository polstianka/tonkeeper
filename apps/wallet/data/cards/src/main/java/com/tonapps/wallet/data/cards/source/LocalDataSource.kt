package com.tonapps.wallet.data.cards.source

import android.content.Context
import android.content.SharedPreferences
import com.tonapps.extensions.putString
import com.tonapps.extensions.string
import com.tonapps.security.Security
import com.tonapps.wallet.data.cards.entity.CardsDataEntity
import com.tonapps.wallet.data.core.BlobDataSource

internal class LocalDataSource(
    context: Context
) {
    private val cardsData = BlobDataSource.simple<CardsDataEntity>(context, "holders_cards")

    private val encryptedPrefs: SharedPreferences by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Security.pref(context, KEY_ALIAS, NAME)
    }

    fun setCards(address: String, testnet: Boolean, cards: CardsDataEntity) {
        cardsData.setCache(cardsCacheKey(address, testnet), cards)
    }

    fun getCards(address: String, testnet: Boolean): CardsDataEntity? {
        return cardsData.getCache(cardsCacheKey(address, testnet))
    }

    fun getAccountToken(address: String, testnet: Boolean): String? {
        val key = accountTokenKey(address, testnet)
        return encryptedPrefs.string(key)
    }

    fun setAccountToken(address: String, testnet: Boolean, token: String) {
        val key = accountTokenKey(address, testnet)
        encryptedPrefs.putString(key, token)
    }

    private fun accountTokenKey(address: String, testnet: Boolean): String {
        val prefix = ACCOUNT_TOKEN_PREFIX + "_" + (if (testnet) "testnet" else "mainnet")
        return "${prefix}_${address}"
    }

    private fun cardsCacheKey(address: String, testnet: Boolean): String {
        val prefix = if (testnet) "testnet" else "mainnet"
        return "$prefix:$address"
    }

    private companion object {
        private const val NAME = "cards"
        private const val KEY_ALIAS = "_com_tonapps_wallet_cards_key_"
        private const val ACCOUNT_TOKEN_PREFIX = "account_token"
    }
}

