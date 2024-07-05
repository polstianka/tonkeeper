package com.tonapps.tonkeeper.ui.screen.swapnative

import android.util.Log
import com.tonapps.extensions.toByteArray
import com.tonapps.security.Security
import com.tonapps.security.hex
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import ton.transfer.STONFI_CONSTANTS
import ton.transfer.Transfer
import java.math.BigInteger

data class SwapData(
    val userWalletAddress: AddrStd,
    val minAskAmount: BigInteger,
    val offerAmount: BigInteger,
    val jettonFromWalletAddress: String,
    val jettonToWalletAddress: String,
    val forwardAmount: BigInteger,
    val attachedAmount: BigInteger,
    val referralAddress: String? = null,
) {

    val sendMode: Int = 3

    /*val amount: Coins
        get() {
            val value = Coin.prepareValue(offerAmount).toDoubleOrNull() ?: 0.0
            val nano = Coin.toNanoDouble(value, decimals)
            return Coins.ofNano(nano)
        }*/

    fun buildSwapTransfer(
        responseAddress: MsgAddressInt,
        stateInit: StateInit?,
    ): WalletTransfer {
        val builder = WalletTransferBuilder()
        builder.bounceable = true
        builder.destination = MsgAddressInt.parse(jettonFromWalletAddress)
        builder.body = buildSwapTransferBody(responseAddress)
        builder.sendMode = sendMode
        builder.coins = Coins.ofNano(attachedAmount)
        builder.stateInit = stateInit

        Log.d("swap-log", "#4 Transfer.jetton ${builder.body} ")
        Log.d("swap-log", "#5 builder ${builder} ")

        return builder.build()
    }

    private fun buildSwapTransferBody(
        responseAddress: MsgAddressInt,
    ): Cell {

        val stonfiSwapCell = Transfer.swap(
            assetToSwap = MsgAddressInt.parse(jettonToWalletAddress),
            minAskAmount = minAskAmount,
            userWalletAddress = responseAddress,
            referralAddress = referralAddress
        )

        Log.d("swap-log", "#3 Transfer.swap ${stonfiSwapCell} ")

        return Transfer.jetton(
            coins = Coins.ofNano(offerAmount),
            toAddress = MsgAddressInt.parse(STONFI_CONSTANTS.RouterAddress),
            responseAddress = responseAddress,
            queryId = newWalletQueryId(),
            forwardAmount = forwardAmount,
            body = stonfiSwapCell,
        )

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