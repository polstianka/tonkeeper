package com.tonapps.tonkeeper.fragment.trade.di

import com.tonapps.tonkeeper.fragment.trade.exchange.vm.ExchangeItems
import com.tonapps.tonkeeper.fragment.trade.domain.GetAvailableCurrenciesCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetExchangeMethodsCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetDefaultCurrencyCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetPaymentOperatorsCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.tonkeeper.fragment.trade.ui.rv.mapper.ExchangeMethodMapper
import org.koin.dsl.module

val ratesDomainModule = module {
    single { GetRateFlowCase(get()) }
    single { ExchangeMethodMapper() }
    single { GetExchangeMethodsCase() }
    factory { ExchangeItems(get()) }
    single { GetAvailableCurrenciesCase() }
    single { GetDefaultCurrencyCase() }
    single { GetPaymentOperatorsCase() }
}