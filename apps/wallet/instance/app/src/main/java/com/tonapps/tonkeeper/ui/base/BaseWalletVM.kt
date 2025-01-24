package com.tonapps.tonkeeper.ui.base

import android.app.Application
import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tonapps.extensions.bestMessage
import com.tonapps.extensions.isUIThread
import com.tonapps.tonkeeper.extensions.loading
import com.tonapps.tonkeeper.extensions.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.base.BaseFragment
import uikit.navigation.Navigation
import uikit.navigation.Navigation.Companion.navigation
import java.lang.ref.WeakReference
import java.util.concurrent.Executor

abstract class BaseWalletVM(
    app: Application
): AndroidViewModel(app) {

    interface Holder {
        val uiContext: Context?
        fun finish()
    }

    class EmptyViewViewModel(app: Application): BaseWalletVM(app)

    private var holderRef: WeakReference<Holder>? = null

    private val holder: Holder?
        get() = holderRef?.get()

    val context: Context
        get() = holder?.uiContext ?: getApplication()

    private val navigation: Navigation?
        get() = Navigation.from(context)

    open fun attachHolder(holder: Holder) {
        holderRef = WeakReference(holder)
    }

    fun <T> Flow<T>.launch() {
        this.launchIn(viewModelScope)
    }

    fun <T> Flow<T>.collectFlow(action: suspend (T) -> Unit) {
        this.onEach { action(it) }.launch()
    }

    fun detachHolder() {
        holderRef?.clear()
        holderRef = null
    }

    fun getString(resId: Int) = context.getString(resId)

    fun getString(resId: Int, vararg formatArgs: Any) = context.getString(resId, *formatArgs)

    override fun onCleared() {
        super.onCleared()
        detachHolder()
    }

    @UiThread
    suspend fun finish() = withContext(Dispatchers.Main) {
        holder?.finish()
    }

    suspend fun toast(@StringRes resId: Int) = withContext(Dispatchers.Main) {
        context.showToast(resId)
    }

    suspend fun toast(text: String) = withContext(Dispatchers.Main) {
        context.showToast(text)
    }

    suspend fun loading(loading: Boolean) = withContext(Dispatchers.Main) {
        context.loading(loading)
    }

    suspend fun openScreen(screen: BaseFragment) = withContext(Dispatchers.Main) {
        try {
            navigation?.add(screen)
        } catch (e: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}