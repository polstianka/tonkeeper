package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class SwapAmount(
    val amount: FormattedDecimal = FormattedDecimal(BigDecimal.ZERO),
    val origin: Origin = Origin.STALE_INPUT
) : Parcelable {
    enum class Origin {
        /**
         * User entered this input. Should not be touched by app.
         */
        ACTIVE_USER_INPUT,

        /**
         * User pressed "MAX" button.
         */
        BALANCE_MAXIMUM,

        /**
         * App shall replace this amount with up-to-date rate as soon as possible.
         */
        STALE_INPUT,

        /**
         * App shall replace this amount with up-to-date rate as soon as possible.
         */
        LOCAL_SIMULATION_GUESS,

        /**
         * App shall replace whatever in the input field with this up-to-date rate.
         */
        REMOTE_SIMULATION_RESULT
    }

    companion object {
        val EMPTY = SwapAmount()
    }

    @IgnoredOnParcel
    val isEmpty: Boolean =
        amount.number == BigDecimal.ZERO

    @IgnoredOnParcel
    val isUserInput: Boolean =
        when (origin) {
            Origin.ACTIVE_USER_INPUT,
            Origin.BALANCE_MAXIMUM -> true

            Origin.STALE_INPUT,
            Origin.LOCAL_SIMULATION_GUESS,
            Origin.REMOTE_SIMULATION_RESULT -> false
        }

    @IgnoredOnParcel
    val isOutdated: Boolean =
        when (origin) {
            Origin.STALE_INPUT,
            Origin.LOCAL_SIMULATION_GUESS -> true

            Origin.ACTIVE_USER_INPUT,
            Origin.REMOTE_SIMULATION_RESULT,
            Origin.BALANCE_MAXIMUM -> false
        }

    fun withOrigin(origin: Origin): SwapAmount =
        if (this.origin != origin) {
            copy(origin = origin)
        } else {
            this
        }
}