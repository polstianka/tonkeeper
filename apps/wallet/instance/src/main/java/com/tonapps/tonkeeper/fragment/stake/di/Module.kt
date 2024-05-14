package com.tonapps.tonkeeper.fragment.stake.di

import com.tonapps.tonkeeper.fragment.stake.data.mapper.StakingServiceMapper
import com.tonapps.tonkeeper.fragment.stake.domain.StakingRepository
import org.koin.dsl.module

val stakingModule = module {
    single { StakingServiceMapper() }
    single { StakingRepository(get(), get()) }
}