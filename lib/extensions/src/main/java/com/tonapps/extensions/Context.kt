package com.tonapps.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.LocaleList
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import uikit.extensions.activity
import java.io.File
import java.util.Locale
import java.util.concurrent.CancellationException

val isUIThread: Boolean
    get() = Thread.currentThread() == android.os.Looper.getMainLooper().thread

val Context.locale: Locale
    get() {
        return resources.configuration.locales.get(0)
    }

val Context.locales: LocaleList
    get() {
        return resources.configuration.locales
    }

fun Context.setLocales(locales: LocaleListCompat) {
    try {
        AppCompatDelegate.setApplicationLocales(locales)
    } catch (e: Throwable) {
        recreate()
    }
}

fun Context.recreate() {
    val activity = activity ?: return
    ActivityCompat.recreate(activity)
}

val Context.isDebug: Boolean
    get() = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

fun Context.cacheFolder(name: String): File {
    return cacheDir.folder(name)
}

fun Context.raw(@RawRes id: Int): ByteArray {
    return resources.openRawResource(id).readBytes()
}

fun Context.rawText(@RawRes id: Int): String {
    return raw(id).toString(Charsets.UTF_8)
}

val Context.packageInfo: PackageInfo
    get() = packageManager.getPackageInfo(packageName, 0)

val Context.appVersionName: String
    get() = packageInfo.versionName ?: "unknown"

val Context.appVersionCode: Long
    get() = packageInfo.versionCodeCompat

fun Context.prefs(name: String): SharedPreferences {
    return getSharedPreferences(name, Context.MODE_PRIVATE)
}

val Context.activity: ComponentActivity?
    get() {
        var context = this
        while (context is ContextWrapper) {
            if (context is ComponentActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }


fun Context.logError(e: Throwable) {
    if (e is CancellationException) {
        return
    }
    Log.e("TonkeeperLog", e.message, e)
}
