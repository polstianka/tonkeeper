package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import android.util.Log
import com.tonapps.tonkeeper.ui.screen.swap.data.AssetTag.Companion.hasTag
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.entity.RateEntity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import kotlin.math.sign

enum class AssetKind {
    TON, JETTON, WTON;

    companion object {
        fun valueOf(kind: String): AssetKind = when (kind) {
            "JETTON" -> JETTON
            "TON" -> TON
            "WTON" -> WTON
            else -> error("Unknown kind: $kind")
        }
    }
}

@Parcelize
data class AssetEntity(
    val userFriendlyAddress: String,
    val token: TokenEntity,

    val priority: Int,
    val kind: AssetKind,

    val balance: FormattedDecimal?,

    val deprecated: Boolean,
    val community: Boolean,
    val blacklisted: Boolean,
    val defaultSymbol: Boolean,
    val defaultList: Boolean,
    val tags: Int,

    val dexPriceUsd: BigDecimal?,
    val thirdPartyPriceUsd: BigDecimal?,
    val balanceInUsd: FormattedDecimal?,

    val userCurrency: WalletCurrency?,
    val priceInUserCurrency: BigDecimal?,
    val balanceInUserCurrency: FormattedDecimal?
): Parcelable, Comparable<AssetEntity> {
    @IgnoredOnParcel
    val hidden: Boolean =
        tags.hasTag(AssetTag.HIDDEN)

    @IgnoredOnParcel
    val hasFunds: Boolean =
        (balance != null && balance.number > BigDecimal.ZERO)

    @IgnoredOnParcel
    val isSuggested: Boolean =
        !hidden && (priority > 0 || token.isTon) && !deprecated

    @IgnoredOnParcel
    val anyPriceInUsd: BigDecimal?
        get() = dexPriceUsd ?: thirdPartyPriceUsd

    @IgnoredOnParcel
    val anyPriceInUserCurrency: BigDecimal?
        get() = priceInUserCurrency

    @IgnoredOnParcel
    val specialBadge: String? =
        if (kind == AssetKind.JETTON) TokenEntity.specialSymbol(token.symbol, token.address) else null


    override fun compareTo(other: AssetEntity): Int {
        return when {
            hidden != other.hidden -> hidden.compareTo(other.hidden)
            hasFunds != other.hasFunds -> other.hasFunds.compareTo(hasFunds)
            deprecated != other.deprecated -> deprecated.compareTo(other.deprecated)
            token.isTon != other.token.isTon -> other.token.isTon.compareTo(token.isTon)
            else -> {
                if (hasFunds) {
                    val aUser = this.balanceInUserCurrency?.number
                    val bUser = other.balanceInUserCurrency?.number
                    if (aUser != null && bUser != null) {
                        val cmp = bUser.compareTo(aUser)
                        if (cmp != 0) return cmp.sign
                    }
                    val aUsd = this.balanceInUsd?.number
                    val bUsd = other.balanceInUsd?.number
                    if (aUsd != null && bUsd != null) {
                        val cmp = bUsd.compareTo(aUsd)
                        if (cmp != 0) return cmp.sign
                    }
                }
                when {
                    priority != other.priority -> if (priority > other.priority) -1 else 1
                    token.symbol != other.token.symbol -> token.symbol.compareTo(other.token.symbol).sign
                    token.name != other.token.name -> token.name.compareTo(other.token.name).sign
                    else -> 0
                }
            }
        }
    }

    fun withRate(rate: RateEntity): AssetEntity {
        val userBalance = balance?.number ?: return this
        val priceInUserCurrency = rate.value.toBigDecimal()
        val balanceInUserCurrency = (priceInUserCurrency * userBalance).stripTrailingZeros()
        return copy(
            userCurrency = rate.currency,
            priceInUserCurrency = priceInUserCurrency,
            balanceInUserCurrency = FormattedDecimal(balanceInUserCurrency, balanceInUserCurrency.toCurrencyString(rate.currency))
        )
    }
}

class AssetTag {
    companion object {
        const val HIDDEN = 1
        const val WHITELISTED = 1 shl 1
        const val DEFAULT_SYMBOL = 1 shl 2
        const val DEFAULT_LIST = 1 shl 3
        const val DEPRECATED = 1 shl 4

        internal fun Int.hasTag(tag: Int): Boolean = (this and tag) == tag

        fun fromTagsList(list: List<String>): Int {
            var tags = 0
            for (tag in list) {
                tags = tags or fromTag(tag)
            }
            return tags
        }

        fun fromTag(tag: String): Int = when (tag) {
            "whitelisted" -> WHITELISTED
            "default_symbol" -> DEFAULT_SYMBOL
            "default_list" -> DEFAULT_LIST
            "hidden" -> HIDDEN
            "deprecated" -> DEPRECATED
            else -> {
                if (SwapConfig.LOGGING_ENABLED) {
                    Log.e(SwapConfig.LOGGING_TAG, "Unknown asset tag: $tag")
                }
                0
            }
        }
    }
}