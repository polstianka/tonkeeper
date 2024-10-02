package com.tonapps.extensions

import android.content.Context
import androidx.annotation.StringRes

val Throwable.bestMessage: String
    get() = localizedMessage ?: message ?: toString()


fun Throwable.getUserMessage(context: Context): String? {
    return when (this) {
        is ErrorForUserException -> getMessage(context)
        else -> null
    }
}

open class ErrorForUserException(
    @StringRes val stringRes: Int = 0,
    val text: String? = null,
    override val cause: Throwable? = null,
) : Exception(cause) {

    companion object {

        fun of(text: String): ErrorForUserException {
            return ErrorForUserException(text = text)
        }
    }

    fun getMessage(context: Context): String {
        if (stringRes == 0 && text.isNullOrBlank()) {
            return cause?.bestMessage ?: "unknown error"
        }
        return text ?: context.getString(stringRes)
    }

}