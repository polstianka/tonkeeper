package com.tonapps.blockchain.ton.tlb

import org.ton.block.Coins
import org.ton.block.MsgAddress
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbObject
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb

data class SwapBody(
    val askJettonWalletAddress: MsgAddress,
    val minAskAmount: Coins,
    val userWalletAddress: MsgAddress,
    val referralAddress: MsgAddress? = null
) : TlbObject {

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("SwapBody") {
        field("askJettonWalletAddress", askJettonWalletAddress)
        field("minAskAmount", minAskAmount)
        field("userWalletAddress", userWalletAddress)
        field("referralAddress", referralAddress)
    }

    companion object : TlbConstructorProvider<SwapBody> by SwapBodyTlbConstructor {

        @JvmStatic
        fun tlbCodec(): TlbCodec<SwapBody> = SwapBodyTlbConstructor
    }
}

private object SwapBodyTlbConstructor : TlbConstructor<SwapBody>(
    schema = "", id = null
) {

    override fun storeTlb(
        cellBuilder: CellBuilder, value: SwapBody
    ) = cellBuilder {
        storeUInt(0x25938561, 32)
        storeTlb(MsgAddress, value.askJettonWalletAddress)
        storeTlb(Coins, value.minAskAmount)
        storeTlb(MsgAddress, value.userWalletAddress)
        if (value.referralAddress != null) {
            storeBit(true)
            storeTlb(MsgAddress, value.referralAddress)
        } else {
            storeBit(false)
        }
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): SwapBody = cellSlice {
        loadUInt32()
        val askJettonWalletAddress = loadTlb(MsgAddress)
        val minAskAmount = loadTlb(Coins)
        val userWalletAddress = loadTlb(MsgAddress)
        val referralAddress = if (loadBit()) loadTlb(MsgAddress) else null

        SwapBody(
            askJettonWalletAddress = askJettonWalletAddress,
            minAskAmount = minAskAmount,
            userWalletAddress = userWalletAddress,
            referralAddress = referralAddress
        )
    }
}
