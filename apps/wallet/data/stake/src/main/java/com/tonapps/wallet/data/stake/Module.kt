package com.tonapps.wallet.data.stake

import org.koin.dsl.module

val stakeModule = module {
    single { StakeRepository(get(), get(), get()) }
}