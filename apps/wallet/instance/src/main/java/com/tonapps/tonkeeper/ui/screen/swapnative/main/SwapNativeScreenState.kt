package com.tonapps.tonkeeper.ui.screen.swapnative.main

import androidx.annotation.StringRes
import com.tonapps.wallet.localization.Localization
import uikit.widget.ProgressButton


data class SwapNativeScreenState(
    val showMainLoading: Boolean = false,
    val continueState: ContinueState = ContinueState.ENTER_AMOUNT,
) {

    sealed class ContinueState(
        @StringRes val text: Int? = null,
        val enabled: ProgressButton.EnableState,
    ) {
        object ENTER_AMOUNT : ContinueState(Localization.enter_an_amount, ProgressButton.EnableState.Disable)
        object NEXT : ContinueState(Localization.continue_action, ProgressButton.EnableState.EnableActiveColor)
        object SELECT_TOKEN : ContinueState(Localization.select_token, ProgressButton.EnableState.Disable)
        object INSUFFICIENT_BALANCE : ContinueState(Localization.insufficient_asset_balance, ProgressButton.EnableState.Disable)
        object INSUFFICIENT_TON_BALANCE :
            ContinueState(Localization.insufficient_ton_balance, ProgressButton.EnableState.EnableDeactiveColor)

        object LOADING : ContinueState(enabled = ProgressButton.EnableState.DisableIgnoreBackground)
        object DISABLE : ContinueState(Localization.continue_action, ProgressButton.EnableState.Disable)

    }


}
