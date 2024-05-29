package com.tonapps.tonkeeper.sign

import android.os.Parcelable
import com.tonapps.blockchain.ton.extensions.parseCell
import com.tonapps.blockchain.ton.extensions.toTlb
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import org.ton.bigint.BigInt
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder

@Parcelize
data class RawMessageEntity(
    val addressValue: String,
    val amount: BigInt,
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
        get() = payloadValue.parseCell()

    val walletTransfer: WalletTransfer by lazy {
        val builder = WalletTransferBuilder()
        builder.stateInit = stateInit
        builder.destination = address
        builder.body = payload
        // builder.bounceable = address.isBounceable()
        builder.coins = coins
        builder.build()
    }

    constructor(json: JSONObject) : this(
        json.getString("address"),
        parseAmount(json.get("amount")),
        json.optString("stateInit"),
        json.optString("payload")
    )

    private companion object {

        private fun parseAmount(value: Any): BigInt {
            return when (value) {
                is Long -> value.toBigInteger()
                is String -> BigInt(value)
                else -> error("Unsupported type: $value")
            }
        }
    }

    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("address", addressValue)
            put("amount", amount.toString())
            stateInitValue?.let {
                put("stateInit", it)
            }
            put("payload", payloadValue)
        }
    }

}