package com.tonapps.tonkeeper.fragment.swap.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.tonapps.tonkeeper.ui.adapter.view.SuggestedTokenView
import com.tonapps.tonkeeperx.R
import uikit.extensions.useAttributes

class SwapTokenInputLayoutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {
    val titleView: AppCompatTextView
    val balanceView: AppCompatTextView
    val maxView: AppCompatTextView
    val chooseTokenView: SuggestedTokenView
    val amountInput: AmountInput

    init {
        inflate(context, R.layout.view_swap_token_input_layout, this)
        orientation = VERTICAL

        titleView = findViewById(R.id.title)
        balanceView = findViewById(R.id.balance)
        maxView = findViewById(R.id.max)
        chooseTokenView = findViewById(R.id.token)
        amountInput = findViewById(R.id.input)

        context.useAttributes(attrs, R.styleable.SwapTokenInputLayoutView) {
            titleView.text = it.getString(R.styleable.SwapTokenInputLayoutView_android_title)
        }
    }
}