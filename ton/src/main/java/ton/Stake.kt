package ton

import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.cell.buildCell
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef
import org.ton.tlb.storeTlb
import java.math.BigInteger

object Stake {

    fun stakeDepositLiquidTf(queryId: BigInteger = BigInteger.ZERO): Cell {
        return buildCell {
            storeUInt(0x47d54391, 32)
            storeUInt(queryId, 64)
        }
    }

    fun stakeDepositWhales(queryId: BigInteger = BigInteger.ZERO): Cell {
        return buildCell {
            storeUInt(2077040623, 32)
            storeUInt(queryId, 64)
            storeTlb(Coins, Coins.ofNano(100000))
        }
    }

    fun stakeDepositTf(): Cell {
        return buildCell {
            storeUInt(0, 32)
            storeBytes("d".toByteArray())
        }
    }

    fun unstakeLiquidTf(
        queryId: BigInteger = BigInteger.ZERO,
        amount: Coins,
        responseAddress: MsgAddressInt
    ): Cell {
        val body = buildCell {
            storeUInt(1, 1)
            storeUInt(0, 1)
        }
        return buildCell {
            storeUInt(0x595f07bc, 32)
            storeUInt(queryId, 64)
            storeTlb(Coins, amount)
            storeTlb(MsgAddressInt, responseAddress)
            storeBit(true)
            storeRef(AnyTlbConstructor, CellRef(body))
        }
    }

    fun unstakeWhales(queryId: BigInteger = BigInteger.ZERO): Cell {
        return buildCell {
            storeUInt(2077040623, 32)
            storeUInt(queryId, 64)
            storeTlb(Coins, Coins.ofNano(100000))
        }
    }

    fun unstakeTf(): Cell {
        return buildCell {
            storeUInt(0, 32)
            storeBytes("w".toByteArray())
        }
    }
}