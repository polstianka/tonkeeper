package com.tonapps.tonkeeper.fragment.stake.domain.model

import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.send.TransactionData
import com.tonapps.tonkeeper.fragment.stake.domain.CellProducer
import com.tonapps.tonkeeper.fragment.stake.domain.StakingTransactionType
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.cell.buildCell
import org.ton.tlb.storeTlb
import java.math.BigDecimal

enum class StakingServiceType {
    WHALES,
    TF,
    LIQUID_TF
}

fun StakingServiceType.getAmount(
    direction: StakingTransactionType,
    amount: BigDecimal,
    isSendAll: Boolean,
): BigDecimal {
    return when (this) {
        StakingServiceType.WHALES -> when (direction) {
            StakingTransactionType.DEPOSIT -> amount
            StakingTransactionType.UNSTAKE -> withdrawalFee
        }

        StakingServiceType.TF -> when (direction) {
            StakingTransactionType.DEPOSIT -> amount
            StakingTransactionType.UNSTAKE -> withdrawalFee
        }
        StakingServiceType.LIQUID_TF -> when (direction) {
            StakingTransactionType.DEPOSIT -> when (isSendAll) {
                true -> amount
                false -> amount + withdrawalFee
            }
            StakingTransactionType.UNSTAKE -> withdrawalFee
        }
    }
}

fun StakingPool.getDestinationAddress(
    direction: StakingTransactionType,
): MsgAddressInt? {
    return if (liquidJettonMaster != null && direction == StakingTransactionType.UNSTAKE) {
        null
    } else {
        MsgAddressInt.parse(address)
    }
}

fun StakingServiceType.getCellProducer(
    direction: StakingTransactionType,
    amount: BigDecimal,
    walletAddress: MsgAddressInt,
    isSendAll: Boolean
): CellProducer {
    return when (direction) {
        StakingTransactionType.DEPOSIT -> addStakeCellProducer
        StakingTransactionType.UNSTAKE -> getWithdrawCellProducer(
            Coins.Companion.ofNano(Coin.toNano(amount)),
            walletAddress,
            isSendAll
        )
    }
}

private fun StakingServiceType.getWithdrawCellProducer(
    amount: Coins,
    walletAddress: MsgAddressInt,
    isSendAll: Boolean
): CellProducer {
    return when (this) {
        StakingServiceType.WHALES -> WhaleWithdrawStakeCellProducer(amount, isSendAll)
        StakingServiceType.TF -> TfWithdrawStakeCellProducer
        StakingServiceType.LIQUID_TF -> LiquidTfWithdrawStakeCellProducer(amount, walletAddress)
    }
}

private val StakingServiceType.addStakeCellProducer: CellProducer
    get() = when (this) {
        StakingServiceType.WHALES -> WhaleAddStakeCellProducer
        StakingServiceType.TF -> TFAddStakeCellProducer
        StakingServiceType.LIQUID_TF -> LiquidTFAddStakeCellProducer
    }

val StakingServiceType.withdrawalFee: BigDecimal
    get() = when (this) {
        StakingServiceType.WHALES -> BigDecimal("0.2")
        StakingServiceType.TF -> BigDecimal.ONE
        StakingServiceType.LIQUID_TF -> BigDecimal.ONE
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
            storeUInt('d'.toByte(), 8)
        }
    }
}

private class WhaleWithdrawStakeCellProducer(
    private val amount: Coins,
    private val isSendAll: Boolean
) : CellProducer {

    override fun produce(): Cell {
        return buildCell {
            // opcode
            storeUInt(3665837821, 32)
            // query id
            storeUInt(TransactionData.getWalletQueryId(), 64)
            // gas
            storeTlb(Coins.tlbCodec(), Coins.ofNano(100_000L))
            // amount
            if (isSendAll) {
                storeTlb(Coins.tlbCodec(), Coins.ofNano(0L))
            } else {
                storeTlb(Coins.tlbCodec(), amount)
            }
        }
    }
}

private class LiquidTfWithdrawStakeCellProducer(
    private val amount: Coins,
    private val walletAddress: MsgAddressInt
) : CellProducer {
    override fun produce(): Cell {
        val customPayload = buildCell {
            storeUInt(1, 1)
            storeUInt(0, 1)
        }
        return buildCell {
            // opcode
            storeUInt(0x595f07bc, 32)
            // query id
            storeUInt(TransactionData.getWalletQueryId(), 64)
            // amount
            storeTlb(Coins.tlbCodec(), amount)
            // address
            storeTlb(MsgAddressInt.tlbCodec(), walletAddress)
            storeBit(true)
            storeRef(customPayload)
        }
    }
}

private object TfWithdrawStakeCellProducer : CellProducer {
    override fun produce(): Cell {
        return buildCell {
            storeUInt(0, 32)
            storeUInt('w'.toByte(), 8)
        }
    }
}