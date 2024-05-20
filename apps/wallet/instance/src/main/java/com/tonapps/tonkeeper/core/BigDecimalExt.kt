package com.tonapps.tonkeeper.core

import com.tonapps.blockchain.Coin
import org.ton.block.Coins
import java.math.BigDecimal

fun BigDecimal.toCoins(decimals: Int = Coin.TON_DECIMALS): Coins {
    return movePointRight(decimals)
        .setScale(0)
        .longValueExact()
        .let { Coins.ofNano(it) }
}