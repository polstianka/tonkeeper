package com.tonapps.wallet.data.jetton

import org.koin.dsl.module

val jettonModule = module {
    single { JettonRepository(get()) }
}