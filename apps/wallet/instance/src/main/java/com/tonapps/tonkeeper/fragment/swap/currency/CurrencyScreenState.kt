package com.tonapps.tonkeeper.fragment.swap.currency

import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo

data class CurrencyScreenState(
    val sendInfo: SwapInfo = SwapInfo(),
    val receiveInfo: SwapInfo = SwapInfo(),
    val simulation: Boolean = false,
    val loadingDetails: Boolean = false,
    val details: Details = Details(),
) {

    val buttonState: ButtonState
        get() {
            return if (receiveInfo.token == null) {
                ButtonState.CHOOSE_TOKEN
            } else if (sendInfo.amount == 0f) {
                ButtonState.ENTER_AMOUNT
            } else if (loadingDetails) {
                ButtonState.LOADING
            } else {
                ButtonState.CONTINUE
            }
        }

    data class SwapInfo(
        val amount: Float = 0f,
        val token: TokenInfo? = null,
    )

    data class Details(
        val items: List<SwapDetailsItem> = emptyList(),
        val expanded: Boolean = true,
    )

    enum class ButtonState {
        ENTER_AMOUNT,
        CHOOSE_TOKEN,
        CONTINUE,
        LOADING
    }
}
