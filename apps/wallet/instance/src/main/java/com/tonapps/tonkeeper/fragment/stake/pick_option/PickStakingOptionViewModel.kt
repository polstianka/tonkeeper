package com.tonapps.tonkeeper.fragment.stake.pick_option

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.stake.pick_option.rv.StakingOptionListItem
import com.tonapps.tonkeeper.fragment.stake.presentation.getIconUri
import com.tonapps.uikit.list.ListCell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PickStakingOptionViewModel : ViewModel() {

    private val args = MutableSharedFlow<PickStakingOptionFragmentArgs>(replay = 1)
    private val _events = MutableSharedFlow<PickStakingOptionEvent>()
    val items = args.map { args ->
        args.options.mapIndexed { index, item ->
            StakingOptionListItem(
                iconUrl = item.type.getIconUri(),
                title = item.name,
                isMaxApy = index == 0,
                subtitle = item.description, // todo
                isPicked = args.picked.serviceType == item.type,
                stakingServiceType = item.type,
                position = ListCell.getPosition(args.options.size, index)
            )
        }
    }

    val events: Flow<PickStakingOptionEvent>
        get() = _events

    fun provideArgs(args: PickStakingOptionFragmentArgs) {
        emit(this.args, args)
    }

    fun onChevronClicked() {
        emit(_events, PickStakingOptionEvent.NavigateBack)
    }

    fun onCrossClicked() {
        emit(_events, PickStakingOptionEvent.CloseFlow)
    }

    fun onItemClicked(item: StakingOptionListItem) = viewModelScope.launch {
        // todo add branching when there's only one option
        val args = args.first()
        val domainItem = args.options.first { it.type == item.stakingServiceType }
        val pickedItem = args.picked
        val currency = args.currency
        val event = if (domainItem.pools.size == 1) {
            PickStakingOptionEvent.ShowPoolDetails(
                domainItem,
                domainItem.pools.first(),
                currency
            )
        } else {
            PickStakingOptionEvent.ShowPoolPicker(
                domainItem,
                pickedItem,
                currency
            )
        }
        _events.emit(event)
    }
}