package uikit.extensions

import android.view.View
import android.view.ViewTreeObserver
import android.widget.ScrollView
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

val ScrollView.verticalOffset: Flow<Int>
    get() = callbackFlow {
        val scrollListener = ViewTreeObserver.OnScrollChangedListener {
            trySend(scrollY)
        }

        val layoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            trySend(scrollY)
        }

        viewTreeObserver.addOnScrollChangedListener(scrollListener)
        addOnLayoutChangeListener(layoutListener)
        trySend(scrollY)

        awaitClose {
            viewTreeObserver.removeOnScrollChangedListener(scrollListener)
            removeOnLayoutChangeListener(layoutListener)
        }
    }

val ScrollView.isMaxScrollReached: Boolean
    get() {
        val view = getChildAt(0) ?: return false
        val maxScroll = view.measuredHeight + paddingTop + paddingBottom - height
        return scrollY >= maxScroll
    }

val ScrollView.topScrolled: Flow<Boolean>
    get() = verticalOffset.map {
        it > 0
    }.distinctUntilChanged()

val ScrollView.bottomScrolled: Flow<Boolean>
    get() = verticalOffset.map {
        !isMaxScrollReached
    }.distinctUntilChanged()

