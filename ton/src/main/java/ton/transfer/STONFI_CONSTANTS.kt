package ton.transfer

import java.math.BigInteger

object STONFI_CONSTANTS {

    val RouterAddress = "0:779dcc815138d9500e449c5291e7f12738c23d575b5310000f6a253bd607384e"

    val TONProxyAddress = "0:8cdc1d7640ad5ee326527fc1ad0514f468b30dc84b0173f0e155f451b4e11f7c"

    val SWAP_JETTON_TO_JETTON_GasAmount = BigInteger.valueOf(265000000L)
    val SWAP_JETTON_TO_JETTON_ForwardGasAmount = BigInteger.valueOf(205000000L)

    val SWAP_JETTON_TO_TON_GasAmount = BigInteger.valueOf(185000000L)
    val SWAP_JETTON_TO_TON_ForwardGasAmount = BigInteger.valueOf(125000000L)

    val SWAP_TON_TO_JETTON_ForwardGasAmount = BigInteger.valueOf(215000000L)

}