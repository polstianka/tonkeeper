package com.tonapps.wallet.data.token

import org.koin.dsl.module

val tokenModule = module {
    single { RawStakesRepository(get(), get()) }
    single { RawTokensRepository(get(), get(), get()) }
    single { TokenRepository(get(), get()) }
    single { TokenRepositoryV2(get(), get(), get(), get()) }
}