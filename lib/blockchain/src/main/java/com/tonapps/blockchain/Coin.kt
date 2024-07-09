package com.tonapps.blockchain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

object Coin {

    const val TON_DECIMALS = 9

    private const val DEFAULT_DECIMALS = 18
    private const val BASE = 1000000000L

    fun parseJettonBalance(
        v: String,
        decimals: Int
    ): BigDecimal {
        val bigDecimal = safeBigDecimal(v)
        val divisor = BigDecimal.TEN.pow(decimals)
        val result = bigDecimal.divide(divisor, decimals, RoundingMode.DOWN)
        return result
    }

    private fun safeBigDecimal(
        value: String
    ): BigDecimal {
        return try {
            val string = prepareValue(value)
            BigDecimal(string)
        } catch (e: Throwable) {
            BigDecimal.ZERO
        }
    }

    fun prepareValue(value: String): String {
        var v = value.trim()
        if (v.endsWith(".") || v.startsWith(",")) {
            v = v.dropLast(1)
        }
        if (v.startsWith("0")) {
            v = v.dropWhile { it == '0' }
        }
        if (v.startsWith(".") || v.startsWith(",")) {
            v = "0$v"
        }
        if (v.contains(",")) {
            v = v.replace(",", ".")
        }
        if (v.isEmpty()) {
            v = "0"
        }
        return v
    }

    fun toNano(
        value: Float,
        decimals: Int = TON_DECIMALS
    ): Long {
        // old return (value * BASE).toLong()
        return (value * 10.0.pow(decimals)).toLong()
    }

    fun toCoins(
        value: Long,
        decimals: Int = TON_DECIMALS
    ): BigDecimal {
        return BigDecimal(value).movePointLeft(decimals)
    }

    fun toCoins(
        value: String,
        decimals: Int = TON_DECIMALS
    ): BigDecimal {
        return BigDecimal(value).movePointLeft(decimals)
    }

    fun toNano(
        value: BigDecimal,
        decimals: Int = TON_DECIMALS
    ): Long {
        return value.movePointRight(decimals)
            .setScale(0, RoundingMode.FLOOR)
            .longValueExact()
    }

}