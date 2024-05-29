package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class SwapRequest(
    val sourceWallet: String,
    val send: SwapEntity,
    val receive: SwapEntity,
    val prioritizeSendAmount: Boolean = true,
    val confirmedSimulation: SwapSimulation,
    val operationDetails: SwapOperationDetails? = null
) : Parcelable {
    enum class Type {
        TON_TO_JETTON,
        JETTON_TO_JETTON,
        JETTON_TO_TON
    }

    @IgnoredOnParcel
    val sendAsset: AssetEntity
        get() = send.asset!!

    @IgnoredOnParcel
    val receiveAsset: AssetEntity
        get() = receive.asset!!

    @IgnoredOnParcel
    val type = when {
        sendAsset.kind == AssetKind.TON && receiveAsset.kind == AssetKind.JETTON -> Type.TON_TO_JETTON
        sendAsset.kind == AssetKind.JETTON && receiveAsset.kind == AssetKind.JETTON -> Type.JETTON_TO_JETTON
        sendAsset.kind == AssetKind.JETTON && receiveAsset.kind == AssetKind.TON -> Type.JETTON_TO_TON
        else -> error("Unsupported swap: ${sendAsset.kind} —> ${receiveAsset.kind}")
    }

    @IgnoredOnParcel
    val isRequestPending = operationDetails == null

    fun withoutError(): SwapRequest {
        return if (operationDetails != null && operationDetails.hasError) {
            copy(operationDetails = operationDetails.withoutError())
        } else {
            this
        }
    }
}