package com.tonapps.tonkeeper.helper.flow

import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.helper.Coin2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import java.math.BigDecimal
import java.math.BigInteger

class AmountInputController {
    data class InputState(
        val minAmountNano: BigInteger,
        val maxAmountNano: BigInteger,
        val decimals: Int,
        val symbol: String
    )

    data class State(
        internal val inputState: InputState,
        val input: Coin2.Input,
    ) {
        val valueNano get() = input.coin?.value ?: BigInteger.ZERO

        val isValid get() = input.coin != null
        val isValidOrEmpty get() = isValid || input.input.isEmpty()
        val isCorrect get() = isValid && !isTooBig && !isTooSmall
        val isCorrectAndNonZero get() = isCorrect && valueNano != BigInteger.ZERO

        val useMaxAmount
            get() = input.coin?.value?.let { v -> v == inputState.maxAmountNano } ?: false
        val isTooSmall get() = input.coin?.value?.let { v -> v < inputState.minAmountNano } ?: false
        val isTooBig get() = input.coin?.value?.let { v -> v > inputState.maxAmountNano } ?: false

        val remainingFmt: String
            get() = CurrencyFormatter.format(
                inputState.symbol,
                BigDecimal(inputState.maxAmountNano - valueNano, inputState.decimals)
            ).toString()
        val availableFmt: String
            get() = CurrencyFormatter.format(
                inputState.symbol,
                BigDecimal(inputState.maxAmountNano, inputState.decimals)
            ).toString()
        val minimalFmt: String
            get() = CurrencyFormatter.format(
                inputState.symbol,
                BigDecimal(inputState.minAmountNano, inputState.decimals)
            ).toString()
    }

    private val _outputStateFlow = MutableStateFlow<State?>(null)
    val outputStateFlow = _outputStateFlow.asStateFlow().filterNotNull()

    fun setInputParams(
        minAmountNano: BigInteger,
        maxAmountNano: BigInteger,
        decimals: Int,
        symbol: String
    ) {
        val inputState = InputState(minAmountNano, maxAmountNano, decimals, symbol)

        val oldInputState = _outputStateFlow.value?.inputState
        if (oldInputState != null) {
            if (
                inputState.minAmountNano == oldInputState.minAmountNano &&
                inputState.maxAmountNano == oldInputState.maxAmountNano &&
                inputState.decimals == oldInputState.decimals &&
                inputState.symbol == oldInputState.symbol
            ) return
        }

        _outputStateFlow.value = if (_outputStateFlow.value?.inputState?.decimals == decimals) {
            _outputStateFlow.value?.copy(inputState = inputState)
        } else {
            State(inputState = inputState, input = Coin2.Input.EMPTY)
        }
    }

    fun onInput(value: CharSequence?) {
        _outputStateFlow.value = _outputStateFlow.value?.let { output ->
            output.copy(input = Coin2.Input.parse(value.toString(), output.inputState.decimals))
        }
    }

    fun clearInput() {
        _outputStateFlow.value = _outputStateFlow.value?.copy(input = Coin2.Input.EMPTY)
    }

    fun useMaxAmount() {
        _outputStateFlow.value = _outputStateFlow.value?.let { output ->
            val decimals = output.inputState.decimals
            output.copy(
                input = Coin2.Input.parse(
                    Coin2.fromNano(output.inputState.maxAmountNano.toString())?.toString(decimals)
                        ?: "", decimals
                )
            )
        }
    }

    fun toggleMax() {
        _outputStateFlow.value = _outputStateFlow.value?.let { output ->
            val decimals = output.inputState.decimals
            if (output.useMaxAmount) {
                output.copy(input = Coin2.Input.EMPTY)
            } else {
                output.copy(
                    input = Coin2.Input.parse(
                        Coin2.fromNano(output.inputState.maxAmountNano.toString())
                            ?.toString(decimals) ?: "", decimals
                    )
                )
            }
        }
    }
}