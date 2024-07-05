package com.tonapps.wallet.data.rates

import org.koin.dsl.module

val operatorRatesModule = module {
    single { OperatorRatesRepository(get(), get()) }
}