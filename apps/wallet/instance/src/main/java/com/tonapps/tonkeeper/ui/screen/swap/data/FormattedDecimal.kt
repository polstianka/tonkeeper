package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class FormattedDecimal(
    val number: BigDecimal,
    val stringRepresentation: String = number.toPlainString()
) : Parcelable