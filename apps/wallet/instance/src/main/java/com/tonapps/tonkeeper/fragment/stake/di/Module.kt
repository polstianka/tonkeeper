package com.tonapps.tonkeeper.fragment.stake.di

import com.tonapps.tonkeeper.fragment.stake.domain.StakingRepository
import org.koin.dsl.module

val stakingModule = module {
    single { StakingRepository(get()) }
}