package com.tonapps.tonkeeper.fragment.swap.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo
import com.tonapps.tonkeeperx.R as ProjectR

class SendReceiveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {

    private val sendView: SendView
    private val receiveView: SendView
    private val swapView: AppCompatImageView

    var sendToken: TokenInfo? = null
        set(value) {
            field = value
            sendView.token = value
        }

    var receiveToken: TokenInfo? = null
        set(value) {
            field = value
            receiveView.token = value
        }

    var onSwapDetailsItemClick: (SwapDetailsItem) -> Unit = { }
        set(value) {
            field = value
            receiveView.onSwapDetailsItemClick = value
        }

    var details: List<SwapDetailsItem> = emptyList()
        set(value) {
            field = value
            receiveView.details = value
        }

    var expandedDetails: Boolean = true
        set(value) {
            field = value
            receiveView.expandedDetails = expandedDetails
        }

    var swapButtonClick: () -> Unit = { }

    var enterTextEnabled: Boolean = true
        set(value) {
            field = value
            sendView.enterTextEnabled = value
            receiveView.enterTextEnabled = value
        }

    var swapButtonVisible: Boolean = true
        set(value) {
            field = value
            swapView.visibility = if (value) View.VISIBLE else View.GONE
        }

    var balanceVisible: Boolean = true
        set(value) {
            field = value
            sendView.balanceVisible = value
            receiveView.balanceVisible = value
        }

    var balanceActionAvailable: Boolean = true
        set(value) {
            field = value
            sendView.balanceActionAvailable = value
            receiveView.balanceActionAvailable = value
        }

    init {
        inflate(context, ProjectR.layout.view_swap_send_receive_view, this)

        sendView = findViewById(ProjectR.id.swap_send)
        receiveView = findViewById(ProjectR.id.swap_receive)
        swapView = findViewById(ProjectR.id.swap)
        swapView.setOnClickListener {
            swapButtonClick.invoke()
        }
    }

    fun setSendOnBalanceActionClick(onClick: () -> Unit) {
        sendView.setOnBalanceActionClick(onClick)
    }

    fun setSendValue(text: CharSequence) {
        sendView.setValue(text)
    }

    fun setReceiveValue(text: CharSequence) {
        receiveView.setValue(text)
    }

    fun setOnSendTokenClick(onClick: (View) -> Unit) {
        sendView.setOnTokenClickListener(onClick)
    }

    fun setOnReceiveTokenClick(onClick: (View) -> Unit) {
        receiveView.setOnTokenClickListener(onClick)
    }

    fun setSendOnAmountChangeListener(textChanged: (Float) -> Unit) {
        sendView.setOnAmountChangeListener(textChanged)
    }

    fun setReceiveOnAmountChangeListener(textChanged: (Float) -> Unit) {
        receiveView.setOnAmountChangeListener(textChanged)
    }

    fun takeSendFocus() {
        sendView.takeFocus()
    }

    fun hideKeyboard() {
        sendView.hideKeyboard()
        receiveView.hideKeyboard()
    }
}
