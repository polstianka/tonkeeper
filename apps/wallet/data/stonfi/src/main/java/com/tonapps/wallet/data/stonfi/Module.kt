package com.tonapps.wallet.data.stonfi

import org.koin.dsl.module

val stonfiModule = module {
    single { StonfiRepository(get()) }
}