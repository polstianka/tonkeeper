package com.tonapps.blockchain

import android.util.Log
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.pow

object Coin {

    const val TON_DECIMALS = 9

    private const val DEFAULT_DECIMALS = 18
    private const val BASE = 1000000000L

    fun parseJettonBalance(
        v: String,
        decimals: Int
    ): Double {
        val bigDecimal = safeBigDecimal(v)
        val divisor = BigDecimal.TEN.pow(decimals)
        val result = bigDecimal.divide(divisor, decimals, RoundingMode.DOWN)
        return result.toDouble()
    }

    /*fun parseFloat(
        value: String,
        decimals: Int = TON_DECIMALS
    ): Float {
        val floatValue = prepareValue(value).toFloatOrNull() ?: return 0f
        val formatString = "%.${decimals}f"
        val formattedString = formatString.format(floatValue)
        return formattedString.toFloatOrNull() ?: 0f
    }*/

    fun bigDecimal(
        value: String,
        decimals: Int = TON_DECIMALS
    ): BigDecimal {
        return try {
            BigDecimal(value).movePointRight(decimals)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
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

    fun toNanoDouble(
        value: Double,
        decimals: Int = TON_DECIMALS
    ): Long {
        // old return (value * BASE).toLong()
        return (value * 10.0.pow(decimals)).toLong()
    }

    fun toCoins(
        value: Long,
        decimals: Int = TON_DECIMALS
    ): Float {
        // old return value / BASE.toFloat()
        return value / 10.0.pow(decimals).toFloat()
    }

    fun toCoinsDouble(
        value: Long,
        decimals: Int = TON_DECIMALS
    ): Double {
        return value / 10.0.pow(decimals)
    }

}