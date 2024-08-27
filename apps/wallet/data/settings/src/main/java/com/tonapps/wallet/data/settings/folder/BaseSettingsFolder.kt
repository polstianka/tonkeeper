package com.tonapps.wallet.data.settings.folder

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.extensions.getByteArray
import com.tonapps.extensions.getIntArray
import com.tonapps.extensions.putIntArray
import com.tonapps.extensions.state
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.shareIn

internal abstract class BaseSettingsFolder(
    context: Context,
    scope: CoroutineScope,
    name: String
) {

    private val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private val _changedFlow = MutableEffectFlow<Unit>()
    val changedFlow = _changedFlow.shareIn(scope, SharingStarted.Lazily, 1)

    init {
        notifyChanged()
    }

    fun clear() {
        prefs.edit().clear().apply()
        notifyChanged()
    }

    private fun notifyChanged() {
        _changedFlow.tryEmit(Unit)
    }

    fun contains(key: String) = prefs.contains(key)

    fun getBoolean(key: String, defValue: Boolean = false) = prefs.getBoolean(key, defValue)

    fun getInt(key: String, defValue: Int = 0) = prefs.getInt(key, defValue)

    fun getLong(key: String, defValue: Long = 0) = prefs.getLong(key, defValue)

    fun getIntArray(key: String, def: IntArray? = null) = prefs.getIntArray(key) ?: def

    fun putIntArray(key: String, value: IntArray, notify: Boolean = true) {
        prefs.putIntArray(key, value)
        if (notify) {
            notifyChanged()
        }
    }

    fun putLong(key: String, value: Long, notify: Boolean = true) {
        prefs.edit().putLong(key, value).apply()
        if (notify) {
            notifyChanged()
        }
    }

    fun putBoolean(key: String, value: Boolean, notify: Boolean = true) {
        prefs.edit().putBoolean(key, value).apply()
        if (notify) {
            notifyChanged()
        }
    }

    fun putInt(key: String, value: Int, notify: Boolean = true) {
        prefs.edit().putInt(key, value).apply()
        if (notify) {
            notifyChanged()
        }
    }

    fun edit(notify: Boolean = true, block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
        if (notify) {
            notifyChanged()
        }
    }
}