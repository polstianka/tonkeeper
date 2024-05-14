package com.tonapps.tonkeeper.fragment.stake.pick_pool

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.stake.pick_pool.rv.PickPoolListItem
import com.tonapps.tonkeeper.fragment.stake.presentation.getIconUrl
import com.tonapps.uikit.list.ListCell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class PickPoolViewModel : ViewModel() {

    private val args = MutableSharedFlow<PickPoolFragmentArgs>(replay = 1)
    private val _events = MutableSharedFlow<PickPoolEvents>()
    val events: Flow<PickPoolEvents>
        get() = _events
    val title = args.map { it.title }
    val items = args.map { args ->
        args.pools.mapIndexed { index, item ->
            PickPoolListItem(
                iconUrl = item.serviceType.getIconUrl(),
                title = item.name,
                subtitle = "todo",
                isChecked = args.pickedPool.address == item.address,
                accountNumber = item.address,
                position = ListCell.getPosition(args.pools.size, index)
            )
        }
    }
    fun provideArguments(pickPoolFragmentArgs: PickPoolFragmentArgs) {
        emit(args, pickPoolFragmentArgs)
    }

    fun onChevronClicked() {
        emit(_events, PickPoolEvents.NavigateBack)
    }

    fun onCloseClicked() {
        emit(_events, PickPoolEvents.CloseFlow)
    }

    fun onItemClicked(item: PickPoolListItem) {
        Log.wtf("###", "onItemClicked: $item")
    }
}