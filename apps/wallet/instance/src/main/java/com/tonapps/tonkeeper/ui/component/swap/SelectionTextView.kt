package com.tonapps.tonkeeper.ui.component.swap

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import uikit.drawable.InputDrawable

class SelectionTextView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : AppCompatTextView(context, attrs) {
        private val inputDrawable = InputDrawable(context)

        init {
            background = inputDrawable
        }

        fun setActive(active: Boolean) {
            inputDrawable.active = active
        }
    }
