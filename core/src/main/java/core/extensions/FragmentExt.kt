package core.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun <T> Fragment.observeFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        flow.collectLatest(action)
    }
}