package com.tonapps.tonkeeper.ui.screen.swap.data

import com.tonapps.tonkeeperx.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date

class SwapConfig {
    companion object {
        val LOGGING_ENABLED = BuildConfig.DEBUG
        const val LOGGING_TAG = "tonkeeper-swap"

        const val APP_SIMULATION_GUESSES_ENABLED = false
        const val APP_SIMULATIONS_REFETCH_INTERVAL_MS = 5000L

        const val DEX_TRANSACTION_TON_AMOUNT_MARGIN = "10000000"

        const val APP_INPUT_DEBOUNCE_TIMEOUT_MS = 200L
        const val APP_SETTINGS_CHANGE_DEBOUNCE_TIMEOUT_MS = 750L
        const val DEX_SWAP_ESTIMATED_FEE = "0.08 - 0.25"

        val DEX_SWAP_GRADE_PRICE_IMPACT_PERCENT_LOW = 0.01.toBigDecimal()
        val DEX_SWAP_GRADE_PRICE_IMPACT_PERCENT_HIGH = 0.05.toBigDecimal()
        val DEX_SWAP_GRADE_PRICE_IMPACT_THRESHOLD_PERCENT = 0.01.toBigDecimal()

        const val QUICK_TOKEN_PICKER_ASSET_COUNT_MAX = -1 // set to -1 to disable

        const val DEX_SWAP_GRADE_FAIR_PRICE_PERCENT = 0.01
        const val DEX_GRADE_GREAT_PRICE_PERCENT = 0.05

        @SuppressWarnings("SimpleDateFormat")
        fun debugTimestamp(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())

        // "error": "not enough bits" for uninitialized wallet, but without stateInit operation succeeds. Why?
        const val NEED_STATE_INIT = false

        const val ALIGN_BUTTON_BELOW_CONTENT_WHEN_ACTIVE = false
        const val INITIAL_SIMULATION_PROGRESS_VISUAL = true
        const val DEFAULT_SWAP_DETAILS_VISIBLE = false

        const val ENABLE_VIBRATIONS = true

        const val FORCE_CHECK_USD_RATE = true // STON.fi returns some weird rates,
        const val EXCLUDE_GAS_FROM_MAX_AMOUNT = true
        const val EXCLUDE_GAS_FROM_INSUFFICIENT_FUNDS = false

        const val DEBUG_AMOUNT_INPUT = false
        const val MAX_INCREASE_FRACTIONAL_PART = 20

        // TODO:
        //  1. Designer must determine ripple overlay color.
        //  2. Create new UIKitColor with it.
        //  3. Use it instead of opaque colors to allow button background color animations on tap
        const val TRANSPARENT_LABEL_ACTION_RIPPLE = true

        // I think `false` is better, but following contest mockups
        const val APPLY_WEIRD_RECEIVE_WRAP_BOTTOM_PADDING = true
    }
}