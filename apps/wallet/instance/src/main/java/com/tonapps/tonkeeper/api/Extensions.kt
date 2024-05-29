package com.tonapps.tonkeeper.api

import android.content.Context
import android.net.Uri
import android.util.Log
import com.squareup.moshi.adapter
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.extensions.toUserFriendly
import com.tonapps.extensions.ifPunycodeToUnicode
import com.tonapps.tonkeeperx.R
import io.tonapi.infrastructure.Serializer
import io.tonapi.models.Account
import io.tonapi.models.AccountAddress
import io.tonapi.models.AccountEvent
import io.tonapi.models.Action
import io.tonapi.models.ImagePreview
import io.tonapi.models.JettonBalance
import io.tonapi.models.JettonBurnAction
import io.tonapi.models.JettonMintAction
import io.tonapi.models.JettonPreview
import io.tonapi.models.JettonSwapAction
import io.tonapi.models.JettonVerificationType
import io.tonapi.models.MessageConsequences
import io.tonapi.models.NftItem
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.TokenRates
import kotlinx.coroutines.delay
import java.math.BigDecimal
import kotlin.math.abs

private val nftItemPreviewSizes = arrayOf(
    "250x250", "500x500", "100x100"
)

fun TokenRates.to(toCurrency: String, value: Float): Float {
    val price = prices?.get(toCurrency) ?: return 0f
    return price.toFloat() * value
}

val MessageConsequences.totalFees: Long
    get() = trace.transaction.totalFees

val AccountEvent.withTON: Boolean
    get() {
        for (action in actions) {
            val type = action.type
            if (type == Action.Type.tonTransfer ||
                type == Action.Type.jettonSwap ||
                type == Action.Type.electionsDepositStake ||
                type == Action.Type.electionsRecoverStake ||
                type == Action.Type.subscribe ||
                type == Action.Type.unSubscribe ||
                type == Action.Type.depositStake ||
                type == Action.Type.withdrawStake) {
                return true
            }
        }
        return false
    }

val PoolImplementationType.icon: Int
    get() {
        return when (this) {
            PoolImplementationType.tf -> com.tonapps.uikit.icon.R.drawable.ic_staking_tf
            PoolImplementationType.whales -> com.tonapps.uikit.icon.R.drawable.ic_staking_whales
            PoolImplementationType.liquidTF -> com.tonapps.uikit.icon.R.drawable.ic_staking_tonstakers
        }
    }

val Uri.icon: Int
    get() {
        return when (this.host) {
            "t.me" -> R.drawable.ic_telegram_16
            "tonviewer.com" -> com.tonapps.uikit.icon.R.drawable.ic_magnifying_glass_16
            "twitter.com" -> R.drawable.ic_twitter_16
            else -> R.drawable.ic_globe_16
        }
    }

fun Uri.getName(context: Context): String {
    return when (this.host) {
        "t.me" -> context.getString(com.tonapps.wallet.localization.R.string.link_community)
        "twitter.com" -> context.getString(com.tonapps.wallet.localization.R.string.link_twitter)
        else -> this.host ?: ""
    }
}

val Float.percentage: String
    get() = BigDecimal(this.toDouble()).percentage

val BigDecimal.percentage: String
    get() = this.setScale(2, BigDecimal.ROUND_DOWN).toPlainString() + "%"

val PoolImplementationType.iconURL: String
    get() = "res:/${icon}"

val PoolImplementationType.iconUri: Uri
    get() = Uri.parse(iconURL)


suspend fun <R> withRetry(
    times: Int = 5,
    delay: Long = 1000,
    block: () -> R
): R? {
    for (i in 0 until times) {
        try {
            return block()
        } catch (e: Throwable) {
            Log.e("RetryErrorLog", "error", e)
        }
        delay(delay)
    }
    return null
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> toJSON(obj: T?): String {
    if (obj == null) {
        return ""
    }
    return Serializer.moshi.adapter<T>().toJson(obj)
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> fromJSON(json: String): T {
    return Serializer.moshi.adapter<T>().fromJson(json)!!
}

val AccountEvent.fee: Long
    get() {
        if (0 > extra) {
            return abs(extra)
        }
        return 0
    }

val AccountEvent.refund: Long
    get() {
        if (0 < extra) {
            return extra
        }
        return 0
    }

val JettonPreview.isTon: Boolean
    get() {
        return address == "TON"
    }

val JettonSwapAction.jettonPreview: JettonPreview?
    get() {
        return jettonMasterIn ?: jettonMasterOut
    }

val JettonSwapAction.amount: String
    get() {
        if (amountIn.isEmpty()) {
            return amountOut
        }
        return amountIn
    }

val JettonSwapAction.ton: Long
    get() {
        return tonIn ?: tonOut ?: 0
    }

fun AccountAddress.getNameOrAddress(testnet: Boolean): String {
    if (!name.isNullOrBlank()) {
        return name!!.ifPunycodeToUnicode()
    }
    return address.toUserFriendly(
        wallet = isWallet,
        testnet = testnet
    ).shortAddress
}

val AccountAddress.iconURL: String?
    get() = icon

val String.shortAddress: String
    get() {
        if (length < 8) return this
        return substring(0, 4) + "…" + substring(length - 4, length)
    }

val String.shortHash: String
    get() {
        if (length < 16) return this
        return substring(0, 8) + "…" + substring(length - 8, length)
    }

fun JettonBalance.getAddress(testnet: Boolean): String {
    return jetton.address.toUserFriendly(
        wallet = false,
        testnet = testnet
    )
}

val JettonBalance.symbol: String
    get() = jetton.symbol

val JettonBalance.parsedBalance: Float
    get() = Coin.parseJettonBalance(balance, jetton.decimals)

val JettonMintAction.parsedAmount: Float
    get() = Coin.parseJettonBalance(amount, jetton.decimals)

val JettonBurnAction.parsedAmount: Float
    get() = Coin.parseJettonBalance(amount, jetton.decimals)

fun NftItem.imageBySize(size: String): ImagePreview? {
    return previews?.firstOrNull { it.resolution == size }
}

val NftItem.imageURL: String
    get() {
        for (size in nftItemPreviewSizes) {
            val preview = imageBySize(size)
            if (preview != null) {
                return preview.url
            }
        }
        return previews?.lastOrNull()?.url ?: ""
    }

val NftItem.title: String
    get() {
        var metadataName = metadata["name"] as? String
        if (metadataName == null) {
            metadataName = collection?.name ?: ""
        }
        if (metadataName.endsWith(".t.me")) {
            metadataName = "@" + metadataName.substring(0, metadataName.length - 6)
        }
        return metadataName
    }

val NftItem.description: String?
    get() {
        val metadataName = metadata["description"] as? String
        if (metadataName != null) {
            return metadataName
        }
        return null
    }

val NftItem.collectionName: String
    get() {
        return collection?.name ?: ""
    }

val NftItem.collectionDescription: String?
    get() {
        return collection?.description
    }

fun NftItem.getOwnerAddress(testnet: Boolean): String? {
    return owner?.address?.toUserFriendly(
        wallet = true,
        testnet = testnet
    )
}
