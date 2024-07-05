package com.tonapps.tonkeeper.dialog

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.tonapps.extensions.ifPunycodeToUnicode
import com.tonapps.wallet.localization.Localization
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.api.shortAddress
import com.tonapps.tonkeeper.api.shortHash
import com.tonapps.tonkeeper.core.history.ActionType
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.tonkeeper.core.history.nameRes
import com.tonapps.tonkeeper.extensions.copyWithToast
import com.tonapps.tonkeeper.ui.screen.root.RootActivity
import com.tonapps.tonkeeper.view.TransactionDetailView
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import kotlinx.coroutines.CoroutineScope
import uikit.base.BaseSheetDialog
import uikit.extensions.activity
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView

class TransactionDialog(
    context: Context,
    private val scope: CoroutineScope
): BaseSheetDialog(context) {

    companion object {

        fun open(context: Context, action: HistoryItem.Event) {
            val rootActivity = context.activity as? RootActivity ?: return
            rootActivity.transactionDialog.show(action)
        }
    }

    private val iconView: FrescoView
    private val amountView: AppCompatTextView
    private val currencyView: AppCompatTextView
    private val dateView: AppCompatTextView
    private val dataView: LinearLayoutCompat
    private val feeView: TransactionDetailView
    private val commentView: TransactionDetailView
    private val accountNameView: TransactionDetailView
    private val accountAddressView: TransactionDetailView
    private val txView: TransactionDetailView
    private val explorerButton: View

    init {
        setContentView(R.layout.dialog_transaction)

        iconView = findViewById(R.id.icon)!!
        amountView = findViewById(R.id.amount)!!
        currencyView = findViewById(R.id.currency)!!
        dateView = findViewById(R.id.date)!!

        dataView = findViewById(R.id.data)!!

        feeView = findViewById(R.id.fee)!!
        feeView.title = getString(Localization.fee)

        commentView = findViewById(R.id.comment)!!
        commentView.title = getString(Localization.comment)

        accountNameView = findViewById(R.id.account_name)!!
        accountAddressView = findViewById(R.id.account_address)!!

        txView = findViewById(R.id.tx)!!
        txView.title = getString(Localization.transaction)

        explorerButton = findViewById(R.id.open_explorer)!!
    }

    fun show(action: HistoryItem.Event) {
        super.show()
        if (action.hiddenBalance) {
            amountView.text = HIDDEN_BALANCE
            feeView.setData(HIDDEN_BALANCE, HIDDEN_BALANCE)
        } else {
            amountView.text = action.value
            feeView.setData(action.fee!!, action.feeInCurrency!!)
        }

        applyIcon(action.coinIconUrl)
        applyComment(action.comment)
        applyAccount(action.isOut, action.address, action.addressName?.ifPunycodeToUnicode())
        applyCurrency(action.currency, action.hiddenBalance)
        applyDate(action.action, action.date)

        txView.setData(action.txId.shortHash, "")
        txView.setOnClickListener {
            context.copyWithToast(action.txId)
        }


        explorerButton.setOnClickListener {
            dismiss()
            navigation?.openURL("https://tonviewer.com/transaction/${action.txId}", true)
        }


        updateDataView()
    }

    private fun applyDate(actionType: ActionType, date: String) {
        val prefix = getString(actionType.nameRes)
        dateView.text = "$prefix $date"
    }

    private fun applyCurrency(currency: CharSequence?, hiddenBalance: Boolean) {
        if (currency.isNullOrBlank()) {
            currencyView.visibility = View.GONE
        } else {
            currencyView.visibility = View.VISIBLE
            if (hiddenBalance) {
                currencyView.text = HIDDEN_BALANCE
            } else {
                currencyView.text = currency
            }
        }
    }

    private fun applyIcon(icon: String?) {
        if (icon.isNullOrBlank()) {
            iconView.visibility = View.GONE
        } else {
            iconView.visibility = View.VISIBLE
            iconView.setImageURI(icon)
        }
    }

    private fun applyComment(comment: String?) {
        if (comment.isNullOrBlank()) {
            commentView.visibility = View.GONE
        } else {
            commentView.visibility = View.VISIBLE
            commentView.setData(comment, "")
            commentView.setOnClickListener {
                context.copyWithToast(comment)
            }
        }
    }

    private fun applyAccount(out: Boolean, address: String?, name: String?) {
        accountNameView.visibility = View.GONE
        accountAddressView.visibility = View.GONE

        val hasName = !name.isNullOrBlank()
        if (hasName) {
            applyAccountWithName(out, address!!, name!!)
        } else if (address != null) {
            accountAddressView.visibility = View.VISIBLE
            accountAddressView.title = getAccountTitle(out)
            accountAddressView.setData(address.shortAddress, "")
            accountAddressView.setOnClickListener {
                context.copyWithToast(address)
            }
        } else {
            accountAddressView.visibility = View.GONE
        }
    }

    private fun applyAccountWithName(out: Boolean, address: String, name: String) {
        accountNameView.visibility = View.VISIBLE
        accountNameView.title = getAccountTitle(out)
        accountNameView.setData(name, "")
        accountNameView.setOnClickListener {
            context.copyWithToast(name)
        }

        accountAddressView.visibility = View.VISIBLE
        accountAddressView.title = getString(if (out) {
            Localization.recipient_address
        } else Localization.sender_address)
        accountAddressView.setData(address.shortAddress, "")
        accountAddressView.setOnClickListener {
            context.copyWithToast(address)
        }
    }

    private fun getAccountTitle(out: Boolean): String {
        return getString(if (out) {
            Localization.recipient
        } else Localization.sender)
    }

    private fun updateDataView() {
        val visibleViews = mutableListOf<TransactionDetailView>()
        for (i in 0 until dataView.childCount) {
            val child = dataView.getChildAt(i)
            if (child is TransactionDetailView && child.visibility == View.VISIBLE) {
                visibleViews.add(child)
            }
        }

        for (i in 0 until visibleViews.size) {
            visibleViews[i].position = com.tonapps.uikit.list.ListCell.getPosition(visibleViews.size, i)
        }

        fixPeekHeight()
    }
}