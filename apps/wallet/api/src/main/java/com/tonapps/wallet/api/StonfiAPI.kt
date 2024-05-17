package com.tonapps.wallet.api

import android.content.Context
import kotlinx.coroutines.CoroutineScope

class StonefiAPI(
    private val context: Context,
    private val scope: CoroutineScope
) {

    val defaultHttpClient = API.baseOkHttpClientBuilder().build()

    private val provider = StonfiProvider()
}