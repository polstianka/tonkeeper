package com.tonapps.tonkeeper.ui.screen.stake

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.tonapi.models.PoolImplementation
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import uikit.mvi.UiFeature

class StakeScreenFeature : UiFeature<StakeScreenState, StakeScreenEffect>(StakeScreenState()) {

    private val _onReadyView: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val onReadyView = _onReadyView.asStateFlow()

    private val _data = MutableLiveData(StakeData())
    val data: LiveData<StakeData> = _data

    fun readyView() {
        _onReadyView.value = true
    }

    fun setAmount(amount: String) {
        _data.value = _data.value?.copy(amount = amount)
    }

    fun setPool(pool: PoolInfo?) {
        _data.value = _data.value?.copy(poolInfo = pool)
    }

    fun setPoolCandidate(pool: PoolInfo?) {
        _data.value?.poolInfoCandidate = pool
    }

    fun setHeaderTitle(title: String) {
        updateUiState { it.copy(headerTitle = title) }
    }

    fun setHeaderVisible(visibility: Boolean) {
        updateUiState { it.copy(headerVisible = visibility) }
    }

    fun nextPage() {
        updateUiState { it.copy(currentPage = it.currentPage + 1) }
    }

    fun prevPage() {
        updateUiState { it.copy(currentPage = it.currentPage - 1) }
    }

    fun setCurrentPage(index: Int) {
        updateUiState { it.copy(currentPage = index) }
    }

    fun setPreData(address: String, unstake: Boolean) {
        _data.value?.preAddress = address
        _data.value?.preUnstake = unstake
    }
}