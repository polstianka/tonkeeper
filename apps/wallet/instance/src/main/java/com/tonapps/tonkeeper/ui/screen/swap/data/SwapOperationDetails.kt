package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import com.tonapps.wallet.data.rates.entity.RatesEntity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

enum class SwapOperationError {
    SIMULATION_FAILED,
    CURRENCY_RATE_UPDATE_FAILED,
    LOW_BALANCE_MAY_FAIL;

    val isFatal: Boolean
        get() = when (this) {
            SIMULATION_FAILED -> true
            CURRENCY_RATE_UPDATE_FAILED,
            LOW_BALANCE_MAY_FAIL -> false
        }
}

@Parcelize
data class SwapOperationDetails(
    val transferDetails: SwapRequestTransferDetails? = null,
    val feesInTon: FormattedDecimal? = null,
    val ratesInUserCurrency: RatesEntity? = null,
    val error: SwapOperationError? = null,
    val errorMessage: String? = null,
    val isFatalError: Boolean = error?.isFatal ?: false
) : Parcelable {
    @IgnoredOnParcel
    val hasError: Boolean
        get() = error != null

    @IgnoredOnParcel
    val canAttempt: Boolean
        get() = transferDetails != null && (!hasError || !isFatalError)

    fun withoutError(): SwapOperationDetails {
        return if (hasError) {
            copy(
                error = null,
                errorMessage = null,
                isFatalError = false
            )
        } else {
            this
        }
    }
}

@Parcelize
data class SwapSimulation(
    val simulation: SimulationResult,
    val visibleSimulation: SimulationDisplayData,
) : Parcelable