package com.tonapps.tonkeeper.fragment.swap.di

import com.tonapps.tonkeeper.api.jetton.JettonRepository
import com.tonapps.tonkeeper.fragment.swap.data.DexAssetRatesLocalStorage
import com.tonapps.tonkeeper.fragment.swap.domain.CreateStonfiSwapMessageCase
import com.tonapps.tonkeeper.fragment.swap.domain.CreateSwapCellCase
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.domain.GetDefaultSwapSettingsCase
import com.tonapps.tonkeeper.fragment.swap.domain.JettonWalletAddressRepository
import com.tonapps.tonkeeper.fragment.swap.pick_asset.rv.TokenListHelper
import com.tonapps.tonkeeper.fragment.swap.pick_asset.rv.TokenListItemMapper
import org.koin.dsl.module

val swapModule = module {
    single { JettonRepository() }
    single { DexAssetRatesLocalStorage(get()) }
    single { DexAssetsRepository(get(), get(), get(), get(), get()) }
    single { GetDefaultSwapSettingsCase() }
    single { CreateSwapCellCase() }
    single { CreateStonfiSwapMessageCase(get(), get(), get(), get(), get()) }
    factory { TokenListHelper(get()) }
    single { TokenListItemMapper() }
    single { JettonWalletAddressRepository(get()) }
}