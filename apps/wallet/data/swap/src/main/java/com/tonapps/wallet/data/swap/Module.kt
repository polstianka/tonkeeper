package com.tonapps.wallet.data.swap

import org.koin.dsl.module

val swapModule = module {
    single { SwapRepository(get(), get()) }
    single { WalletAssetsRepository(get(), get(), get()) }
}