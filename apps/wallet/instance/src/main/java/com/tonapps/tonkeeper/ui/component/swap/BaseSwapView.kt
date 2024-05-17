package com.tonapps.tonkeeper.ui.component.swap

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.tonapps.tonkeeper.fragment.swap.model.SwapState
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization

abstract class BaseSwapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {

    private val label: TextView
    private val balance: TextView
    private val token: TokenButtonView

    var onTokenClick: (() -> Unit)? = null

    init {
        initView()
        label = findViewById(R.id.label)
        balance = findViewById(R.id.balance)
        token = findViewById(R.id.token)

        token.setOnClickListener {
            onTokenClick?.invoke()
        }
    }

    private fun initView() {
        inflate(context, layoutId(), this)
        setBackgroundResource(uikit.R.drawable.bg_content)
    }

    open fun setTokenState(tokenState: SwapState.TokenState?) {
        if (tokenState != null) {
            balance.text = context.getString(Localization.balance, tokenState.balance)
            token.setToken(tokenState.imageUri, tokenState.symbol)
        } else {
            balance.text = ""
            token.setButton(context.getString(Localization.choose))
        }
    }

    abstract fun layoutId(): Int

    open fun contentViews(): List<View> {
        return listOf(label, balance, token)
    }
}