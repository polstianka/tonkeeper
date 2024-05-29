package com.tonapps.tonkeeper.ui.screen.swap.data

import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.core.toDefaultCoinAmount
import com.tonapps.wallet.api.entity.TokenEntity

data class SwapState(
    val walletAddress: String,
    val remoteAssets: RemoteAssets? = null,
    val settings: SwapSettings = SwapSettings(),
    val send: SwapEntity = SwapEntity.EMPTY,
    val receive: SwapEntity = SwapEntity.EMPTY,
    val feesRequest: SwapRequest? = null,
    val transfer: SwapTransfer? = null,
    val simulation: SimulationResult = transfer?.request?.confirmedSimulation?.simulation ?: SimulationResult.EMPTY,
    val visibleSimulation: SimulationDisplayData? = transfer?.request?.confirmedSimulation?.visibleSimulation
) {
    enum class Status {
        WAITING_FOR_SEND_TOKEN,
        WAITING_FOR_AMOUNT,
        WAITING_FOR_RECEIVE_TOKEN,
        INVALID_SEND_AMOUNT,
        INVALID_RECEIVE_AMOUNT,
        LOADING_ASSETS,

        FIRST_SIMULATION_IN_PROGRESS,
        SIMULATION_ERROR,
        PRICE_IMPACT_TOO_HIGH,

        READY_FOR_USER_CONFIRMATION, // Continue button
        ESTIMATING_BLOCKCHAIN_FEES, // User pressed Continue -> Simulate transaction -> Display more precise swap details

        SWAP_READY,
        SWAP_IN_PROGRESS,
        SWAP_SUCCESSFUL,
        SWAP_FAILED;
    }

    fun takeSwapEntity(target: SwapTarget) = when (target) {
        SwapTarget.RECEIVE -> receive
        SwapTarget.SEND -> send
    }

    private val isConfirmation: Boolean = transfer != null

    val status: Status = when {
        isConfirmation -> {
            when (transfer?.state ?: SwapTransfer.State.WAITING_FOR_USER_CONFIRMATION) {
                SwapTransfer.State.WAITING_FOR_USER_CONFIRMATION -> Status.SWAP_READY

                SwapTransfer.State.PREPARING,
                SwapTransfer.State.WAITING_FOR_SIGNATURE,
                SwapTransfer.State.IN_PROGRESS -> Status.SWAP_IN_PROGRESS

                SwapTransfer.State.SUCCESSFUL -> Status.SWAP_SUCCESSFUL
                SwapTransfer.State.FAILED -> Status.SWAP_FAILED
            }
        }

        !send.hasToken -> Status.WAITING_FOR_SEND_TOKEN
        send.amount.isEmpty && receive.amount.isEmpty -> Status.WAITING_FOR_AMOUNT
        send.amount.isUserInput && send.amount.amount.number.scale() > send.token!!.decimals -> Status.INVALID_SEND_AMOUNT

        !receive.hasToken -> Status.WAITING_FOR_RECEIVE_TOKEN
        receive.amount.isUserInput && receive.amount.amount.number.scale() > receive.token!!.decimals -> Status.INVALID_RECEIVE_AMOUNT

        remoteAssets == null -> Status.LOADING_ASSETS

        simulation.isEmpty -> Status.FIRST_SIMULATION_IN_PROGRESS
        simulation.isError -> Status.SIMULATION_ERROR

        !settings.enableExpertMode && simulation.isSuccessful && simulation.data!!.priceImpactGrade == PriceImpactGrade.HIGH -> Status.PRICE_IMPACT_TOO_HIGH

        feesRequest != null && feesRequest.isRequestPending -> Status.ESTIMATING_BLOCKCHAIN_FEES
        else -> Status.READY_FOR_USER_CONFIRMATION
    }

    val canBeModified = !isConfirmation && status != Status.ESTIMATING_BLOCKCHAIN_FEES

    val canRunSimulations: Boolean =
        send.hasToken && receive.hasToken && (SwapConfig.APP_SIMULATION_GUESSES_ENABLED || (send.hasAmount || receive.hasAmount)) && when (status) {
            Status.WAITING_FOR_SEND_TOKEN,
            Status.WAITING_FOR_AMOUNT,
            Status.WAITING_FOR_RECEIVE_TOKEN,
            Status.INVALID_SEND_AMOUNT,
            Status.INVALID_RECEIVE_AMOUNT,
            Status.LOADING_ASSETS -> false

            Status.FIRST_SIMULATION_IN_PROGRESS,
            Status.SIMULATION_ERROR,
            Status.PRICE_IMPACT_TOO_HIGH,
            Status.READY_FOR_USER_CONFIRMATION -> true

            Status.ESTIMATING_BLOCKCHAIN_FEES -> false

            Status.SWAP_READY,
            Status.SWAP_IN_PROGRESS,
            Status.SWAP_SUCCESSFUL,
            Status.SWAP_FAILED -> false // TODO?
        }

    val requiresSimulationUpdate: Boolean = canRunSimulations && (send.amount.isOutdated || receive.amount.isOutdated)

    fun isApplicableSimulation(simulation: SimulationResult): Boolean {
        return if (send.token == simulation.send.token && receive.token == simulation.receive.token) {
            val target = if (prioritizeSendEntity) SwapTarget.SEND else SwapTarget.RECEIVE
            val entity = takeSwapEntity(target)
            val checkAmount = if (prioritizeSendEntity) simulation.send else simulation.receive
            checkAmount.amount.amount.number == entity.amount.amount.number
        } else {
            false
        }
    }

    fun findLastSuccessfulSimulation(send: TokenEntity, receive: TokenEntity, allowSwap: Boolean = true): SimulationResult? {
        return when {
            this.simulation.isSuccessful && this.simulation.belongsToTokens(send, receive, allowSwap) -> this.simulation
            else -> null
        }
    }

    val prioritizeSendEntity: Boolean =
        send.amount.isUserInput || !receive.amount.isUserInput

    val simulationErrorMessage: String
        get() = simulation.error?.message ?: ""

    fun updateWithSimulation(result: SimulationResult): SwapState {
        if (result.uptimeMillis <= this.simulation.uptimeMillis || !canRunSimulations || !isApplicableSimulation(result)) {
            return this
        }
        var send = this.send
        var receive = this.receive
        val visibleSimulation: SimulationDisplayData?
        if (result.isSuccessful) {
            if (!send.amount.isUserInput) {
                val sendAmount = result.data!!.send.coins
                send = send.withAmount(SwapAmount(
                    FormattedDecimal(sendAmount, sendAmount.toDefaultCoinAmount(send.token)),
                    SwapAmount.Origin.REMOTE_SIMULATION_RESULT
                ))
            }
            if (!receive.amount.isUserInput) {
                val receiveAmount = result.data!!.receive.coins
                receive = receive.withAmount(SwapAmount(
                    FormattedDecimal(receiveAmount, receiveAmount.toDefaultCoinAmount(receive.token)),
                    SwapAmount.Origin.REMOTE_SIMULATION_RESULT
                ))
            }
            visibleSimulation = SimulationDisplayData(App.defaultNumberFormat(), send, receive, result.data!!, transfer?.request)
        } else {
            visibleSimulation = this.visibleSimulation
        }
        return copy(
            send = send,
            receive = receive,
            simulation = result,
            visibleSimulation = visibleSimulation
        )
    }

    val hasVisibleSimulation: Boolean = visibleSimulation != null

    fun withoutContext(): SwapState {
        return if (this.simulation == SimulationResult.EMPTY && visibleSimulation == null && feesRequest == null) {
            this
        } else {
            copy(
                simulation = SimulationResult.EMPTY,
                visibleSimulation = null,
                feesRequest = null
            )
        }
    }
}