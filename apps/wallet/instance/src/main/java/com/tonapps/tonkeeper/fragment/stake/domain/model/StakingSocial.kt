package com.tonapps.tonkeeper.fragment.stake.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StakingSocial(
    val type: StakingSocialType,
    val link: String
) : Parcelable

fun StakingPool.tonViewerSocial(): StakingSocial {
    return StakingSocial(
        type = StakingSocialType.TONVIEWER,
        link = "https://tonviewer.com/$address"
    )
}