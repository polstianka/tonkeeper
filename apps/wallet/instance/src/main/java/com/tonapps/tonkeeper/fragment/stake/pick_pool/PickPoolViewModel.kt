package com.tonapps.tonkeeper.fragment.stake.pick_pool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.stake.pick_pool.rv.PickPoolListItem
import com.tonapps.tonkeeper.fragment.stake.presentation.apyText
import com.tonapps.tonkeeper.fragment.stake.presentation.getIconUrl
import com.tonapps.uikit.list.ListCell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PickPoolViewModel : ViewModel() {

    private val args = MutableSharedFlow<PickPoolFragmentArgs>(replay = 1)
    private val _events = MutableSharedFlow<PickPoolEvents>()
    val events: Flow<PickPoolEvents>
        get() = _events
    val title = args.map { it.service.name }
    val items = args.map { args ->
        args.service.pools.mapIndexed { index, item ->
            PickPoolListItem(
                iconUrl = item.serviceType.getIconUrl(),
                title = item.name,
                subtitle = item.apyText(),
                isChecked = args.pickedPool.address == item.address,
                address = item.address,
                position = ListCell.getPosition(args.service.pools.size, index),
                isMaxApy = item.isMaxApy
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

    fun onItemClicked(item: PickPoolListItem) = viewModelScope.launch {
        val args = args.first()
        val service = args.service
        val pool = service.pools.first { it.address == item.address }
        val event = PickPoolEvents.NavigateToPoolDetails(service, pool, args.currency)
        _events.emit(event)
    }
}