package com.tonapps.tonkeeper.ui.screen.swap

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Amount
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Continue
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Select
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity

class SwapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val sendTokenLayout: SmallTokenView
    private val sendBalance: TextView
    private val sendInput: AmountInput
    private val max: TextView

    private val receiveTokenLayout: SmallTokenView
    private val receiveInput: AmountInput
    private val receiveBalance: TextView

    private val button: Button

    private var sendModel: AssetModel? = null

    init {
        inflate(context, R.layout.view_swap_full_layout, this)

        sendTokenLayout = findViewById(R.id.send_token_view)
        sendBalance = findViewById(R.id.send_balance)
        sendInput = findViewById(R.id.send_amount_input)
        max = findViewById(R.id.max_button)

        receiveTokenLayout = findViewById(R.id.receive_token_view)
        receiveInput = findViewById(R.id.receive_amount_input)
        receiveBalance = findViewById(R.id.receive_balance)

        button = findViewById(R.id.enter_button)

        sendTokenLayout.setText(TokenEntity.TON.symbol)
        sendTokenLayout.setIcon(TokenEntity.TON.imageUri)

        receiveTokenLayout.setIconVisibility(false)
        receiveTokenLayout.setText(context.resources.getString(com.tonapps.wallet.localization.R.string.choose))

        max.setOnClickListener {
            sendModel?.let { model ->
                sendInput.setText(model.balance.toString())
            }
        }
    }

    fun setOnSendTokenClickListener(click: () -> Unit) {
        sendTokenLayout.setOnClickListener { click() }
    }

    fun setOnReceiveTokenClickListener(click: () -> Unit) {
        receiveTokenLayout.setOnClickListener { click() }
    }

    fun addSendTextChangeListener(onChange: (String) -> Unit) {
        sendInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onChange(s.toString())
            }

            override fun afterTextChanged(s: Editable?) = Unit

        })
    }

    fun addReceiveTextChangeListener(onChange: (String) -> Unit) {
        receiveInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onChange(s.toString())
            }

            override fun afterTextChanged(s: Editable?) = Unit

        })
    }

    fun setSendToken(model: AssetModel?) {
        sendModel = model
        if (model == null) {
            sendBalance.isVisible = false
        } else {
            sendBalance.isVisible = true
            sendBalance.text = getBalance(model)
        }
        sendTokenLayout.setAsset(model)
    }

    fun setReceiveToken(model: AssetModel?) {
        if (model == null) {
            receiveBalance.isVisible = false
        } else {
            receiveBalance.isVisible = true
            receiveBalance.text = getBalance(model)
        }
        receiveTokenLayout.setAsset(model)
    }

    private fun getBalance(model: AssetModel) =
        context.getString(
            com.tonapps.wallet.localization.R.string.balance_total,
            CurrencyFormatter.format(
                value = model.balance,
                decimals = model.token.decimals,
                currency = model.token.symbol
            ).toString()
        )

    fun updateBottomButton(state: BottomButtonState) {
        val (textId, backgroundId) = when (state) {
            Select -> com.tonapps.wallet.localization.R.string.choose_token to uikit.R.drawable.bg_button_secondary
            Amount -> com.tonapps.wallet.localization.R.string.enter_amount to uikit.R.drawable.bg_button_secondary
            Continue -> com.tonapps.wallet.localization.R.string.continue_action to uikit.R.drawable.bg_button_primary
        }
        button.setText(textId)
        button.setBackgroundResource(backgroundId)
    }

}