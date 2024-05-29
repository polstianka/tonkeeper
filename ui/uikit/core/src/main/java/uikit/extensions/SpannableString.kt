package uikit.extensions

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import uikit.spannable.SpanTag


fun SpannableString.setColor(color: Int, start: Int, end: Int) {
    val what = ForegroundColorSpan(color)
    setSpan(what, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun CharSequence.addTag(span: SpanTag) : CharSequence {
    val spanText = SpannableString("${this}t")
    spanText.setSpan(span, this.length, this.length + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

    return spanText
}