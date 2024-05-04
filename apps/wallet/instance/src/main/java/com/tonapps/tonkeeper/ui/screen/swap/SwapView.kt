package com.tonapps.tonkeeper.ui.screen.swap

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Amount
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Continue
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Loading
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Select
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.Details
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.extensions.dp
import uikit.extensions.setPaddingVertical
import uikit.extensions.withAnimation
import uikit.widget.ColumnLayout
import uikit.widget.DividerView
import uikit.widget.LoaderView
import uikit.widget.RowLayout

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
    private val detailsLayout: ColumnLayout
    private val loadingView: LoaderView

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
        detailsLayout = findViewById(R.id.details_layout)
        loadingView = findViewById(R.id.loading_view)

        sendTokenLayout.setText(TokenEntity.TON.symbol)
        sendTokenLayout.setIcon(TokenEntity.TON.imageUri)

        receiveTokenLayout.setIconVisibility(false)
        receiveTokenLayout.setText(context.resources.getString(com.tonapps.wallet.localization.R.string.choose))

        max.setOnClickListener {
            sendModel?.let { model ->
                sendInput.setText(model.balance.toString())
            }
        }

        receiveInput.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                receiveInput.setSelection(receiveInput.text.toString().length)
            }
        }
        receiveInput.doAfterTextChanged {
            receiveInput.setSelection(receiveInput.text.toString().length)
        }
        sendInput.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                sendInput.setSelection(sendInput.text.toString().length)
            }
        }
        sendInput.doAfterTextChanged {
            sendInput.setSelection(sendInput.text.toString().length)
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
            val tempReceiveText = receiveInput.text.toString()
            receiveInput.updateText(sendInput.text.toString(), receiveTextWatcher)
            sendInput.updateText(tempReceiveText, sendTextWatcher)
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
            Loading -> com.tonapps.wallet.localization.R.string.continue_action to uikit.R.drawable.bg_button_secondary
        }
        if (state == Loading) {
            button.text = ""
            loadingView.isVisible = true
            loadingView.startAnimation()
        } else {
            loadingView.isVisible = false
            loadingView.stopAnimation()
            button.setText(textId)
        }
        button.setBackgroundResource(backgroundId)
    }

    fun setSendText(s: String) {
        sendInput.updateText(s, sendTextWatcher)
    }

    fun setReceivedText(s: String) {
        receiveInput.updateText(s, receiveTextWatcher)
    }

    fun setDetails(details: List<Details>?) {
        if (details == null) {
            withAnimation(300) {
                detailsLayout.removeAllViews()
            }
            return
        }
        val needAnimation = detailsLayout.childCount == 0
        detailsLayout.removeAllViews()
        detailsLayout.isVisible = !needAnimation
        val lpText = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            1.0f
        )
        details.forEach {
            val row = RowLayout(context).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                )
            }
            if (it is Details.DetailUiModel) {
                val title = AppCompatTextView(context).apply {
                    layoutParams = lpText
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                    text = context.getString(it.title)
                    setPaddingVertical(8.dp)
                    setTextAppearance(uikit.R.style.TextAppearance_Body2)
                    setTextColor(context.textSecondaryColor)
                }
                val value = AppCompatTextView(context).apply {
                    layoutParams = lpText
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                    text = it.value
                    setPaddingVertical(8.dp)
                    setTextAppearance(uikit.R.style.TextAppearance_Body2)
                    setTextColor(context.textPrimaryColor)
                }
                row.addView(title)
                row.addView(value)
                detailsLayout.addView(row)
            } else if (it is Details.Header) {
                val title = AppCompatTextView(context).apply {
                    layoutParams = lpText
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                    text = it.swapRate
                    setPaddingVertical(14.dp)
                    setTextAppearance(uikit.R.style.TextAppearance_Body2)
                    setTextColor(context.textSecondaryColor)
                }
                val loading = LoaderView(context).apply {
                    layoutParams = LinearLayoutCompat.LayoutParams(16.dp, 16.dp).apply {
                        gravity = Gravity.CENTER_VERTICAL or Gravity.END
                    }
                    isVisible = it.loading
                    startAnimation()
                }
                row.addView(title)
                row.addView(loading)
                val divider1 = DividerView(context)
                val divider2 = DividerView(context)
                detailsLayout.addView(divider1)
                detailsLayout.addView(row)
                detailsLayout.addView(divider2)
            }
        }
        if (needAnimation) {
            withAnimation(300) {
                detailsLayout.isVisible = true
            }
        }
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