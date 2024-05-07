package com.tonapps.tonkeeper.ui.screen.swap

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.transition.TransitionManager
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Amount
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Confirm
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Continue
import com.tonapps.tonkeeper.ui.screen.swap.SwapUiModel.BottomButtonState.Insufficient
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
) : ConstraintLayout(context, attrs, defStyle) {

    private val sendTokenLayout: SmallTokenView
    private val sendBalance: TextView
    private val sendInput: AmountInput
    private val sendTitle: TextView
    private val sendMaxButton: TextView

    private val receiveTokenLayout: SmallTokenView
    private val receiveInput: AmountInput
    private val receiveBalance: TextView
    private val receiveTitle: TextView

    private val button: Button
    private val swapButton: ImageButton
    private val receiveDetailsLayout: ColumnLayout
    private val loadingView: LoaderView
    private val cancelButton: Button

    private var sendModel: AssetModel? = null
    private var receiveModel: AssetModel? = null

    private var sendTextWatcher: SwapTextWatcher? = null
    private var receiveTextWatcher: SwapTextWatcher? = null

    private var isReversed: Boolean = false

    var doOnClick: ((BottomButtonState) -> Unit) = {}
    var doOnCancel: () -> Unit = {}

    init {
        inflate(context, R.layout.view_swap_full_layout, this)

        sendTokenLayout = findViewById(R.id.send_token_view)
        sendBalance = findViewById(R.id.send_balance)
        sendInput = findViewById(R.id.send_amount_input)
        sendTitle = findViewById(R.id.send_title)
        sendMaxButton = findViewById(R.id.send_max_button)

        receiveTokenLayout = findViewById(R.id.receive_token_view)
        receiveInput = findViewById(R.id.receive_amount_input)
        receiveBalance = findViewById(R.id.receive_balance)
        receiveTitle = findViewById(R.id.receive_title)

        swapButton = findViewById(R.id.swap_button)
        button = findViewById(R.id.enter_button)
        receiveDetailsLayout = findViewById(R.id.receive_details_layout)
        loadingView = findViewById(R.id.loading_view)
        cancelButton = findViewById(R.id.cancel_button)

        sendTokenLayout.setText(TokenEntity.TON.symbol)
        sendTokenLayout.setIcon(TokenEntity.TON.imageUri)

        receiveTokenLayout.setIconVisibility(false)
        receiveTokenLayout.setText(context.resources.getString(com.tonapps.wallet.localization.R.string.choose))

        sendMaxButton.setOnClickListener {
            sendModel?.let { model ->
                sendInput.setText(model.balance.toString())
            }
        }

        receiveInput.doAfterTextChanged {
            receiveInput.setSelection(receiveInput.text.toString().length)
        }
        sendInput.doAfterTextChanged {
            sendInput.setSelection(sendInput.text.toString().length)
        }
        cancelButton.setOnClickListener { doOnCancel() }
    }

    fun setOnSendTokenClickListener(click: (AssetModel?) -> Unit) {
        sendTokenLayout.setOnClickListener { click(receiveModel) }
    }

    fun setOnReceiveTokenClickListener(click: (AssetModel?) -> Unit) {
        receiveTokenLayout.setOnClickListener { click(sendModel) }
    }

    fun setOnSwapClickListener(click: () -> Unit) {
        swapButton.setOnClickListener {
            it.animate()
                .rotationBy(180f)
                .setDuration(300)
                .setInterpolator(LinearInterpolator())
                .withStartAction { it.isEnabled = false }
                .withEndAction { it.isEnabled = true }

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
            sendMaxButton.isVisible = false
            sendBalance.isVisible = false
        } else {
            sendMaxButton.isVisible = true
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

    fun setConfirmState(confirm: Boolean) {
        sendBalance.isVisible = !confirm && sendModel != null
        receiveBalance.isVisible = !confirm && receiveModel != null
        swapButton.isVisible = !confirm
        sendInput.isEnabled = !confirm
        receiveInput.isEnabled = !confirm
        sendMaxButton.isVisible = !confirm && sendModel != null
        sendTokenLayout.isEnabled = !confirm
        receiveTokenLayout.isEnabled = !confirm
        cancelButton.isVisible = confirm
        if (receiveDetailsLayout.childCount > 0) {
            receiveDetailsLayout.getChildAt(0).isVisible = !confirm
            receiveDetailsLayout.getChildAt(1).isVisible = !confirm
        }
        TransitionManager.beginDelayedTransition(this)
    }

    fun updateBottomButton(state: BottomButtonState) {
        val (text, backgroundId) = when (state) {
            Select -> context.getString(com.tonapps.wallet.localization.R.string.choose_token) to uikit.R.drawable.bg_button_secondary
            Amount -> context.getString(com.tonapps.wallet.localization.R.string.enter_amount) to uikit.R.drawable.bg_button_secondary
            Continue -> context.getString(com.tonapps.wallet.localization.R.string.continue_action) to uikit.R.drawable.bg_button_primary
            Confirm -> context.getString(com.tonapps.wallet.localization.R.string.confirm) to uikit.R.drawable.bg_button_primary
            Loading -> context.getString(com.tonapps.wallet.localization.R.string.continue_action) to uikit.R.drawable.bg_button_secondary
            Insufficient -> context.getString(
                com.tonapps.wallet.localization.R.string.insufficient_balance_buy,
                sendModel?.token?.symbol.orEmpty()
            ) to uikit.R.drawable.bg_button_secondary
        }
        if (state == Loading) {
            button.text = ""
            loadingView.isVisible = true
            loadingView.startAnimation()
        } else {
            loadingView.isVisible = false
            loadingView.stopAnimation()
            button.text = text
        }
        button.setOnClickListener { doOnClick(state) }
        button.setBackgroundResource(backgroundId)
    }

    fun setSendText(s: String) {
        if (isReversed) {
            receiveInput.updateText(s, receiveTextWatcher)
        } else {
            sendInput.updateText(s, sendTextWatcher)
        }
    }

    fun setReceivedText(s: String) {
        if (isReversed) {
            sendInput.updateText(s, sendTextWatcher)
        } else {
            receiveInput.updateText(s, receiveTextWatcher)
        }
    }

    fun setDetails(details: List<Details>?) {
        if (details == null) {
            withAnimation(300) {
                receiveDetailsLayout.removeAllViews()
            }
            return
        }
        val needAnimation = receiveDetailsLayout.childCount == 0
        receiveDetailsLayout.removeAllViews()
        receiveDetailsLayout.isVisible = !needAnimation
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
                    if (it.valueTint != null) {
                        setTextColor(ContextCompat.getColor(context, it.valueTint))
                    } else {
                        setTextColor(context.textPrimaryColor)
                    }
                }
                row.addView(title)
                row.addView(value)
                receiveDetailsLayout.addView(row)
            } else if (it is Details.Header) {
                val title = AppCompatTextView(context).apply {
                    layoutParams = lpText
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                    text = it.swapRate
                    setPaddingVertical(14.dp)
                    setTextAppearance(uikit.R.style.TextAppearance_Body2)
                    if (it.tint != null) {
                        setTextColor(ContextCompat.getColor(context, it.tint))
                    } else {
                        setTextColor(context.textPrimaryColor)
                    }
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
                receiveDetailsLayout.addView(divider1)
                receiveDetailsLayout.addView(row)
                receiveDetailsLayout.addView(divider2)
            }
        }
        if (needAnimation) {
            withAnimation(300) {
                receiveDetailsLayout.isVisible = true
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