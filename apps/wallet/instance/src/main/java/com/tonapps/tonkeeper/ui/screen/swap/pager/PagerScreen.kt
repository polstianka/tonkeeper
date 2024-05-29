package com.tonapps.tonkeeper.ui.screen.swap.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.tonapps.tonkeeper.fragment.send.view.SendFrameLayout
import com.tonapps.tonkeeper.ui.screen.swap.SwapScreenFeature
import uikit.mvi.UiEffect
import uikit.mvi.UiFeature
import uikit.mvi.UiScreen
import uikit.mvi.UiState

abstract class PagerScreen<S : UiState, E : UiEffect, F : UiFeature<S, E>>(
    layoutRes: Int
) : UiScreen<S, E, F>(layoutRes) {

    val swapFeature: SwapScreenFeature by viewModels({ requireParentFragment() })

    var visible: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                onVisibleChange(value)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        val rootView = SendFrameLayout(view.context)
        rootView.addView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        rootView.tag = this
        return rootView
    }

    open fun onVisibleChange(visible: Boolean) {

    }

    override fun onResume() {
        super.onResume()
        visible = true
    }

    fun isVisibleForUser() = visible && isVisible
}