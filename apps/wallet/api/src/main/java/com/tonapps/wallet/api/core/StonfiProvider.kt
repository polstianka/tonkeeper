package com.tonapps.wallet.api.core

import io.stonfiapi.apis.DEXScreenerAdapterApi
import io.stonfiapi.apis.DexApi
import io.stonfiapi.apis.ExportApi
import io.stonfiapi.apis.JettonApi
import io.stonfiapi.apis.StatsApi
import io.stonfiapi.apis.WalletsApi
import okhttp3.OkHttpClient

class BaseStonfiApi(
    basePath: String,
    okHttpClient: OkHttpClient
) {

    val jetton: JettonApi by lazy { JettonApi(basePath, okHttpClient) }
    val dex: DexApi by lazy { DexApi(basePath, okHttpClient) }
    val dexScreenerAdapter: DEXScreenerAdapterApi by lazy {
        DEXScreenerAdapterApi(
            basePath,
            okHttpClient
        )
    }
    val export: ExportApi by lazy { ExportApi(basePath, okHttpClient) }
    val stats: StatsApi by lazy { StatsApi(basePath, okHttpClient) }
    val wallets: WalletsApi by lazy { WalletsApi(basePath, okHttpClient) }
}