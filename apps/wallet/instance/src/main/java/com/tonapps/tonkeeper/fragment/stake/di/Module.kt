package com.tonapps.tonkeeper.fragment.stake.di

import com.tonapps.tonkeeper.fragment.stake.confirm.ConfirmStakeListHelper
import com.tonapps.tonkeeper.fragment.stake.confirm.ConfirmStakeListItemMapper
import com.tonapps.tonkeeper.fragment.stake.data.mapper.StakingServiceMapper
import com.tonapps.tonkeeper.fragment.stake.domain.GetStakeWalletTransferCase
import com.tonapps.tonkeeper.fragment.stake.domain.GetStateInitCase
import com.tonapps.tonkeeper.fragment.stake.domain.StakeCase
import com.tonapps.tonkeeper.fragment.stake.domain.StakingRepository
import org.koin.dsl.module

val stakingModule = module {
    single { StakingServiceMapper() }
    single { StakingRepository(get(), get()) }
    single { ConfirmStakeListItemMapper() }
    factory { ConfirmStakeListHelper(get()) }
    single { GetStateInitCase() }
    single { StakeCase(get(), get(), get()) }
    single { GetStateInitCase() }
    single { GetStakeWalletTransferCase(get(), get()) }
}