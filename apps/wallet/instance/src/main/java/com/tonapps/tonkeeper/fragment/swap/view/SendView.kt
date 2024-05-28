package com.tonapps.tonkeeper.fragment.swap.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsAdapter
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo
import uikit.R
import uikit.extensions.collapse
import uikit.extensions.dp
import uikit.extensions.expand
import uikit.extensions.focusWithKeyboard
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.hideKeyboard
import uikit.extensions.useAttributes
import uikit.widget.ColumnLayout
import com.tonapps.tonkeeperx.R as ProjectR

class SendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ColumnLayout(context, attrs, defStyle) {

    private val titleView: AppCompatTextView
    private val preBalance: AppCompatTextView
    private val balanceView: AppCompatTextView
    private val balanceActionView: AppCompatTextView
    private val tokenView: TokenChipView
    private val valueView: AppCompatEditText
    private val listView: RecyclerView

    private val adapter: SwapDetailsAdapter by lazy { SwapDetailsAdapter { onSwapDetailsItemClick.invoke(it) } }

    var token: TokenInfo? = null
        set(value) {
            field = value
            tokenView.token = value
            if (value == null) {
                preBalance.visibility = View.GONE
                balanceView.visibility = View.GONE
            } else {
                preBalance.visibility = View.VISIBLE
                balanceView.visibility = View.VISIBLE
                balanceView.text = value.balance
            }
        }

    var balanceVisible: Boolean = true
        set(value) {
            field = value
            preBalance.visibility = if (value) View.VISIBLE else View.GONE
        }

    var balanceActionAvailable: Boolean = true
        set(value) {
            field = value
            balanceActionView.visibility = if (value) View.VISIBLE else View.GONE
        }

    var enterTextEnabled: Boolean = true
        set(value) {
            field = value
            valueView.isEnabled = value
        }

    var onSwapDetailsItemClick: (SwapDetailsItem) -> Unit = { }

    var details: List<SwapDetailsItem> = emptyList()
        set(value) {
            field = value
            adapter.submitList(value) {
                listView.requestLayout()
            }
        }

    var expandedDetails: Boolean = true
        set(value) {
            if (value) {
                if (!field) {
                    expand(listView, 48.dp)
                }
            } else {
                if (field) {
                    collapse(listView, 48.dp)
                }
            }
            field = value
        }

    init {
        setPadding(context.getDimensionPixelSize(R.dimen.offsetMedium))
        setBackgroundResource(R.drawable.bg_content)
        inflate(context, ProjectR.layout.view_swap_send_view, this)

        titleView = findViewById(ProjectR.id.action_cell_title)
        preBalance = findViewById(ProjectR.id.pre_balance)
        balanceView = findViewById(ProjectR.id.action_cell_balance)
        balanceActionView = findViewById(ProjectR.id.action_cell_balance_action)
        tokenView = findViewById(ProjectR.id.token)
        valueView = findViewById(ProjectR.id.value)

        listView = findViewById(ProjectR.id.list)
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(context = context, canScrollVertically = false)
        listView.adapter = adapter

        context.useAttributes(attrs, R.styleable.SwapSendView) {
            titleView.text = it.getString(R.styleable.SwapSendView_android_title)

            balanceActionAvailable = it.getBoolean(R.styleable.SwapSendView_maxAvailable, false)
            balanceActionView.visibility = if (balanceActionAvailable) View.VISIBLE else View.GONE

            val singleLine = it.getBoolean(R.styleable.SwapSendView_android_singleLine, false)
            if (singleLine) {
                titleView.setSingleLine()
            }
        }
    }

    fun setValue(text: CharSequence) {
        valueView.setText(text)
        valueView.setSelection(text.length)
    }

    fun setOnTokenClickListener(onClick: (View) -> Unit) {
        tokenView.setOnClickListener(onClick)
    }

    fun setOnBalanceActionClick(onClick: () -> Unit) {
        balanceActionView.setOnClickListener { onClick.invoke() }
    }

    fun setOnAmountChangeListener(textChanged: (Float) -> Unit) {
        valueView.doOnTextChanged { _, _, _, _ ->
            textChanged.invoke(getValue())
        }
    }

    fun takeFocus() {
        valueView.focusWithKeyboard()
    }

    fun hideKeyboard() {
        valueView.hideKeyboard()
    }

    private fun getValue(): Float {
        val text = Coin.prepareValue(valueView.text.toString())
        return text.toFloatOrNull() ?: 0f
    }
}
