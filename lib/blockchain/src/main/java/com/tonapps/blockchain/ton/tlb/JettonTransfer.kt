package com.tonapps.blockchain.ton.tlb

import org.ton.block.Coins
import org.ton.block.MsgAddressInt
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
import java.math.BigInteger

data class JettonTransfer(
    val queryId: BigInteger,
    val coins: Coins,
    val toAddress: MsgAddressInt,
    val responseAddress: MsgAddressInt,
    val forwardAmount: Coins,
    val forwardPayload: Cell?
) : TlbObject {

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("JettonTransfer") {
        field("queryId", queryId)
        field("coins", coins)
        field("toAddress", toAddress)
        field("responseAddress", responseAddress)
        field("forwardAmount", forwardAmount)
        field("forward payload", forwardPayload)
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
        cellBuilder: CellBuilder, value: JettonTransfer
    ) = cellBuilder {
        storeUInt(0xf8a7ea5, 32)
        storeUInt(value.queryId, 64)
        storeTlb(Coins, value.coins)
        storeTlb(MsgAddressInt, value.toAddress)
        storeTlb(MsgAddressInt, value.responseAddress)
        storeBit(false)
        storeTlb(Coins, value.forwardAmount)
        if (value.forwardPayload == null) {
            storeBit(false)
        } else {
            storeBit(true)
            storeRef(value.forwardPayload)
        }
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): JettonTransfer = cellSlice {
        loadUInt32()
        val queryId = loadUInt64().toLong().toBigInteger()
        val coins = loadTlb(Coins)
        val toAddress = loadTlb(MsgAddressInt)
        val responseAddress = loadTlb(MsgAddressInt)
        loadBit()
        val forwardAmount = loadTlb(Coins)
        val comment = if (loadBit()) loadRef() else null
        JettonTransfer(queryId, coins, toAddress, responseAddress, forwardAmount, comment)
    }
}
