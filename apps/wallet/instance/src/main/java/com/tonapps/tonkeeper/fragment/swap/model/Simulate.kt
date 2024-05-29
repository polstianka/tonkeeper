package com.tonapps.tonkeeper.fragment.swap.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Simulate(
    val swapRate: CharSequence,
    val priceImpact: CharSequence,
    val askUnits: CharSequence,
    val minimumReceived: CharSequence,
    val liquidityProviderFee: CharSequence,
    val blockchainFee: CharSequence,
    val route: CharSequence,
): Parcelable