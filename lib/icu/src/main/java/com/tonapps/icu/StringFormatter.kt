package com.tonapps.icu

import java.math.BigDecimal
import java.math.RoundingMode

object StringFormatter {

    fun truncateToFourDecimalPlaces(value: String): String {
        val bigDecimalValue = BigDecimal(value)
        val truncatedValue = bigDecimalValue.setScale(4, RoundingMode.DOWN)
        return truncatedValue.toPlainString()
    }

}