package com.tonapps.wallet.data.token

import org.koin.dsl.module

val assetModule = module {
    single { AssetRepository(get(), get()) }
}