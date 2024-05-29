package com.tonapps.wallet.api.core

import io.stonfi.apis.DexApi
import okhttp3.OkHttpClient

class BaseStonFiAPI(
    basePath: String,
    okHttpClient: OkHttpClient
) {
    val dex: DexApi by lazy { DexApi(basePath, okHttpClient) }
}