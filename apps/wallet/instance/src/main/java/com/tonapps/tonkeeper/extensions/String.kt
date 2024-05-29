package com.tonapps.tonkeeper.extensions

import android.text.Editable
import android.widget.EditText
import com.tonapps.blockchain.Coin

fun String.substringSafe(startIndex: Int, endIndex: Int): String {
    return if (startIndex > length) {
        ""
    } else if (endIndex > length) {
        substring(startIndex)
    } else {
        substring(startIndex, endIndex)
    }
}

val String.capitalized: String
    get() {
        return if (isNotEmpty()) {
            val first = this[0].uppercase()
            if (length > 1) {
                first + substring(1)
            } else {
                first
            }
        } else {
            ""
        }
    }


val EditText.amount: Float
    get() {
        text?: return 0f
        val text = Coin.prepareValue(text.toString())
        return text.toFloatOrNull() ?: 0f
    }
