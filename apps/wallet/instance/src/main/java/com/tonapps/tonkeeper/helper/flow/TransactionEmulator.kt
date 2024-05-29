package com.tonapps.tonkeeper.helper.flow

import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.WalletEntity
import io.tonapi.models.MessageConsequences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.ton.contract.wallet.WalletTransfer

object TransactionEmulator {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T : Request> makeTransactionEmulatorFlow(api: API, requestsFlow: Flow<T>): Flow<State<T>> {
        return requestsFlow.flatMapLatest { value ->
            flow {
                emit((State(loading = true, result = null, error = null, request = value)))
                try {
                    val result = emulate(api, value)
                    emit((State(loading = false, result = result, error = null, request = value)))
                } catch (t: Throwable) {
                    emit((State(loading = false, result = null, error = t, request = value)))
                }
            }
        }
    }

    private suspend fun emulate(api: API, request: Request): MessageConsequences = withContext(
        Dispatchers.IO
    ) {
        request.getWallet().emulate(api, request.getTransfer())
    }

    data class State<T : Request>(
        val request: T,
        val loading: Boolean,
        val result: MessageConsequences?,
        val error: Throwable?
    )

    interface Request {
        fun getWallet(): WalletEntity
        fun getTransfer(): WalletTransfer
    }
}