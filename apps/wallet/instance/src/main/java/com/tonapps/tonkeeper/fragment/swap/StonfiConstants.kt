package com.tonapps.tonkeeper.fragment.swap

import java.math.BigInteger

object StonfiConstants {
    const val ROUTER_ADDRESS = "0:779dcc815138d9500e449c5291e7f12738c23d575b5310000f6a253bd607384e"
    const val TON_PROXY_ADDRESS = "0:8cdc1d7640ad5ee326527fc1ad0514f468b30dc84b0173f0e155f451b4e11f7c"
    const val REFERRAL = "0:6C8EEF549F003F4F421D009AEDE507E3F7535667B639E98FD09B55C8FC0E23CB"
    const val REFERRAL_USER_FRIENDLY = "EQBsju9UnwA_T0IdAJrt5Qfj91NWZ7Y56Y_Qm1XI_A4jyzHr"
    const val BLOCKCHAIN_FEE = "0.08 - 0.25 TON"
    const val PROVIDER = "STON.fi"

    object SWAP_JETTON_TO_JETTON {
        val GAS_AMOUNT = BigInteger("265000000")
        val FORWARD_GAS_AMOUNT = BigInteger("205000000")
    }

    object SWAP_JETTON_TO_TON {
        val GAS_AMOUNT = BigInteger("185000000")
        val FORWARD_GAS_AMOUNT = BigInteger("125000000")
    }

    object SWAP_TON_TO_JETTON {
        val FORWARD_GAS_AMOUNT = BigInteger("215000000")
    }
}