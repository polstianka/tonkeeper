package com.tonapps.tonkeeper.ui.screen.action

import android.os.Bundle
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import uikit.base.BaseArgs

data class ActionArgs(
    val accountId: String,
    val walletId: String,
    val request: SignRequestEntity,
    val historyItems: List<HistoryItem>,
    val feeFormat: CharSequence,
    val feeFiatFormat: CharSequence,
    val resultKey: String,
    val isBattery: Boolean,
): BaseArgs() {

    private companion object {
        private const val ARG_ACCOUNT_ID = "account_id"
        private const val ARG_WALLET_ID = "wallet_id"
        private const val ARG_HISTORY_ITEMS = "history_items"
        private const val ARG_FEE_FORMAT = "fee_format"
        private const val ARG_FEE_FIAT_FORMAT = "fee_fiat_format"
        private const val ARG_RESULT_KEY = "result_key"
        private const val ARG_REQUEST = "request"
        private const val ARG_IS_BATTERY = "is_battery"
    }

    constructor(bundle: Bundle) : this(
        accountId = bundle.getString(ARG_ACCOUNT_ID)!!,
        walletId = bundle.getString(ARG_WALLET_ID)!!,
        request = bundle.getParcelable(ARG_REQUEST)!!,
        historyItems = bundle.getParcelableArrayList(ARG_HISTORY_ITEMS)!!,
        feeFormat = bundle.getCharSequence(ARG_FEE_FORMAT)!!,
        feeFiatFormat = bundle.getCharSequence(ARG_FEE_FIAT_FORMAT)!!,
        resultKey = bundle.getString(ARG_RESULT_KEY)!!,
        isBattery = bundle.getBoolean(ARG_IS_BATTERY)
    )

    override fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString(ARG_ACCOUNT_ID, accountId)
        bundle.putString(ARG_WALLET_ID, walletId)
        bundle.putParcelable(ARG_REQUEST, request)
        bundle.putParcelableArrayList(ARG_HISTORY_ITEMS, ArrayList(historyItems))
        bundle.putCharSequence(ARG_FEE_FORMAT, feeFormat)
        bundle.putCharSequence(ARG_FEE_FIAT_FORMAT, feeFiatFormat)
        bundle.putString(ARG_RESULT_KEY, resultKey)
        bundle.putBoolean(ARG_IS_BATTERY, isBattery)
        return bundle
    }
}