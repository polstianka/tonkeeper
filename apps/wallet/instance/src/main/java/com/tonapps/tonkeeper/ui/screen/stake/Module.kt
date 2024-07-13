package com.tonapps.tonkeeper.ui.screen.stake

import org.koin.dsl.module

val stakingModule = module {
    single { StakingRepository(get(), get()) }
}