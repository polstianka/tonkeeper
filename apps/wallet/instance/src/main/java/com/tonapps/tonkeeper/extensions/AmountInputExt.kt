package com.tonapps.tonkeeper.extensions

import androidx.core.widget.doOnTextChanged
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import java.math.BigDecimal

fun convertAmountInputText(text: CharSequence?): BigDecimal? {
    return text?.toString()?.let { it.toBigDecimalOrNull() ?: BigDecimal.ZERO }
}

inline fun AmountInput.doOnAmountChange(
    crossinline converter: (CharSequence?) -> BigDecimal? = ::convertAmountInputText,
    crossinline action: (BigDecimal) -> Unit
) {
    doOnTextChanged { text, _, _, _ ->
        converter(text)?.let { action(it) }
    }
}