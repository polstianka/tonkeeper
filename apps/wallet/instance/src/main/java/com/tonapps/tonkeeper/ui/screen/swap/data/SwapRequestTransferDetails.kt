package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.ton.bigint.BigInt

@Parcelize
data class SwapRequestTransferDetails(
    val userWalletAddress: String,
    val minAskAmount: BigInt,
    val offerAmount: BigInt,
    val jettonToWalletAddress: String,
    val jettonFromWalletAddress: String,
    val routerAddress: String,
    val forwardAmount: BigInt,
    val attachedAmount: BigInt,
    val userVisibleFeesAmount: BigInt,
    val sendMaximum: Boolean,
    val referralAddress: String?
) : Parcelable