package com.tonapps.tonkeeper.fragment.swap.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SwapConfirmArgs(
    val walletAddress: String,
    val send: SwapState.TokenState,
    val receive: SwapState.TokenState,
    val simulate: Simulate,
    val offerAmount: String,
    val minAskAmount: String,
): Parcelable