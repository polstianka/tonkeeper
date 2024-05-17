package com.tonapps.tonkeeper.fragment.swap.domain.model

import android.os.Parcelable
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.stake.root.StakeViewModel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class DexAsset(
    val isCommunity: Boolean,
    val contractAddress: String,
    val decimals: Int,
    val hasDefaultSymbol: Boolean,
    val type: DexAssetType,
    val symbol: String,
    val imageUrl: String,
    val displayName: String,
): Parcelable

enum class DexAssetType {
    JETTON,
    WTON,
    TON
}

fun Long.formatCurrency(asset: DexAsset): String {
    val value = BigDecimal(this).movePointLeft(asset.decimals)
    return CurrencyFormatter.format(asset.symbol, value).toString()
}
