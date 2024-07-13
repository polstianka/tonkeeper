package uikit.extensions

import android.text.Spanned
import androidx.core.text.HtmlCompat

fun String.parseWords(): List<String> {
    val words = split(",", "\n", " ").map {
        it.trim()
    }.filter {
        it.isNotEmpty()
    }
    return words
}

fun String.isWords(): Boolean {
    return contains(",") || contains("\n") || contains(" ")
}

fun String.html(): Spanned {
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT)
}