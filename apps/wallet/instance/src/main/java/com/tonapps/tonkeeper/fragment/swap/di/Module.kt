package com.tonapps.tonkeeper.fragment.swap.di

import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.domain.GetDefaultSwapSettingsCase
import org.koin.dsl.module

val swapModule = module {
    single { DexAssetsRepository(get()) }
    single { GetDefaultSwapSettingsCase() }
}