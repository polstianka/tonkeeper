package com.tonapps.tonkeeper.core

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

sealed class TextWrapper {
    class StringResource(@StringRes val id: Int, vararg val args: Any) : TextWrapper()
    class PlainString(val string: String): TextWrapper()
}

fun Fragment.toString(wrapper: TextWrapper): String {
    return requireContext().toString(wrapper)
}

fun Context.toString(wrapper: TextWrapper): String {
    return when (wrapper) {
        is TextWrapper.StringResource -> getString(wrapper.id, *wrapper.args)
        is TextWrapper.PlainString -> wrapper.string
    }
}