package com.tonapps.blockchain.ton.tlb

import io.tonapi.models.PoolImplementationType
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.tlb.storeTlb
import java.math.BigInteger

object StakingTlb {

    private fun createWhalesAddStakeBody(
        queryId: BigInteger
    ) = CellBuilder.createCell {
        storeUInt(2077040623, 32)
        storeUInt(queryId, 64)
        storeTlb(Coins, Coins.ofNano(100000))
    }

    private fun createWhalesWithdrawStakeBody(
        queryId: BigInteger,
        amount: Coins
    ) = CellBuilder.createCell {
        storeUInt(3665837821, 32)
        storeUInt(queryId, 64)
        storeTlb(Coins, Coins.ofNano(100000))
        storeTlb(Coins, amount)
    }

    private fun createLiquidTfAddStakeBody(
        queryId: BigInteger,
    ) = CellBuilder.createCell {
        storeUInt(0x47d54391, 32)
        storeUInt(queryId, 64)
        storeUInt(0x000000000005b7ceL, 64)
    }

    private fun createLiquidTfWithdrawStakeBody(
        queryId: BigInteger,
        amount: Coins,
        addressInt: MsgAddressInt
    ) = CellBuilder.createCell {
        val customPayload = CellBuilder.createCell {
            storeUInt(1, 1)
            storeUInt(0, 1)
        }

        storeUInt(0x595f07bc, 32)
        storeUInt(queryId, 64)
        storeTlb(Coins, amount)
        storeTlb(MsgAddressInt, addressInt)
        storeBit(true)
        storeRef(customPayload)
    }

    private fun createTfAddStakeBody() = CellBuilder.createCell {
        storeUInt(0, 32)
        storeUInt('d'.code, 8)
    }

    private fun createTfWithdrawStakeBody() = CellBuilder.createCell {
        storeUInt(0, 32)
        storeUInt('w'.code, 8)
    }

    fun getDepositFee(type: PoolImplementationType): Coins {
        return when (type) {
            PoolImplementationType.whales -> Coins.ofNano(200_000_000)
            PoolImplementationType.liquidTF -> Coins.ofNano(1_000_000_000)
            PoolImplementationType.tf -> Coins.ofNano(1_000_000_000)
        }
    }

    fun getWithdrawalFee(type: PoolImplementationType): Coins {
        return when (type) {
            PoolImplementationType.whales -> Coins.ofNano(200_000_000)
            PoolImplementationType.liquidTF -> Coins.ofNano(1_000_000_000)
            PoolImplementationType.tf -> Coins.ofNano(1_000_000_000)
        }
    }

    data class MessageData(
        val to: MsgAddressInt,
        val payload: Cell,
        val gasAmount: Coins
    )

    fun buildStakeTxParams (
        poolType: PoolImplementationType,
        poolAddress: MsgAddressInt,
        queryId: BigInteger,
        amount: Coins,
    ): MessageData {
        val address = poolAddress
        val payload = when (poolType) {
            PoolImplementationType.whales -> createWhalesAddStakeBody(queryId)
            PoolImplementationType.liquidTF -> createLiquidTfAddStakeBody(queryId)
            PoolImplementationType.tf -> createTfAddStakeBody()
        }

        return MessageData(
            to = address,
            gasAmount = amount,
            payload = payload
        )
    }

    fun buildUnstakeTxParams (
        poolType: PoolImplementationType,
        poolAddress: MsgAddressInt,
        queryId: BigInteger,
        amount: Coins,
        useAllAmount: Boolean,
        responseAddress: MsgAddressInt,
        stakingJettonWalletAddress: MsgAddressInt?
    ): MessageData {
        val withdrawalFee = getWithdrawalFee(poolType);
        val address = stakingJettonWalletAddress ?: poolAddress

        val payload = when (poolType) {
            PoolImplementationType.whales -> {
                createWhalesWithdrawStakeBody(queryId, if (useAllAmount) Coins.ofNano(0) else amount)
            }
            PoolImplementationType.liquidTF -> {
                createLiquidTfWithdrawStakeBody(queryId, amount, responseAddress)

            }
            PoolImplementationType.tf -> {
                createTfWithdrawStakeBody()
            }
        }

        return MessageData(
            to = address,
            gasAmount = withdrawalFee,
            payload = payload
        )
    }
}