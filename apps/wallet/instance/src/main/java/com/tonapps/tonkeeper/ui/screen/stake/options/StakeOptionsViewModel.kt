package com.tonapps.tonkeeper.ui.screen.stake.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.helper.NumberFormatter
import com.tonapps.tonkeeper.ui.screen.stake.model.icon
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsUiState.StakeInfo
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.StakePoolsEntity
import com.tonapps.wallet.api.entity.StakePoolsEntity.PoolImplementationType
import com.tonapps.wallet.data.stake.StakeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.math.BigDecimal

class StakeOptionsViewModel(
    private val repository: StakeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StakeOptionsUiState())
    val uiState: StateFlow<StakeOptionsUiState> = _uiState

    init {
        repository.selectedPoolAddress
            .onStart {
                init()
            }.onEach { address ->
                init(address)
            }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }

    fun select(address: String) {

    }

    private suspend fun init(selectedAddress: String = "") {
        val pools = repository.get()
        val typeToInfoMap = pools.pools.groupBy { it.implementation }
        val maxApyByType = mutableMapOf<PoolImplementationType, BigDecimal>()
        val minStakeByType = mutableMapOf<PoolImplementationType, Long>()
        val maxApy = pools.pools.maxByOrNull { it.apy } ?: return
        typeToInfoMap.keys.forEach { typeKey ->
            maxApyByType[typeKey] = typeToInfoMap.getValue(typeKey).maxOf { it.apy }
            minStakeByType[typeKey] = typeToInfoMap.getValue(typeKey).minOf { it.minStake }
        }
        val optionsList = generatePairOfLists(
            typeToInfoMap = typeToInfoMap,
            pools = pools,
            maxApy = maxApy,
            maxApyByType = maxApyByType,
            minStakeByType = minStakeByType,
            selectedAddress = selectedAddress
        )

        _uiState.value = StakeOptionsUiState(optionsList)
    }

    private fun generatePairOfLists(
        typeToInfoMap: Map<PoolImplementationType, List<StakePoolsEntity.PoolInfo>>,
        pools: StakePoolsEntity,
        maxApy: StakePoolsEntity.PoolInfo,
        maxApyByType: MutableMap<PoolImplementationType, BigDecimal>,
        minStakeByType: MutableMap<PoolImplementationType, Long>,
        selectedAddress: String,
    ): List<StakeInfo> {
        val liquid = mutableListOf<StakeInfo.Liquid>()
        val other = mutableListOf<StakeInfo.Other>()
        typeToInfoMap.forEach { (key, value) ->
            val implementation = pools.implementations.getValue(key.value)
            val minStake = minStakeByType[key] ?: 0
            val links = implementation.socials + implementation.url
            val maxApyFormatted = NumberFormatter.format(maxApyByType[key])
            val isMaxApy = maxApy.implementation == key

            if (key == PoolImplementationType.liquidTF) {
                value.forEachIndexed { index, info ->
                    liquid.add(
                        StakeInfo.Liquid(
                            address = info.address,
                            name = info.name,
                            description = implementation.description,
                            maxApyFormatted = maxApyFormatted,
                            isMaxApy = isMaxApy,
                            type = key,
                            selected = if (selectedAddress.isEmpty()) isMaxApy else info.address == selectedAddress,
                            minStake = minStake,
                            links = links,
                            position = ListCell.getPosition(value.size, index),
                            iconRes = info.implementation.icon
                        )
                    )
                }
            } else {
                other.add(
                    StakeInfo.Other(
                        name = implementation.name,
                        description = implementation.description,
                        maxApyFormatted = maxApyFormatted,
                        isMaxApy = maxApy.implementation == key,
                        type = key,
                        minStake = minStake,
                        links = links,
                        maxApyAddress = maxApy.address,
                        iconRes = key.icon
                    )
                )
            }
        }

        val positionedOther = other.mapIndexed { index, it ->
            it.copy(position = ListCell.getPosition(other.size, index))
        }
        return liquid + positionedOther
    }
}


data class StakeOptionsUiState(
    val info: List<StakeInfo> = emptyList()
) {

    sealed class StakeInfo(
        open val name: String,
        open val description: String,
        open val maxApyFormatted: String,
        open val isMaxApy: Boolean,
        open val type: PoolImplementationType,
        open val position: ListCell.Position,
        open val minStake: Long,
        open val links: List<String>,
        open val iconRes: Int,
    ) {
        data class Liquid(
            override val name: String,
            override val description: String,
            override val maxApyFormatted: String,
            override val isMaxApy: Boolean,
            override val type: PoolImplementationType,
            override val position: ListCell.Position = ListCell.Position.SINGLE,
            override val minStake: Long,
            override val links: List<String>,
            override val iconRes: Int,
            val address: String,
            val selected: Boolean
        ) : StakeInfo(
            name = name,
            description = description,
            maxApyFormatted = maxApyFormatted,
            isMaxApy = isMaxApy,
            type = type,
            position = position,
            minStake = minStake,
            links = links,
            iconRes = iconRes
        )

        data class Other(
            override val name: String,
            override val description: String,
            override val maxApyFormatted: String,
            override val isMaxApy: Boolean,
            override val type: PoolImplementationType,
            override val position: ListCell.Position = ListCell.Position.SINGLE,
            override val minStake: Long,
            override val links: List<String>,
            override val iconRes: Int,
            val maxApyAddress: String,
        ) : StakeInfo(
            name = name,
            description = description,
            maxApyFormatted = maxApyFormatted,
            isMaxApy = isMaxApy,
            type = type,
            position = position,
            minStake = minStake,
            links = links,
            iconRes = iconRes
        )
    }

}