package com.tonapps.wallet.data.rates

import org.koin.dsl.module

val ratesDataModule = module {
    single { RatesRepository(get(), get()) }
}