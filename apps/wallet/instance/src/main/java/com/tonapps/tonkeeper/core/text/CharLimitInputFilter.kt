package com.tonapps.tonkeeper.core.text

import android.text.InputFilter
import android.text.Spanned

class CharLimitInputFilter(
    val maxOccurrences: Int,
    val accept: (char: Char) -> Boolean
) : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        var count = 0
        if (dest != null) {
            for (index in 0 until dstart) {
                val char = dest[index]
                if (accept(char) && ++count > maxOccurrences) {
                    break
                }
            }
            if (count <= maxOccurrences) {
                for (index in dend until dest.length) {
                    val char = dest[index]
                    if (accept(char) && ++count > maxOccurrences) {
                        break
                    }
                }
            }
        }
        if (source != null && count <= maxOccurrences) {
            for (index in start until end) {
                val char = source[index]
                if (accept(char) && ++count > maxOccurrences) {
                    break
                }
            }
        }

        return if (count > maxOccurrences) {
            ""
        } else {
            null
        }
    }
}