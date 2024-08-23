package com.tonapps.extensions

import android.content.SharedPreferences
import androidx.core.content.edit

fun SharedPreferences.getByteArray(key: String): ByteArray? {
    val bytes = getString(key, null)?.base64 ?: return null
    if (bytes.isEmpty()) {
        return null
    }
    return bytes
}

fun SharedPreferences.putByteArray(key: String, value: ByteArray?) {
    if (value == null) {
        remove(key)
    } else {
        edit {
            putString(key, value.base64)
        }
    }
}

fun SharedPreferences.getIntArray(key: String): IntArray? {
    if (!contains(key)) {
        return null
    }
    val value = getString(key, null) ?: return null
    return value.split(",").mapNotNull { it.toIntOrNull() }.toIntArray()
}

fun SharedPreferences.putIntArray(key: String, value: IntArray) {
    edit {
        putString(key, value.joinToString(","))
    }
}

fun SharedPreferences.remove(key: String) {
    edit {
        remove(key)
    }
}

fun SharedPreferences.clear() {
    edit {
        clear()
    }
}

fun SharedPreferences.putString(key: String, value: String?) {
    if (value.isNullOrBlank()) {
        remove(key)
    } else {
        edit {
            putString(key, value)
        }
    }
}

fun SharedPreferences.putLong(key: String, value: Long) {
    edit {
        putLong(key, value)
    }
}

fun SharedPreferences.putInt(key: String, value: Int) {
    edit {
        putInt(key, value)
    }
}

fun SharedPreferences.getStringArray(key: String): Array<String> {
    val string = getString(key, null) ?: return emptyArray()
    return string.split(",").toTypedArray()
}


fun SharedPreferences.putStringArray(key: String, value: Array<String>) {
    putString(key, value.joinToString(","))
}

fun SharedPreferences.putBoolean(key: String, value: Boolean) {
    edit {
        putBoolean(key, value)
    }
}

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