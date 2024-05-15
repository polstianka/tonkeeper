package com.tonapps.tonkeeper.fragment.stake.domain.model

import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.send.TransactionData
import com.tonapps.tonkeeper.fragment.stake.domain.CellProducer
import org.ton.bitstring.BitString
import org.ton.block.Coins
import org.ton.cell.Cell
import org.ton.cell.buildCell
import org.ton.tlb.storeTlb

enum class StakingServiceType {
    WHALES,
    TF,
    LIQUID_TF
}

val StakingServiceType.addStakeCellProducer: CellProducer
    get() = when (this) {
        StakingServiceType.WHALES -> WhaleAddStakeCellProducer
        StakingServiceType.TF -> TFAddStakeCellProducer
        StakingServiceType.LIQUID_TF -> LiquidTFAddStakeCellProducer
    }

val StakingServiceType.withdrawalFee: Long
    get() = when (this) {
        StakingServiceType.WHALES -> Coin.toNano(0.2f)
        StakingServiceType.TF -> Coin.toNano(1f)
        StakingServiceType.LIQUID_TF -> Coin.toNano(1f)
    }

private object WhaleAddStakeCellProducer : CellProducer {
    override fun produce(): Cell {
        return buildCell {
            storeUInt(2077040623, 32)
            storeUInt(TransactionData.getWalletQueryId(), 64)
            storeTlb(Coins, Coins.ofNano(100_000))
        }
    }
}

private object LiquidTFAddStakeCellProducer : CellProducer {
    override fun produce(): Cell {
        return buildCell {
            storeUInt(0x47d54391, 32)
            storeUInt(TransactionData.getWalletQueryId(), 64)
            storeUInt(0x000000000005b7ce, 64)
        }
    }
}

private object TFAddStakeCellProducer : CellProducer {
    override fun produce(): Cell {
        return buildCell {
            storeUInt(0, 32)
            storeBits(BitString("d"))
        }
    }
}