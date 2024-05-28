package com.tonapps.wallet.api

import com.tonapps.wallet.api.core.BaseAPI
import com.tonapps.wallet.api.core.SourceAPI
import okhttp3.OkHttpClient

internal class Provider(
    stonfiHost: String,
    mainnetHost: String,
    testnetHost: String,
    okHttpClient: OkHttpClient,
) {

    private val main = BaseAPI(mainnetHost, okHttpClient)
    private val test = BaseAPI(testnetHost, okHttpClient)
    private val stonfi = BaseAPI(stonfiHost, okHttpClient)

    val swap = SourceAPI(stonfi.swap, stonfi.swap)

    val accounts = SourceAPI(main.accounts, test.accounts)

    val blockchain = SourceAPI(main.blockchain, test.blockchain)

    val connect = SourceAPI(main.connect, test.connect)

    val dns = SourceAPI(main.dns, test.dns)

    val emulation = SourceAPI(main.emulation, test.emulation)

    val events = SourceAPI(main.events, test.events)

    val jettons = SourceAPI(main.jettons, test.jettons)

    val liteServer = SourceAPI(main.liteServer, test.liteServer)

    val nft = SourceAPI(main.nft, test.nft)

    val rates = SourceAPI(main.rates, test.rates)

    val staking = SourceAPI(main.staking, test.staking)

    val storage = SourceAPI(main.storage, test.storage)

    val traces = SourceAPI(main.traces, test.traces)

    val wallet = SourceAPI(main.wallet, test.wallet)
}