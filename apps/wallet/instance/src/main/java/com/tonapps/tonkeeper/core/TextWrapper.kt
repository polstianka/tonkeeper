package com.tonapps.tonkeeper.core

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

sealed class TextWrapper {
    class StringResource(@StringRes val id: Int, vararg val args: Any) : TextWrapper()
}

fun Fragment.toString(wrapper: TextWrapper): String {
    return when (wrapper) {
        is TextWrapper.StringResource -> getString(wrapper.id, *wrapper.args)
    }
}