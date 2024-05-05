package ton.transfer

import org.ton.block.AddrNone
import org.ton.block.Coins
import org.ton.block.MsgAddress
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.cell.buildCell
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef
import org.ton.tlb.storeTlb
import java.math.BigInteger

object Transfer {

    fun text(text: String?): Cell? {
        if (text.isNullOrEmpty()) {
            return null
        }

        return buildCell {
            storeUInt(0, 32)
            storeBytes(text.toByteArray())
        }
    }

    fun body(body: Any?): Cell? {
        if (body == null) {
            return null
        }
        return when (body) {
            is String -> text(body)
            is Cell -> body
            else -> null
        }
    }

    fun jetton(
        coins: Coins,
        toAddress: MsgAddressInt,
        responseAddress: MsgAddressInt?,
        queryId: BigInteger = BigInteger.ZERO,
        forwardAmount: Long = 1L,
        body: Any? = null,
    ): Cell {
        val payload = body(body)

        return buildCell {
            storeUInt(0xf8a7ea5, 32)
            storeUInt(queryId, 64)
            storeTlb(Coins, coins)
            storeTlb(MsgAddressInt, toAddress)
            storeTlb(MsgAddress, responseAddress ?: AddrNone)
            storeBit(false)
            storeTlb(Coins, Coins.ofNano(forwardAmount))
            if (payload == null) {
                storeBit(false)
            } else {
                storeBit(true)
                storeRef(AnyTlbConstructor, CellRef(payload))
            }
        }
    }

    fun swap(askAddress: MsgAddressInt, userAddressInt: MsgAddressInt, coins: Coins): Cell {
        return buildCell {
            storeUInt(0x25938561, 32)
            storeTlb(MsgAddressInt, askAddress)
            storeTlb(Coins, coins)
            storeTlb(MsgAddressInt, userAddressInt)
            storeBit(false)
        }
    }

    fun nft(
        newOwnerAddress: MsgAddressInt,
        excessesAddress: MsgAddressInt,
        queryId: Long = 0L,
        forwardAmount: Long = 1L,
        body: Any? = null,
    ): Cell {
        val payload = body(body)

        return buildCell {
            storeUInt(0x5fcc3d14, 32)
            storeUInt(queryId, 64)
            storeTlb(MsgAddressInt, newOwnerAddress)
            storeTlb(MsgAddressInt, excessesAddress)
            storeBit(false)
            storeTlb(Coins, Coins.ofNano(forwardAmount))
            storeBit(payload != null)
            payload?.let {
                storeRef(AnyTlbConstructor, CellRef(it))
            }
        }
    }

}