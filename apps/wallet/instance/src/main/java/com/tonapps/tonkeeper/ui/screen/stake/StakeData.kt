package com.tonapps.tonkeeper.ui.screen.stake

import android.net.Uri
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.tlb.StringTlbConstructor
import com.tonapps.extensions.toByteArray
import com.tonapps.security.Security
import com.tonapps.security.hex
import com.tonapps.wallet.api.entity.TokenEntity
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.cell.buildCell
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef
import org.ton.tlb.storeTlb
import ton.SendMode
import ton.transfer.Transfer
import java.math.BigInteger

data class StakeData(
    var poolInfoCandidate: PoolInfo? = null,
    val poolInfo: PoolInfo? = null,
    val amount: String = "0",
    var preAddress: String = "",
    var preUnstake: Boolean = false
) {

    val comment: String? = null

    private val t: TokenEntity
        get() = TokenEntity.TON

    val icon: Uri
        get() = t.imageUri

    val tokenName: String
        get() = t.name

    val tokenAddress: String
        get() = t.address

    val tokenSymbol: String
        get() = t.symbol

    val isTon: Boolean
        get() = t.isTon

    val destination: AddrStd
        get() {
            return AddrStd.parse(poolInfo?.address ?: "")
        }

    val sendMode: Int
        get() {
            return SendMode.PAY_GAS_SEPARATELY.value + SendMode.IGNORE_ERRORS.value
        }

    val coins: Coins
        get() {
            if (isTon) {
                return amountR
            }
            return Coins.ofNano(Coin.toNano(0.064f))
        }

    val decimals: Int
        get() = t.decimals

    val amountR: Coins
        get() {
            val value = Coin.prepareValue(amount).toDoubleOrNull() ?: 0.0
            val nano = Coin.toNanoDouble(value, decimals)
            return Coins.ofNano(nano)
        }
    fun buildWalletTransfer(
        responseAddress: MsgAddressInt,
        stateInit: StateInit?,
    ): WalletTransfer {
        val builder = WalletTransferBuilder()
        builder.bounceable = true
        builder.destination = destination
        builder.body = if (preUnstake) buildStakingWithdrawalBody(responseAddress) else buildStakingDepositBody(responseAddress)
        builder.sendMode = sendMode
        builder.coins = coins
        builder.stateInit = stateInit
        return builder.build()
    }

    private fun buildStakingDepositBody(responseAddress: MsgAddressInt): Cell? {
        return when (poolInfo?.implementation) {
            PoolImplementationType.liquidTF -> createLiquidTfAddStakeCommand(newWalletQueryId())
            PoolImplementationType.whales -> createWhalesAddStakeCommand(newWalletQueryId())
            PoolImplementationType.tf -> createTfAddStakeCommand()
            else -> null
        }
    }

    private fun buildStakingWithdrawalBody(responseAddress: MsgAddressInt): Cell? {
        return when (poolInfo?.implementation) {
            PoolImplementationType.liquidTF -> createLiquidTfWithdrawStakeCell(newWalletQueryId())
            PoolImplementationType.whales -> createWhalesWithdrawStakeCell(newWalletQueryId())
            PoolImplementationType.tf -> createTfWithdrawStakeCell()
            else -> null
        }
    }

    private fun createLiquidTfAddStakeCommand(queryId: BigInteger = BigInteger.ZERO): Cell {
        return buildCell {
            storeUInt(0x47d54391, 32)
            storeUInt(queryId, 64)
            storeUInt(0x000000000005b7ce, 64)
        }
    }

    private fun createWhalesAddStakeCommand(queryId: BigInteger = BigInteger.ZERO): Cell {
        return buildCell {
            storeUInt(2077040623, 32)
            storeUInt(queryId, 64)
            storeTlb(Coins, Coins.ofNano(100000))
        }
    }

    private fun createTfAddStakeCommand(): Cell {
        return buildCell {
            storeUInt(0, 32)
            storeTlb(StringTlbConstructor, "d")
        }
    }

    private fun createLiquidTfWithdrawStakeCell(queryId: BigInteger = BigInteger.ZERO): Cell {
        val customPayload = buildCell {
            storeUInt(1, 1)
            storeUInt(0, 1)
        }

        return buildCell {
            storeUInt(0x595f07bc, 32)
            storeUInt(queryId, 64)
            storeTlb(Coins, coins)
            storeTlb(MsgAddressInt, destination)
            storeBit(true)
            storeRef(AnyTlbConstructor, CellRef(customPayload))
        }
    }

    private fun createWhalesWithdrawStakeCell(queryId: BigInteger = BigInteger.ZERO): Cell {
        return buildCell {
            storeUInt(3665837821, 32)
            storeUInt(queryId, 64)
            storeTlb(Coins, Coins.ofNano(100000))
            storeTlb(Coins, coins)
        }
    }

    private fun createTfWithdrawStakeCell(): Cell {
        return buildCell {
            storeUInt(0, 32)
            storeTlb(StringTlbConstructor, "w")
        }
    }

    private companion object {

        fun newWalletQueryId(): BigInteger {
            return try {
                val tonkeeperSignature = 0x546de4ef.toByteArray()
                val randomBytes = Security.randomBytes(4)
                val value = tonkeeperSignature + randomBytes
                val hexString = hex(value)
                BigInteger(hexString, 16)
            } catch (e: Throwable) {
                BigInteger.ZERO
            }
        }
    }
}