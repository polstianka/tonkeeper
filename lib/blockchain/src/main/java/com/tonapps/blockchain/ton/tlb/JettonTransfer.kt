package com.tonapps.blockchain.ton.tlb

import org.ton.block.Coins
import org.ton.block.MsgAddress
import org.ton.cell.Cell
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

data class JettonTransfer(
    val queryId: Long,
    val amount: Coins,
    val destination: MsgAddress,
    val responseDestination: MsgAddress?,
    val customPayload: Cell?,
    val forwardTonAmount: Coins,
    val forwardPayload: Cell?,
) : TlbObject {

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter =
        printer.type("JettonTransfer") {
            field("queryId", queryId)
            field("amount", amount)
            field("destination", destination)
            field("responseDestination", responseDestination)
            field("customPayload", customPayload)
            field("forwardTonAmount", forwardTonAmount)
            field("forwardPayload", forwardPayload)
        }

    companion object : TlbConstructorProvider<JettonTransfer> by JettonTransferTlbConstructor {

        @JvmStatic
        fun tlbCodec(): TlbCodec<JettonTransfer> = JettonTransferTlbConstructor
    }
}

private object JettonTransferTlbConstructor : TlbConstructor<JettonTransfer>(
    schema = "", id = null
) {

    override fun storeTlb(
        cellBuilder: CellBuilder, value: JettonTransfer,
    ) = cellBuilder {
        storeUInt(0xf8a7ea5, 32)
        storeUInt(value.queryId, 64)
        storeTlb(Coins, value.amount)
        storeTlb(MsgAddress, value.destination)
        value.responseDestination?.let { storeTlb(MsgAddress, value.responseDestination) }
        storeBit(false)
        storeTlb(Coins, value.forwardTonAmount)
        if (value.forwardPayload != null) {
            storeBit(true)
            storeRef(value.forwardPayload)
        } else {
            storeBit(false)
        }
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): JettonTransfer = cellSlice {
        loadUInt32()
        val queryId = loadUInt64().toLong()
        val amount = loadTlb(Coins)
        val destination = loadTlb(MsgAddress)
        val responseDestination = loadTlb(MsgAddress)
        loadBit()
        val forwardTonAmount = loadTlb(Coins)
        val forwardPayload = loadRef()
        JettonTransfer(
            queryId,
            amount,
            destination,
            responseDestination,
            null,
            forwardTonAmount,
            forwardPayload
        )
    }
}
