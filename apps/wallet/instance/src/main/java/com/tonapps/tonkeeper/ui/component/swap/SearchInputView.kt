package com.tonapps.tonkeeper.ui.component.swap

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import com.tonapps.tonkeeperx.R
import uikit.extensions.dp
import uikit.extensions.focusWithKeyboard
import uikit.extensions.hideKeyboard
import uikit.extensions.useAttributes
import uikit.widget.RowLayout

class SearchInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RowLayout(context, attrs, defStyle) {

    private val input: EditText
    var doOnTextChanged: ((text: CharSequence) -> Unit)? = null

    init {
        inflate(context, R.layout.view_search_input_simple, this)
        setBackgroundResource(uikit.R.drawable.bg_content)

        input = findViewById(R.id.input)

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                cancel()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        input.doOnTextChanged { text, _, _, _ ->
            doOnTextChanged?.invoke(text ?: "")
        }

        context.useAttributes(attrs, R.styleable.SearchInputView) {
            it.getString(R.styleable.SearchInputView_android_hint)?.let { text ->
                input.hint = text
            }
        }
    }

    fun cancel() {
        input.text?.clear()
        input.hideKeyboard()
    }

    fun focus() {
        input.focusWithKeyboard()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpec = MeasureSpec.makeMeasureSpec(48.dp, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }
}