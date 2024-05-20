package com.tonapps.tonkeeper.ui.screen.stake.pools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.helper.NumberFormatter
import com.tonapps.tonkeeper.ui.screen.stake.model.PoolModel
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.StakePoolsEntity
import com.tonapps.wallet.data.stake.StakeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StakePoolsViewModel(
    private val stakeRepository: StakeRepository
) : ViewModel() {

    private val _items = MutableStateFlow(emptyList<PoolModel>())
    val items: StateFlow<List<PoolModel>> = _items

    fun load(type: StakePoolsEntity.PoolImplementationType, maxApyAddress: String) {
        stakeRepository.selectedPoolAddress.onEach { selectedAddress ->
            val poolsEntity = stakeRepository.get()
            val impl = poolsEntity.implementations.getValue(type.value)
            val pools = poolsEntity.pools.filter { it.implementation == type }
            _items.value = pools.mapIndexed { index, it ->
                PoolModel(
                    address = it.address,
                    name = it.name,
                    apyFormatted = NumberFormatter.format(it.apy),
                    isMaxApy = it.address == maxApyAddress,
                    implType = it.implementation,
                    position = ListCell.getPosition(pools.size, index),
                    minStake = it.minStake,
                    links = impl.socials + impl.url,
                    selected = it.address == selectedAddress
                )
            }
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }

    fun select(address: String) {
        stakeRepository.select(address)
    }
}