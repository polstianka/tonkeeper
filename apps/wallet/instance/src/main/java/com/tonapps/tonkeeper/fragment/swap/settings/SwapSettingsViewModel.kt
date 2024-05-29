package com.tonapps.tonkeeper.fragment.swap.settings

import androidx.lifecycle.ViewModel
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import uikit.extensions.collectFlow

class SwapSettingsViewModel(
    private val settings: SettingsRepository,
) : ViewModel() {
    data class State(
        val slippage: Float?,
        private val _useCustomSlippage: Boolean,
        val customPriceImpact: Float?,
        val priceImpactVisible: Boolean,
        val priceImpactFocused: Boolean,
        val force: Boolean
    ) {
        companion object {
            var SLIPPAGE_VALUES = arrayOf(0.001f, 0.005f, 0.01f)
        }

        val slippageIndex: Int
            get() {
                if (_useCustomSlippage) {
                    return SLIPPAGE_VALUES.size
                }

                for (i in SLIPPAGE_VALUES.indices) {
                    if (slippage == SLIPPAGE_VALUES[i]) {
                        return i
                    }
                }

                return SLIPPAGE_VALUES.size
            }

        val useCustomSlippage: Boolean
            get() {
                return slippageIndex == SLIPPAGE_VALUES.size
            }

        val priceImpact: Float
            get() {
                if (priceImpactVisible) {
                    return customPriceImpact ?: SettingsRepository.DEFAULT_PRICE_IMPACT
                }
                return SettingsRepository.DEFAULT_PRICE_IMPACT
            }

        val slippageError: Boolean
            get() {
                return useCustomSlippage && (slippage?.let { it > 1f || it < 0f } ?: true)
            }

        val priceImpactError: Boolean
            get() {
                return priceImpactVisible && (customPriceImpact?.let { it > 1f || it < 0f } ?: true)
            }

        val valid: Boolean
            get() {
                return !slippageError && !priceImpactError
            }
    }

    private val _stateFlow = MutableStateFlow<State?>(null)
    val stateFlow = _stateFlow.asStateFlow().filterNotNull()

    init {
        collectFlow(settings.swapSettingsFlow) { swapSettings ->
            _stateFlow.value = State(
                slippage = swapSettings.slippage,
                _useCustomSlippage = swapSettings.slippageUseCustom,
                customPriceImpact = swapSettings.priceImpact,
                priceImpactVisible = swapSettings.priceImpactUseCustom || swapSettings.priceImpact != SettingsRepository.DEFAULT_PRICE_IMPACT,
                priceImpactFocused = false,
                force = true
            )
        }
    }

    fun onChangeSlippageValue(slippageValue: Float?, custom: Boolean) {
        _stateFlow.value = _stateFlow.value?.copy(
            slippage = slippageValue,
            _useCustomSlippage = custom,
            priceImpactFocused = false,
            force = false
        )
    }

    fun onChangePriceImpactValue(priceImpactValue: Float?) {
        _stateFlow.value = _stateFlow.value?.copy(
            customPriceImpact = priceImpactValue,
            priceImpactFocused = true,
            force = false
        )
    }

    fun onChangePriceImpactVisible(visible: Boolean) {
        _stateFlow.value = _stateFlow.value?.copy(
            priceImpactVisible = visible,
            priceImpactFocused = visible,
            force = false
        )
    }

    fun onChangePriceImpactFocused(focus: Boolean) {
        _stateFlow.value = _stateFlow.value?.copy(priceImpactFocused = focus, force = false)
    }

    fun save() {
        _stateFlow.value?.let {
            settings.swapSettings = SettingsRepository.SwapSettings(
                slippage = it.slippage ?: SettingsRepository.DEFAULT_SLIPPAGE,
                slippageUseCustom = it.useCustomSlippage,
                priceImpact = it.priceImpact,
                priceImpactUseCustom = it.priceImpactVisible
            )
        }
    }
}