package com.tonapps.tonkeeper.helper

import com.tonapps.icu.CurrencyFormatter
import com.tonapps.wallet.api.entity.TokenEntity
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class Coin2 private constructor(val value: BigInteger) {
    class Input private constructor(
        val input: String,
        val coin: Coin2?
    ) {
        val isNotEmptyAndParseError: Boolean get() = input.isNotEmpty() && coin == null

        companion object {
            val EMPTY = Input(input = "", coin = null)

            fun parse(input: String, decimals: Int): Input {
                return Input(input = input, coin = fromInput(input, decimals))
            }
        }
    }

    fun toDecimal(decimals: Int): BigDecimal {
        return BigDecimal(value).divide(BigDecimal.TEN.pow(decimals), decimals, RoundingMode.DOWN)
    }

    fun toString(decimals: Int): String {
        return toDecimal(decimals).stripTrailingZeros().toPlainString()
    }

    fun toFloat(decimals: Int): Float {
        return toDecimal(decimals).toFloat()
    }

    fun toNanoString(): String = value.toString()

    fun format(token: TokenEntity): String {
        return StringBuilder(CurrencyFormatter.format(value = toDecimal(token.decimals))).append(' ').append(token.symbol).toString()
    }

    companion object {
        val ZERO = Coin2(BigInteger.ZERO)

        fun fromNano(input: String): Coin2? {
            return try {
                val parsedValue = BigInteger(input)
                Coin2(parsedValue)
            } catch (e: NumberFormatException) {
                null
            }
        }

        fun fromNano(input: BigInteger): Coin2 {
            return Coin2(input)
        }

        fun fromNano(input: Long): Coin2 {
            return Coin2(BigInteger.valueOf(input))
        }

        fun fromInput(input: String, decimals: Int): Coin2? {
            return try {
                val parsedValue = BigDecimal(input)
                val scaledValue = parsedValue.movePointRight(decimals).toBigInteger()
                Coin2(scaledValue)
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}