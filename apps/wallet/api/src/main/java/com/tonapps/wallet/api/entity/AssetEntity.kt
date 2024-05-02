package com.tonapps.wallet.api.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssetEntity(
    val token: TokenEntity,
    val value: Float,
    val walletAddress: String,
    val usdPrice: Float
) : Parcelable
