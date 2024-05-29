package com.tonapps.tonkeeper.ui.screen.swap.stonfi

import com.tonapps.blockchain.Coin
import org.ton.bigint.BigInt
import java.math.BigDecimal

enum class DEX_VERSION(val version: String) {
    v1("v1")
}

object Stonfi {
    const val RouterAddress: String =
        "0:779dcc815138d9500e449c5291e7f12738c23d575b5310000f6a253bd607384e"

    const val TONProxyAddress: String =
        "0:8cdc1d7640ad5ee326527fc1ad0514f468b30dc84b0173f0e155f451b4e11f7c"

    object SWAP_JETTON_TO_JETTON {
        val GasAmount by lazy {
            BigInt("265000000")
        }
        val ForwardGasAmount by lazy {
            BigInt("205000000")
        }
    }

    object SWAP_JETTON_TO_TON {
        val GasAmount by lazy {
            BigInt("185000000")
        }
        val ForwardGasAmount by lazy {
            BigInt("125000000")
        }
    }

    object SWAP_TON_TO_JETTON {
        val ForwardGasAmount by lazy {
            BigInt("215000000")
        }
    }

    private val extraTonFees by lazy {
        Coin.toCoins(SWAP_TON_TO_JETTON.ForwardGasAmount)
    }

    fun extraFees(isTon: Boolean): BigDecimal {
        return if (isTon) extraTonFees else BigDecimal.ZERO
    }
}