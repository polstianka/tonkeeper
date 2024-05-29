package com.tonapps.tonkeeper.ui.screen.swap.data

import com.tonapps.tonkeeper.core.signer.SingerResultContract
import org.ton.cell.Cell

data class SwapTransfer(
    val request: SwapRequest,
    val state: State = State.WAITING_FOR_USER_CONFIRMATION,
    val signerInput: SingerResultContract.Input? = null,
    val error: String? = null,
    val seqno: Int = -1,
    val success: Cell? = null
) {
    enum class State {
        WAITING_FOR_USER_CONFIRMATION,

        PREPARING,
        WAITING_FOR_SIGNATURE,
        IN_PROGRESS,

        SUCCESSFUL,
        FAILED;

        val inProgress: Boolean
            get() = when (this) {
                PREPARING, WAITING_FOR_SIGNATURE, IN_PROGRESS -> true
                WAITING_FOR_USER_CONFIRMATION, SUCCESSFUL, FAILED -> false
            }
    }

    val canPerformOnBlockchain: Boolean
        get() = state == State.WAITING_FOR_USER_CONFIRMATION || state == State.FAILED

    fun getSwapEntity(target: SwapTarget): SwapEntity {
        return when (target) {
            SwapTarget.SEND -> request.send
            SwapTarget.RECEIVE -> request.receive
        }
    }
}