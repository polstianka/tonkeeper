package uikit.widget.amount

import android.text.InputFilter
import android.text.Spanned

class PercentInputFilter : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        try {
            val newValue = StringBuilder(dest).apply {
                replace(dstart, dend, source.subSequence(start, end).toString())
            }.toString()

            if (newValue == "." || newValue.isEmpty()) {
                return null
            }

            val input = newValue.toDouble()

            if (input in 0.0..100.0) {
                if (newValue.contains(".")) {
                    val decimalPart = newValue.split(".")[1]
                    if (decimalPart.length > 1) {
                        return ""
                    }
                }
                return null
            }
        } catch (_: NumberFormatException) {

        }
        return ""
    }
}
