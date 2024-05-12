package com.tonapps.tonkeeper.helper

import android.icu.text.DecimalFormat
import java.math.BigDecimal

object NumberFormatter {
    private val df = DecimalFormat("#0.##")

    fun format(n: Float?): String {
        return df.format(n)
    }

    fun format(n: BigDecimal?): String {
        return df.format(n)
    }
}