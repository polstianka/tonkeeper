package com.tonapps.tonkeeper.fragment.swap.di

import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import org.koin.dsl.module

val swapModule = module {
    single { DexAssetsRepository(get()) }
}