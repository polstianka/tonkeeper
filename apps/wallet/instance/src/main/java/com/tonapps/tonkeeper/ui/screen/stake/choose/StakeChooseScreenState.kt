package com.tonapps.tonkeeper.ui.screen.stake.choose

import io.tonapi.models.PoolInfo
import uikit.mvi.UiState

data class StakeChooseScreenState(
    val selectedPool: PoolInfo? = null,
    val minDeposit: String = "",
    val apy: String = "",
    val socials: List<String> = emptyList()
) : UiState()