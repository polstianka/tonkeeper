package com.tonapps.tonkeeper.fragment.trade.di

import com.tonapps.tonkeeper.fragment.trade.buy.vm.BuyListHolder
import com.tonapps.tonkeeper.fragment.trade.domain.GetAvailableCurrenciesCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetBuyMethodsCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetDefaultCurrencyCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetPaymentOperatorsCase
import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import com.tonapps.tonkeeper.fragment.trade.ui.rv.mapper.BuyMethodMapper
import org.koin.dsl.module

val ratesDomainModule = module {
    single { GetRateFlowCase(get()) }
    single { BuyMethodMapper() }
    single { GetBuyMethodsCase() }
    single { BuyListHolder(get()) }
    single { GetAvailableCurrenciesCase() }
    single { GetDefaultCurrencyCase() }
    single { GetPaymentOperatorsCase() }
}