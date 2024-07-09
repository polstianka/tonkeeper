package com.tonapps.tonkeeper.fragment.trade.domain

import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry

class GetRateFlowCase(
    private val repository: RatesRepository
) {

    companion object {
        private const val DELAY_MILLIS = 5_000L
        private const val TOKEN_TON = "TON"
    }

    fun execute(
        currency: WalletCurrency = WalletCurrency.DEFAULT,
        initialTokens: List<String> = emptyList()
    ): Flow<RatesEntity> = flow {
        val tokens = initialTokens + TOKEN_TON
        while (true) {
            repository.load(currency, tokens.toMutableList())
            val rates = repository.getRates(currency, tokens)
            emit(rates)
            delay(DELAY_MILLIS)
        }
    }.retry {
        delay(DELAY_MILLIS)
        true
    }
}