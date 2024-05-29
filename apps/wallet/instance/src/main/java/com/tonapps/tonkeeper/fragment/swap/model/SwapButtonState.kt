package com.tonapps.tonkeeper.fragment.swap.model

import android.content.Context
import android.graphics.Color
import com.tonapps.uikit.color.buttonPrimaryForegroundColor
import com.tonapps.uikit.color.buttonSecondaryForegroundColor
import com.tonapps.wallet.localization.Localization

enum class SwapButtonState(
    val textRes: Int?,
    val backgroundRes: Int,
    val showLoading: Boolean) {

    EnterAmount(
        textRes = Localization.swap_btn_enter_amount,
        backgroundRes = uikit.R.drawable.bg_button_secondary,
        showLoading = false
    ) {
        override fun textColor(context: Context): Int {
            return context.buttonSecondaryForegroundColor
        }
    },
    ChooseToken(
        textRes = Localization.swap_btn_choose_token,
        backgroundRes = uikit.R.drawable.bg_button_secondary,
        showLoading = false
    ) {
        override fun textColor(context: Context): Int {
            return context.buttonSecondaryForegroundColor
        }
    },
    Loading(
        textRes = null,
        backgroundRes = uikit.R.drawable.bg_button_secondary,
        showLoading = true
    ) {
        override fun textColor(context: Context): Int {
            return Color.TRANSPARENT
        }
    },
    Confirm(
        textRes = Localization.continue_action,
        backgroundRes = uikit.R.drawable.bg_button_primary,
        showLoading = false
    ) {
        override fun textColor(context: Context): Int {
            return context.buttonPrimaryForegroundColor
        }
    };

    abstract fun textColor(context: Context): Int

}