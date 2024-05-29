package com.tonapps.wallet.data.pools

import org.koin.dsl.module

val poolsModule = module {
    single { StakingPoolsRepository(get(), get(), get()) }
    single { StakingHistoryRepository(get(), get()) }
}