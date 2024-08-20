package com.tonapps.wallet.data.core.entity

import android.os.Parcelable
import android.util.Log
import com.tonapps.blockchain.ton.TonNetwork
import kotlinx.datetime.Clock
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONObject
import org.ton.block.AddrStd
import kotlin.time.Duration.Companion.seconds

@Parcelize
data class SignRequestEntity(
    val fromValue: String?,
    val validUntil: Long,
    val messages: List<RawMessageEntity>,
    val network: TonNetwork
): Parcelable {

    val from: AddrStd?
        get() = fromValue?.let { AddrStd.parse(it) }

    val transfers = messages.map { it.getWalletTransfer() }

    constructor(json: JSONObject) : this(
        fromValue = parseFrom(json),
        validUntil = parseValidUnit(json),
        messages = parseMessages(json.getJSONArray("messages")),
        network = parseNetwork(json.opt("network"))
    )

    private companion object {

        private fun parseValidUnit(json: JSONObject): Long {
            val value = json.optLong("valid_until", json.optLong("validUntil", 0))
            if (value > 1000000000000) {
                return value / 1000
            }
            if (value > 1000000000) {
                return value
            }
            return 0
        }

        private fun parseMessages(array: JSONArray): List<RawMessageEntity> {
            val messages = mutableListOf<RawMessageEntity>()
            for (i in 0 until array.length()) {
                val json = array.getJSONObject(i)
                messages.add(RawMessageEntity(json))
            }
            return messages
        }

        private fun parseFrom(json: JSONObject): String? {
            return if (json.has("from")) {
                json.getString("from")
            } else if (json.has("source")) {
                json.getString("source")
            } else {
                null
            }
        }

        private fun parseNetwork(value: Any?): TonNetwork {
            if (value == null) {
                return TonNetwork.MAINNET
            }
            if (value is String) {
                return parseNetwork(value.toIntOrNull())
            }
            if (value !is Int) {
                return parseNetwork(value.toString())
            }
            return if (value == TonNetwork.TESTNET.value) {
                TonNetwork.TESTNET
            } else {
                TonNetwork.MAINNET
            }
        }
    }
}