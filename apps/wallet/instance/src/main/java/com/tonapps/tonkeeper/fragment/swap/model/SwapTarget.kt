package com.tonapps.tonkeeper.fragment.swap.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class SwapTarget: Parcelable {
    Send, Receive
}