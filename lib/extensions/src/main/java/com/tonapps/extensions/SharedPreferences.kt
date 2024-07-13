package com.tonapps.extensions

import android.content.SharedPreferences
import androidx.core.content.edit

inline fun <reified T : Enum<T>> SharedPreferences.getEnum(key: String, default: T): T {
    val value = this.getInt(key, -1)
    return if (value >= 0) {
        enumValues<T>()[value]
    } else {
        default
    }
}

fun <T : Enum<T>> SharedPreferences.Editor.putEnum(key: String, value: T?) = apply {
    this.putInt(key, value?.ordinal ?: -1)
}

fun SharedPreferences.string(key: String): String? {
    return this.getString(key, null)
}

fun SharedPreferences.string(key: String, value: String?) {
    edit {
        if (value.isNullOrBlank()) {
            remove(key)
        } else {
            putString(key, value)
        }
    }
}

fun SharedPreferences.float(key: String): Float {
    return this.getFloat(key, 0f)
}

fun SharedPreferences.float(key: String, value: Float?) {
    edit {
        if (value == null) {
            remove(key)
        } else {
            putFloat(key, value)
        }
    }
}

fun SharedPreferences.bool(key: String): Boolean {
    return this.getBoolean(key, false)
}

fun SharedPreferences.bool(key: String, value: Boolean?) {
    edit {
        if (value == null) {
            remove(key)
        } else {
            putBoolean(key, value)
        }
    }
}