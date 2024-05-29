package com.tonapps.blockchain.ton.tlb

import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.tlb.storeTlb
import java.math.BigInteger

object StonFiTlb {
    val OP_SWAP = 0x25938561

    data class GasConstant(val gasAmount: Coins, val forwardGasAmount: Coins)

    val GAS_SWAP_JETTON_TO_JETTON = GasConstant(
        gasAmount = Coins.ofNano(265000000L),
        forwardGasAmount = Coins.ofNano(205000000L)
    )

    val GAS_SWAP_JETTON_TO_TON = GasConstant(
        gasAmount = Coins.ofNano(185000000L),
        forwardGasAmount = Coins.ofNano(125000000L)
    )

    val GAS_SWAP_TON_TO_JETTON = Coins.ofNano(215000000L)

    private fun createSwapBody(
        userWalletAddress: MsgAddressInt,
        minAskAmount: Coins,
        askJettonWalletAddress: MsgAddressInt,
        referralAddress: MsgAddressInt?
    ) = CellBuilder.createCell {
        storeUInt(OP_SWAP, 32)
        storeTlb(MsgAddressInt, askJettonWalletAddress)
        storeTlb(Coins, minAskAmount)
        storeTlb(MsgAddressInt, userWalletAddress)
        storeBit(referralAddress != null)
        referralAddress?.let {
            storeTlb(MsgAddressInt, it)
        }
    }

    private fun createJettonTransferMessage(
        queryId: BigInteger,
        amount: Coins,
        destination: MsgAddressInt,
        responseDestination: MsgAddressInt?,
        customPayload: Cell?,
        forwardTonAmount: Coins,
        forwardPayload: Cell?
    ) = CellBuilder.createCell {
        storeUInt(0xf8a7ea5, 32)
        storeUInt(queryId, 64)
        storeTlb(Coins, amount)
        storeTlb(MsgAddressInt, destination)

        if (responseDestination != null) {
            storeTlb(MsgAddressInt, responseDestination)
        } else {
            storeUInt(0, 2)
        }

        storeBit(customPayload != null)
        customPayload?.let { storeRef(it) }

        storeTlb(Coins, forwardTonAmount)

        storeBit(forwardPayload != null)
        forwardPayload?.let { storeRef(it) }
    }

    data class MessageData(val to: MsgAddressInt, val payload: Cell, val gasAmount: Coins)

    fun buildSwapTonToJettonTxParams(
        routerAddress: MsgAddressInt,
        userWalletAddress: MsgAddressInt,
        proxyTonWalletAddress: MsgAddressInt,
        askJettonWalletAddress: MsgAddressInt,
        offerAmount: Coins,
        minAskAmount: Coins,
        referralAddress: MsgAddressInt?,
        forwardGasAmount: Coins?,
        queryId: BigInteger
    ): MessageData {
        val forwardPayload = createSwapBody(
            userWalletAddress = userWalletAddress,
            minAskAmount = minAskAmount,
            askJettonWalletAddress = askJettonWalletAddress,
            referralAddress = referralAddress
        )

        val forwardTonAmount = forwardGasAmount ?: GAS_SWAP_TON_TO_JETTON
        val payload = createJettonTransferMessage(
            queryId = queryId,
            amount = offerAmount,
            destination = routerAddress,
            forwardTonAmount = forwardTonAmount,
            forwardPayload = forwardPayload,
            customPayload = null,
            responseDestination = null
        )

        val gasAmount = Coins.ofNano(offerAmount.amount.value + forwardTonAmount.amount.value)
        return MessageData(to = proxyTonWalletAddress, payload = payload, gasAmount = gasAmount)
    }

    fun buildSwapJettonToJettonTxParams(
        routerAddress: MsgAddressInt,
        userWalletAddress: MsgAddressInt,
        offerJettonWalletAddress: MsgAddressInt,
        askJettonWalletAddress: MsgAddressInt,
        offerAmount: Coins,
        minAskAmount: Coins,
        referralAddress: MsgAddressInt?,
        gasAmount: Coins?,
        forwardGasAmount: Coins?,
        queryId: BigInteger
    ): MessageData {
        val forwardPayload = createSwapBody(
            userWalletAddress = userWalletAddress,
            minAskAmount = minAskAmount,
            askJettonWalletAddress = askJettonWalletAddress,
            referralAddress = referralAddress
        )

        val forwardTonAmount = forwardGasAmount ?: GAS_SWAP_JETTON_TO_JETTON.forwardGasAmount
        val payload = createJettonTransferMessage(
            queryId = queryId,
            amount = offerAmount,
            destination = routerAddress,
            responseDestination = userWalletAddress,
            forwardTonAmount = forwardTonAmount,
            forwardPayload = forwardPayload,
            customPayload = null
        )

        val mGasAmount = gasAmount ?: GAS_SWAP_JETTON_TO_JETTON.gasAmount
        return MessageData(to = offerJettonWalletAddress, payload = payload, gasAmount = mGasAmount)
    }

    fun buildSwapJettonToTonTxParams(
        routerAddress: MsgAddressInt,
        userWalletAddress: MsgAddressInt,
        offerJettonWalletAddress: MsgAddressInt,
        proxyTonWalletAddress: MsgAddressInt,
        offerAmount: Coins,
        minAskAmount: Coins,
        referralAddress: MsgAddressInt?,
        gasAmount: Coins?,
        forwardGasAmount: Coins?,
        queryId: BigInteger
    ): MessageData {
        return buildSwapJettonToJettonTxParams(
            routerAddress = routerAddress,
            userWalletAddress = userWalletAddress,
            offerJettonWalletAddress = offerJettonWalletAddress,
            askJettonWalletAddress = proxyTonWalletAddress,
            gasAmount = gasAmount ?: GAS_SWAP_JETTON_TO_TON.gasAmount,
            forwardGasAmount = forwardGasAmount ?: GAS_SWAP_JETTON_TO_TON.forwardGasAmount,
            offerAmount = offerAmount,
            minAskAmount = minAskAmount,
            referralAddress = referralAddress,
            queryId = queryId
        )
    }
}