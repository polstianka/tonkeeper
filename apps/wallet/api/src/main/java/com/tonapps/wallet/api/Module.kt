package com.tonapps.wallet.api

import org.koin.dsl.module

val apiModule = module {
    single(createdAtStart = true) { API(get(), get()) }
    single(createdAtStart = true) { StonfiAPI(get(), get()) }
}