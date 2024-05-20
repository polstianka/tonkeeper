package com.tonapps.tonkeeper.core

import com.tonapps.blockchain.Coin
import org.ton.block.Coins
import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.toCoins(decimals: Int = Coin.TON_DECIMALS): Coins {
    return movePointRight(decimals)
        .setScale(0, RoundingMode.FLOOR)
        .longValueExact()
        .let { Coins.ofNano(it) }
}