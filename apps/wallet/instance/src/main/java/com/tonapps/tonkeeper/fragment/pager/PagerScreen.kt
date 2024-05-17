package com.tonapps.tonkeeper.fragment.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.component.KeyboardInsetFrameLayout
import uikit.mvi.UiEffect
import uikit.mvi.UiFeature
import uikit.mvi.UiScreen
import uikit.mvi.UiState

abstract class PagerScreen<S: UiState, E: UiEffect, F: UiFeature<S, E>>(
    layoutRes: Int
): UiScreen<S, E, F>(layoutRes) {

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
        val rootView = KeyboardInsetFrameLayout(view.context)
        rootView.addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
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