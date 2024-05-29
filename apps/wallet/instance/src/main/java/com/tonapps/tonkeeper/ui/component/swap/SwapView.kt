package com.tonapps.tonkeeper.ui.component.swap

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import com.tonapps.tonkeeper.fragment.swap.model.Simulate
import com.tonapps.tonkeeper.fragment.swap.model.SwapState
import com.tonapps.tonkeeperx.R


class SwapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {

    val sendView: SendSwapView
    val receiveView: ReceiveSwapView
    val btnSwapView: SwapButtonView

    var onSwapClick: (() -> Unit)? = null

    init {
        inflate(context, R.layout.view_swap, this)

        sendView = findViewById(R.id.send)
        receiveView = findViewById(R.id.receive)
        btnSwapView = findViewById(R.id.btn_swap)

        btnSwapView.setOnClickListener {
            onSwapClick?.invoke()
        }
    }

    fun setConfirmMode(sendToken: SwapState.TokenState, receiveToken: SwapState.TokenState, simulate: Simulate) {
        getContentViews().forEach {
            it.alpha = 1f
            it.visibility = VISIBLE
        }
        sendView.setConfirmMode(sendToken)
        receiveView.setConfirmMode(receiveToken, simulate)
        btnSwapView.isGone = true
    }

    fun getContentViews(): List<View> {
        val list = mutableListOf<View>()
        list.addAll(sendView.contentViews())
        list.addAll(receiveView.contentViews())
        list.add(btnSwapView)
        return list
    }
}