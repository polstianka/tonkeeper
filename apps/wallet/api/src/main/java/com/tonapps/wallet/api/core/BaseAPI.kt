package com.tonapps.wallet.api.core

import io.tonapi.apis.AccountsApi
import io.tonapi.apis.BlockchainApi
import io.tonapi.apis.ConnectApi
import io.tonapi.apis.DNSApi
import io.tonapi.apis.EmulationApi
import io.tonapi.apis.EventsApi
import io.tonapi.apis.JettonsApi
import io.tonapi.apis.LiteServerApi
import io.tonapi.apis.NFTApi
import io.tonapi.apis.OperatorRatesApi
import io.tonapi.apis.RatesApi
import io.tonapi.apis.StakingApi
import io.tonapi.apis.StorageApi
import io.tonapi.apis.SwapApi
import io.tonapi.apis.TracesApi
import io.tonapi.apis.WalletApi
import okhttp3.OkHttpClient

class BaseAPI(
    basePath: String,
    okHttpClient: OkHttpClient
) {

    val swap: SwapApi by lazy { SwapApi(basePath, okHttpClient) }

    val operatorRate: OperatorRatesApi by lazy { OperatorRatesApi(basePath, okHttpClient) }

    val accounts: AccountsApi by lazy { AccountsApi(basePath, okHttpClient) }

    val blockchain: BlockchainApi by lazy { BlockchainApi(basePath, okHttpClient) }

    val connect: ConnectApi by lazy { ConnectApi(basePath, okHttpClient) }

    val dns: DNSApi by lazy { DNSApi(basePath, okHttpClient) }

    val emulation: EmulationApi by lazy { EmulationApi(basePath, okHttpClient) }

    val events: EventsApi by lazy { EventsApi(basePath, okHttpClient) }

    val jettons: JettonsApi by lazy { JettonsApi(basePath, okHttpClient) }

    val liteServer: LiteServerApi by lazy { LiteServerApi(basePath, okHttpClient) }

    val nft: NFTApi by lazy { NFTApi(basePath, okHttpClient) }

    val rates: RatesApi by lazy { RatesApi(basePath, okHttpClient) }

    val staking: StakingApi by lazy { StakingApi(basePath, okHttpClient) }

    val storage: StorageApi by lazy { StorageApi(basePath, okHttpClient) }

    val traces: TracesApi by lazy { TracesApi(basePath, okHttpClient) }

    val wallet: WalletApi by lazy { WalletApi(basePath, okHttpClient) }

}