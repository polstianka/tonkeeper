package com.tonapps.wallet.data.core.entity

import android.os.Parcelable
import com.tonapps.blockchain.ton.extensions.safeParseCell
import com.tonapps.blockchain.ton.extensions.toTlb
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

@Parcelize
data class RawMessageEntity(
    val addressValue: String,
    val amount: Long,
    val stateInitValue: String?,
    val payloadValue: String
): Parcelable {

    val address: AddrStd
        get() = AddrStd.parse(addressValue)

    val coins: Coins
        get() = Coins.ofNano(amount)

    val stateInit: StateInit?
        get() = stateInitValue?.toTlb()

    val payload: Cell
        get() = payloadValue.safeParseCell() ?: Cell()

    fun getWalletTransfer(excessesAddress: AddrStd? = null): WalletTransfer {
        val builder = WalletTransferBuilder()
        builder.stateInit = stateInit
        builder.destination = address
        builder.body = if (excessesAddress != null) rebuildBodyWithCustomExcessesAccount(payload, excessesAddress) else payload
        // builder.bounceable = address.isBounceable()
        builder.coins = coins
        return builder.build()
    }

    private fun rebuildBodyWithCustomExcessesAccount(body: Cell, excessesAddress: AddrStd): Cell {
        val slice = body.beginParse()
        val opCode = slice.loadUInt32()
        var builder = CellBuilder.beginCell()
        return when (opCode.toInt()) {
            // stonfi swap
            0x25938561 -> {
                builder
                    .storeUInt(0x25938561, 32)
                    .storeTlb(MsgAddressInt, slice.loadTlb(AddrStd.tlbCodec()))
                    .storeTlb(Coins, slice.loadTlb(Coins.tlbCodec()))
                    .storeTlb(MsgAddressInt, slice.loadTlb(AddrStd.tlbCodec()))

                if (slice.loadBit()) {
                    slice.loadTlb(AddrStd.tlbCodec())
                }
                slice.endParse()

                builder
                    .storeBit(true)
                    .storeTlb(MsgAddressInt, excessesAddress)

                builder.endCell()
            }
            // nft transfer
            0x5fcc3d14 -> body
            // jetton transfer
            0xf8a7ea5 -> body
            else -> body
        }
    }

    constructor(json: JSONObject) : this(
        json.getString("address"),
        parseAmount(json.get("amount")),
        json.optString("stateInit"),
        json.optString("payload")
    )

    private companion object {

        private fun parseAmount(value: Any): Long {
            if (value is Long) {
                return value
            }
            return value.toString().toLong()
        }
    }

}