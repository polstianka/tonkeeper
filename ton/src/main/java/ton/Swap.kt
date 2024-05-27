package ton

import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.cell.buildCell
import org.ton.tlb.storeTlb

object Swap {

    const val PROXY_TON = "EQCM3B12QK1e4yZSf8GtBRT0aLMNyEsBc_DhVfRRtOEffLez"
    const val STONFI_ROUTER_ADDRESS =
        "0:779dcc815138d9500e449c5291e7f12738c23d575b5310000f6a253bd607384e"

    const val TON_TO_JETTON_FORWARD_GAS = 215000000L

    const val JETTON_TO_TON_FORWARD_GAS = 125000000L
    const val JETTON_TO_TON_GAS = 185000000L

    const val JETTON_TO_JETTON_FORWARD_GAS = 265000000L
    const val JETTON_TO_JETTON_GAS = 205000000L


    fun swapTonToJetton(
        toAddress: MsgAddressInt,
        userAddressInt: MsgAddressInt,
        coins: Coins
    ): Cell {
        return buildCell {
            storeUInt(0x25938561, 32)
            storeTlb(MsgAddressInt, toAddress)
            storeTlb(Coins, coins)
            storeTlb(MsgAddressInt, userAddressInt)
            storeBit(false)
        }
    }
}