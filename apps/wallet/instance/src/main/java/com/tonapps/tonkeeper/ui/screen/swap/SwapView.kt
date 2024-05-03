package com.tonapps.tonkeeper.ui.screen.swap

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
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
    private val swapButton: ImageButton

    private var sendModel: AssetModel? = null
    private var receiveModel: AssetModel? = null

    private var sendTextWatcher: SwapTextWatcher? = null
    private var receiveTextWatcher: SwapTextWatcher? = null

    init {
        inflate(context, R.layout.view_swap_full_layout, this)

        sendTokenLayout = findViewById(R.id.send_token_view)
        sendBalance = findViewById(R.id.send_balance)
        sendInput = findViewById(R.id.send_amount_input)
        max = findViewById(R.id.max_button)

        receiveTokenLayout = findViewById(R.id.receive_token_view)
        receiveInput = findViewById(R.id.receive_amount_input)
        receiveBalance = findViewById(R.id.receive_balance)

        swapButton = findViewById(R.id.swap_button)
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

    fun setOnSendTokenClickListener(click: (AssetModel?) -> Unit) {
        sendTokenLayout.setOnClickListener { click(receiveModel) }
    }

    fun setOnReceiveTokenClickListener(click: (AssetModel?) -> Unit) {
        receiveTokenLayout.setOnClickListener { click(sendModel) }
    }

    fun setOnSwapClickListener(click: () -> Unit) {
        swapButton.setOnClickListener {
            val tempReceiveText = receiveInput.text
            receiveInput.text = sendInput.text
            sendInput.text = tempReceiveText
            click()
        }
    }

    fun addSendTextChangeListener(onChange: (String) -> Unit) {
        sendTextWatcher = SwapTextWatcher(onChange)
        sendInput.addTextChangedListener(sendTextWatcher)
    }

    fun addReceiveTextChangeListener(onChange: (String) -> Unit) {
        receiveTextWatcher = SwapTextWatcher(onChange)
        receiveInput.addTextChangedListener(receiveTextWatcher)
    }

    fun setSendToken(model: AssetModel?) {
        sendModel = model
        if (model == null) {
            max.isVisible = false
            sendBalance.isVisible = false
        } else {
            max.isVisible = true
            sendBalance.isVisible = true
            sendBalance.text = getBalance(model)
        }
        sendTokenLayout.setAsset(model)
    }

    fun setReceiveToken(model: AssetModel?) {
        receiveModel = model
        if (model == null) {
            receiveBalance.isVisible = false
        } else {
            receiveBalance.isVisible = true
            receiveBalance.text = getBalance(model)
        }
        receiveTokenLayout.setAsset(model)
    }

    fun updateBottomButton(state: BottomButtonState) {
        val (textId, backgroundId) = when (state) {
            Select -> com.tonapps.wallet.localization.R.string.choose_token to uikit.R.drawable.bg_button_secondary
            Amount -> com.tonapps.wallet.localization.R.string.enter_amount to uikit.R.drawable.bg_button_secondary
            Continue -> com.tonapps.wallet.localization.R.string.continue_action to uikit.R.drawable.bg_button_primary
        }
        button.setText(textId)
        button.setBackgroundResource(backgroundId)
    }

    fun setSendText(s: String) {
        sendInput.updateText(s, sendTextWatcher)
    }

    fun setReceivedText(s: String) {
        receiveInput.updateText(s, receiveTextWatcher)
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

    private fun AmountInput.updateText(text: String, watcher: TextWatcher?) {
        removeTextChangedListener(watcher)
        setText(text)
        addTextChangedListener(watcher)
    }

    private class SwapTextWatcher(private val onChange: (String) -> Unit) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            onChange(s.toString())
        }

    }

}