package com.tonapps.wallet.data.token

import org.koin.dsl.module

val swapModule = module {
    single { SwapRepository(get(), get()) }
}