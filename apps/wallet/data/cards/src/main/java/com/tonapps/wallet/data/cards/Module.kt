package com.tonapps.wallet.data.cards

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val cardsModule = module {
    singleOf(::CardsRepository)
}
