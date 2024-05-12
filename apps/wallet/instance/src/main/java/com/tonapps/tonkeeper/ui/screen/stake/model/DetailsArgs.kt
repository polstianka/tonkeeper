package com.tonapps.tonkeeper.ui.screen.stake.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailsArgs(
    val address: String,
    val name: String,
    val isApyMax: Boolean,
    val value: String,
    val minDeposit: Long,
    val links: List<String>
) : Parcelable