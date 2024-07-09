package com.tonapps.tonkeeper.fragment.stake.pool_details.presentation

import androidx.annotation.DrawableRes
import com.tonapps.tonkeeper.core.TextWrapper

data class LinksChipModel(
    @DrawableRes val iconResId: Int,
    val text: TextWrapper,
    val url: String
)