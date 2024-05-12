package com.tonapps.tonkeeper.ui.screen.stake

import androidx.lifecycle.ViewModel
import com.tonapps.wallet.data.stake.StakeRepository

class PoolDetailsViewModel(
    private val stakeRepository: StakeRepository
) : ViewModel() {
    fun choose(address: String) {
        stakeRepository.select(address)
    }
}