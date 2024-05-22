package ton

import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.cell.buildCell
import org.ton.tlb.storeTlb

object Swap {

    fun swap(askAddress: MsgAddressInt, userAddressInt: MsgAddressInt, coins: Coins): Cell {
        return buildCell {
            storeUInt(0x25938561, 32)
            storeTlb(MsgAddressInt, askAddress)
            storeTlb(Coins, coins)
            storeTlb(MsgAddressInt, userAddressInt)
            storeBit(false)
        }
    }
}