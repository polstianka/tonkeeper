package com.tonapps.tonkeeper.fragment.trade.di

import com.tonapps.tonkeeper.fragment.trade.domain.GetRateFlowCase
import org.koin.dsl.module

val ratesDomainModule = module {
    single { GetRateFlowCase(get()) }
}