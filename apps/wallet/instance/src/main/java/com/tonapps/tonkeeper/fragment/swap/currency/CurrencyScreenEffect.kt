package com.tonapps.tonkeeper.fragment.swap.currency

import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo

sealed interface CurrencyScreenEffect {
    data class TakeFocus(val target: Focusable) : CurrencyScreenEffect {
        enum class Focusable {
            SEND,
            RECEIVE
        }
    }

    data object NavigateToConfirm : CurrencyScreenEffect

    data class OpenTokenPicker(
        val selected: TokenInfo?,
        val except: TokenInfo?,
    ) : CurrencyScreenEffect

    data class SetSendAmount(
        val amount: Float,
    ) : CurrencyScreenEffect

    data class SetReceiveAmount(
        val amount: Float,
    ) : CurrencyScreenEffect

    data class UpdateAmounts(
        val sendAmount: Float,
        val receiveAmount: Float,
    ) : CurrencyScreenEffect
}