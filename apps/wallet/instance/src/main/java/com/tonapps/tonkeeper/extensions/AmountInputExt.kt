package com.tonapps.tonkeeper.extensions

import androidx.core.widget.doOnTextChanged
import com.tonapps.tonkeeper.fragment.send.view.AmountInput

fun convertAmountInputText(text: CharSequence?): Float? {
    return text?.toString()?.let { it.toFloatOrNull() ?: 0f }
}

inline fun AmountInput.doOnAmountChange(
    crossinline converter: (CharSequence?) -> Float? = ::convertAmountInputText,
    crossinline action: (Float) -> Unit
) {
    doOnTextChanged { text, _, _, _ ->
        converter(text)?.let { action(it) }
    }
}