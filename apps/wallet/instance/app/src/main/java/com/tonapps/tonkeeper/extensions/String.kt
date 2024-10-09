package com.tonapps.tonkeeper.extensions

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.core.net.toUri
import com.tonapps.uikit.icon.UIKitIcon
import uikit.extensions.drawable
import uikit.span.ImageSpanCompat

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

fun String.formatCompat(vararg args: CharSequence?): Spannable {
    val result = SpannableStringBuilder()
    val regex = "%[0-9]+\\\$s".toRegex()
    val split = this.split(regex)
    for (i in split.indices) {
        result.append(split[i])
        if (i < args.size) {
            result.append(args[i])
        }
    }
    return result
}

fun String.isVersionLowerThan(other: String): Boolean {
    val thisParts = this.split(".")
    val otherParts = other.split(".")

    val length = maxOf(thisParts.size, otherParts.size)

    for (i in 0 until length) {
        val thisPart = thisParts.getOrNull(i)?.toIntOrNull() ?: 0
        val otherPart = otherParts.getOrNull(i)?.toIntOrNull() ?: 0

        if (thisPart != otherPart) {
            return thisPart < otherPart
        }
    }
    return false // Versions are equal
}

fun String.withVerificationIcon(context: Context): CharSequence {
    val drawable = context.drawable(UIKitIcon.ic_verification_16)
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

    val builder = SpannableStringBuilder()
    builder.append(this)
    builder.append(" X")
    builder.setSpan(ImageSpanCompat(drawable), builder.length - 1, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return builder
}

fun String.isPrintableAscii(): Boolean {
    return this.all { it.code in 32..126 }
}
