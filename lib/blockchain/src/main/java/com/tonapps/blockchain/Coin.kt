package com.tonapps.blockchain

import org.ton.bigint.BigInt
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

object Coin {

    const val TON_DECIMALS = 9

    @Deprecated("""
        32-bit float isn't suitable for coins,
        e.g. this method will start providing invalid results for amounts above 2^24, which is just 16.7M coins,
        and loses precision in any kind of arithmetic operation.
    """)
    fun parseJettonBalance(
        v: String,
        decimals: Int
    ): Float {
        val bigDecimal = try {
            val string = prepareValue(v)
            BigDecimal(string)
        } catch (e: Throwable) {
            BigDecimal.ZERO
        }
        val divisor = BigDecimal.TEN.pow(decimals)
        val result = bigDecimal.divide(divisor, decimals, RoundingMode.DOWN)
        return result.toFloat()
    }

    @Deprecated("""
        This method:
            1. Isn't well optimized
            2. For user input it doesn't take in mind different locales 
            3. For API responses, when response format is determined, it is a rudimentary overhead.
        
        Use AmountInputView.doOnDecimalValueChange for user input.
        and String?.prepareBigDecimal for everything else.
    """)
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

    @Deprecated("""
        32-bit float isn't suitable for coins,
        e.g. this method will start providing invalid results for amounts above 2^24, which is just 16.7M coins,
        and loses precision in any kind of arithmetic operation.
    """, replaceWith = ReplaceWith("toNano(bigDecimal, decimals)"))
    fun toNano(
        coins: Float,
        decimals: Int = TON_DECIMALS
    ): Long {
        return (coins * 10.0.pow(decimals)).toLong()
    }

    @Deprecated("""
        32-bit float isn't suitable for coins,
        e.g. this method will start providing invalid results for amounts above 2^24, which is just 16.7M coins,
        and loses precision in any kind of arithmetic operation.
        
        64-bit signed long is not suitable for nano representation,
        and will fail for large inputs or some coins.
    """, replaceWith = ReplaceWith("toNano(nanoString, decimals)"))
    fun toCoins(
        nano: Long,
        decimals: Int = TON_DECIMALS
    ): Float {
        return nano / 10.0.pow(decimals).toFloat()
    }

    @Deprecated("""
        64-bit signed long is not suitable for nano representation,
        and will fail for large inputs or some coins.
    """, replaceWith = ReplaceWith("toNanoString(coins, decimals) or toNanoInt(coins, decimals)"))
    fun toNano(
        coins: BigDecimal,
        decimals: Int = TON_DECIMALS
    ): Long {
        return (coins * BigDecimal.TEN.pow(decimals)).toLong()
    }

    fun toNanoInt(
        coins: BigDecimal,
        decimals: Int = TON_DECIMALS
    ): BigInt =
        coins.movePointRight(decimals).stripTrailingZeros().toBigIntegerExact()

    fun toNanoString(
        coins: BigDecimal,
        decimals: Int = TON_DECIMALS
    ): String =
        coins.movePointRight(decimals).stripTrailingZeros().toPlainString()

    fun toCoins(
        nano: BigInt,
        decimals: Int = TON_DECIMALS
    ): BigDecimal =
        toCoins(nano.toBigDecimal(), decimals)

    fun toCoins(
        nano: BigDecimal,
        decimals: Int = TON_DECIMALS
    ): BigDecimal =
        nano.movePointLeft(decimals).stripTrailingZeros()

    fun toCoins(
        nano: String,
        decimals: Int = TON_DECIMALS
    ): BigDecimal =
        toCoins(BigDecimal(nano), decimals)

}