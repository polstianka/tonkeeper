package com.tonapps.wallet.api

import android.content.Context
import android.util.ArrayMap
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.tonapps.blockchain.ton.extensions.EmptyPrivateKeyEd25519
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.blockchain.ton.extensions.isValidTonAddress
import com.tonapps.extensions.locale
import com.tonapps.extensions.unicodeToPunycode
import com.tonapps.icu.Coins
import com.tonapps.network.SSEvent
import com.tonapps.network.SSLSocketFactoryTcpNoDelay
import com.tonapps.network.get
import com.tonapps.network.interceptor.AcceptLanguageInterceptor
import com.tonapps.network.interceptor.AuthorizationInterceptor
import com.tonapps.network.post
import com.tonapps.network.postJSON
import com.tonapps.network.sse
import com.tonapps.wallet.api.core.SourceAPI
import com.tonapps.wallet.api.entity.AccountDetailsEntity
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.api.entity.ChartEntity
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.api.internal.ConfigRepository
import com.tonapps.wallet.api.internal.InternalApi
import io.batteryapi.apis.BatteryApi
import io.batteryapi.apis.BatteryApi.UnitsGetBalance
import io.batteryapi.models.Balance
import io.batteryapi.models.Config
import io.batteryapi.models.RechargeMethods
import io.tonapi.infrastructure.Serializer
import io.tonapi.models.Account
import io.tonapi.models.AccountAddress
import io.tonapi.models.AccountEvent
import io.tonapi.models.AccountEvents
import io.tonapi.models.AccountStatus
import io.tonapi.models.EmulateMessageToWalletRequest
import io.tonapi.models.EmulateMessageToWalletRequestParamsInner
import io.tonapi.models.MessageConsequences
import io.tonapi.models.NftItem
import io.tonapi.models.SendBlockchainMessageRequest
import io.tonapi.models.TokenRates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell
import org.ton.crypto.hex
import java.util.Locale
import java.util.concurrent.TimeUnit

class API(
    private val context: Context,
    private val scope: CoroutineScope
) {

    val defaultHttpClient = baseOkHttpClientBuilder().build()

    private val tonAPIHttpClient: OkHttpClient by lazy {
        createTonAPIHttpClient(
            context = context,
            tonApiV2Key = config.tonApiV2Key,
            allowDomains = listOf(config.tonapiMainnetHost, config.tonapiTestnetHost)
        )
    }

    private val batteryHttpClient: OkHttpClient by lazy {
        createBatteryAPIHttpClient(context)
    }

    private val internalApi = InternalApi(context, defaultHttpClient)
    private val configRepository = ConfigRepository(context, scope, internalApi)

    @Volatile
    private var cachedCountry: String? = null

    val config: ConfigEntity
        get() {
            while (configRepository.configEntity == null) {
                Thread.sleep(16)
            }
            return configRepository.configEntity!!
        }

    val configFlow: Flow<ConfigEntity>
        get() = configRepository.stream

    private val provider: Provider by lazy {
        Provider(config.tonapiMainnetHost, config.tonapiTestnetHost, tonAPIHttpClient)
    }

    private val batteryApi by lazy {
        SourceAPI(BatteryApi(config.batteryHost, batteryHttpClient), BatteryApi(config.batteryTestnetHost, batteryHttpClient))
    }

    private val emulationJSONAdapter: JsonAdapter<MessageConsequences> by lazy {
        Serializer.moshi.adapter(MessageConsequences::class.java)
    }

    fun accounts(testnet: Boolean) = provider.accounts.get(testnet)

    fun jettons(testnet: Boolean) = provider.jettons.get(testnet)

    fun wallet(testnet: Boolean) = provider.wallet.get(testnet)

    fun nft(testnet: Boolean) = provider.nft.get(testnet)

    fun blockchain(testnet: Boolean) = provider.blockchain.get(testnet)

    fun emulation(testnet: Boolean) = provider.emulation.get(testnet)

    fun liteServer(testnet: Boolean) = provider.liteServer.get(testnet)

    fun staking(testnet: Boolean) = provider.staking.get(testnet)

    fun events(testnet: Boolean) = provider.events.get(testnet)

    fun rates() = provider.rates.get(false)

    fun battery(testnet: Boolean) = batteryApi.get(testnet)

    suspend fun getBatteryConfig(testnet: Boolean): Config? {
        return withRetry { battery(testnet).getConfig() }
    }

    suspend fun getBatteryRechargeMethods(testnet: Boolean): RechargeMethods? {
        return withRetry { battery(testnet).getRechargeMethods(false) }
    }

    suspend fun getBatteryBalance(
        tonProofToken: String,
        testnet: Boolean,
        units: UnitsGetBalance = UnitsGetBalance.ton
    ): Balance? {
        return withRetry { battery(testnet).getBalance(tonProofToken, units) }
    }

    suspend fun getAlertNotifications() = withRetry {
        internalApi.getNotifications()
    } ?: emptyList()

    suspend fun isOkStatus(testnet: Boolean): Boolean {
        try {
            val status = withRetry {
                provider.blockchain.get(testnet).status()
            } ?: return false
            if (!status.restOnline) {
                return false
            }
            if (status.indexingLatency > (5 * 60) - 30) {
                return false
            }
            return true
        } catch (e: Throwable) {
            return false
        }
    }

    suspend fun getEvents(
        accountId: String,
        testnet: Boolean,
        beforeLt: Long? = null,
        limit: Int = 20
    ): AccountEvents? = withRetry {
        accounts(testnet).getAccountEvents(
            accountId = accountId,
            limit = limit,
            beforeLt = beforeLt,
            subjectOnly = true
        )
    }

    suspend fun getSingleEvent(
        eventId: String,
        testnet: Boolean
    ): List<AccountEvent>? {
        val event = withRetry { events(testnet).getEvent(eventId) } ?: return null
        val accountEvent = AccountEvent(
            eventId = eventId,
            account = AccountAddress(
                address = "",
                isScam = false,
                isWallet = false,
            ),
            timestamp = event.timestamp,
            actions = event.actions,
            isScam = event.isScam,
            lt = event.lt,
            inProgress = event.inProgress,
            extra = 0L,
        )
        return listOf(accountEvent)
    }

    fun getTokenEvents(
        tokenAddress: String,
        accountId: String,
        testnet: Boolean,
        beforeLt: Long? = null,
        limit: Int = 20
    ): AccountEvents {
        return accounts(testnet).getAccountJettonHistoryByID(
            jettonId = tokenAddress,
            accountId = accountId,
            limit = limit,
            beforeLt = beforeLt
        )
    }

    suspend fun getTonBalance(
        accountId: String,
        testnet: Boolean
    ): BalanceEntity? {
        val account = getAccount(accountId, testnet) ?: return null
        val initializedAccount = account.status != AccountStatus.uninit && account.status != AccountStatus.nonexist
        return BalanceEntity(
            token = TokenEntity.TON,
            value = Coins.of(account.balance),
            walletAddress = accountId,
            initializedAccount = initializedAccount
        )
    }

    suspend fun getJetton(
        accountId: String,
        testnet: Boolean
    ): TokenEntity? {
        val jettonsAPI = jettons(testnet)
        val jetton = withRetry {
            jettonsAPI.getJettonInfo(accountId)
        } ?: return null
        return TokenEntity(jetton)
    }

    suspend fun getJettonsBalances(
        accountId: String,
        testnet: Boolean,
        currency: String? = null
    ): List<BalanceEntity>? {
        val jettonsBalances = withRetry {
            accounts(testnet).getAccountJettonsBalances(
                accountId = accountId,
                currencies = currency?.let { listOf(it) }
            ).balances
        } ?: return null
        return jettonsBalances.map { BalanceEntity(it) }.filter { it.value.isPositive }
    }

    suspend fun resolveAddressOrName(
        query: String,
        testnet: Boolean
    ): AccountDetailsEntity? {
        return try {
            val account = getAccount(query, testnet) ?: return null
            AccountDetailsEntity(query, account, testnet)
        } catch (e: Throwable) {
            null
        }
    }

    suspend fun resolvePublicKey(
        pk: PublicKeyEd25519,
        testnet: Boolean
    ): List<AccountDetailsEntity> {
        return try {
            val query = pk.key.hex()
            val wallets = withRetry {
                wallet(testnet).getWalletsByPublicKey(query).accounts
            } ?: return emptyList()
            wallets.map { AccountDetailsEntity(query, it, testnet) }
        } catch (e: Throwable) {
            emptyList()
        }
    }

    suspend fun getRates(currency: String, tokens: List<String>): Map<String, TokenRates>? {
        val currencies = listOf(currency)
        return withRetry {
            rates().getRates(
                tokens = tokens,
                currencies = currencies
            ).rates
        }
    }

    suspend fun getNft(address: String, testnet: Boolean): NftItem? {
        return withRetry { nft(testnet).getNftItemByAddress(address) }
    }

    suspend fun getNftItems(
        address: String,
        testnet: Boolean,
        limit: Int = 1000
    ): List<NftItem>? {
        return withRetry {
            accounts(testnet).getAccountNftItems(
                accountId = address,
                limit = limit,
                indirectOwnership = true,
            ).nftItems
        }
    }

    private suspend fun getPublicKey(
        accountId: String,
        testnet: Boolean
    ): PublicKeyEd25519? {
        val hex = withRetry {
            accounts(testnet).getAccountPublicKey(accountId)
        }?.publicKey ?: return null
        return PublicKeyEd25519(hex(hex))
    }

    suspend fun safeGetPublicKey(
        accountId: String,
        testnet: Boolean
    ) = getPublicKey(accountId, testnet) ?: EmptyPrivateKeyEd25519.publicKey()

    fun accountEvents(accountId: String, testnet: Boolean): Flow<SSEvent> {
        val endpoint = if (testnet) {
            config.tonapiTestnetHost
        } else {
            config.tonapiMainnetHost
        }
        // val mempool = okHttpClient.sse("$endpoint/v2/sse/mempool?accounts=${accountId}")
        val tx = tonAPIHttpClient.sse("$endpoint/v2/sse/accounts/transactions?accounts=${accountId}")
        // return merge(mempool, tx)
        return tx
    }

    fun tonconnectEvents(
        publicKeys: List<String>,
        lastEventId: String?
    ): Flow<SSEvent> {
        if (publicKeys.isEmpty()) {
            return emptyFlow()
        }
        val value = publicKeys.joinToString(",")
        var url = "${BRIDGE_URL}/events?client_id=$value"
        if (lastEventId != null) {
            url += "&last_event_id=$lastEventId"
        }
        return tonAPIHttpClient.sse(url)
    }

    suspend fun tonconnectPayload(): String? {
        try {
            val url = "${config.tonapiMainnetHost}/v2/tonconnect/payload"
            val json = withRetry {
                JSONObject(tonAPIHttpClient.get(url))
            } ?: return null
            return json.getString("payload")
        } catch (e: Throwable) {
            return null
        }
    }

    suspend fun tonconnectProof(address: String, proof: String): String {
        val url = "${config.tonapiMainnetHost}/v2/wallet/auth/proof"
        val data = "{\"address\":\"$address\",\"proof\":$proof}"
        val response = withRetry {
            tonAPIHttpClient.postJSON(url, data)
        } ?: throw Exception("Empty response")
        if (!response.isSuccessful) {
            throw Exception("Failed creating proof: ${response.code}")
        }
        val body = response.body?.string() ?: throw Exception("Empty response")
        return JSONObject(body).getString("token")
    }

    fun tonconnectSend(
        publicKeyHex: String,
        clientId: String,
        body: String
    ) {
        val mimeType = "text/plain".toMediaType()
        val url = "${BRIDGE_URL}/message?client_id=$publicKeyHex&to=$clientId&ttl=300"
        val response = tonAPIHttpClient.post(url, body.toRequestBody(mimeType))
        if (!response.isSuccessful) {
            throw Exception("Failed sending event: ${response.code}")
        }
    }

    suspend fun emulateWithBattery(
        tonProofToken: String,
        cell: Cell,
        testnet: Boolean
    ) = emulateWithBattery(tonProofToken, cell.base64(), testnet)

    suspend fun emulateWithBattery(
        tonProofToken: String,
        boc: String,
        testnet: Boolean
    ): Pair<MessageConsequences, Boolean>? {
        val host = if (testnet) config.batteryTestnetHost else config.batteryHost
        val url = "$host/wallet/emulate"
        val data = "{\"boc\":\"$boc\"}"

        val response = withRetry {
            tonAPIHttpClient.postJSON(url, data, ArrayMap<String, String>().apply {
                set("X-TonConnect-Auth", tonProofToken)
            })
        } ?: return null

        val supportedByBattery = response.headers["supported-by-battery"] == "true"
        val allowedByBattery = response.headers["allowed-by-battery"] == "true"
        val withBattery = supportedByBattery && allowedByBattery

        val string = response.body?.string() ?: return null
        val consequences = emulationJSONAdapter.fromJson(string) ?: return null
        return Pair(consequences, withBattery)
    }

    suspend fun emulate(
        boc: String,
        testnet: Boolean,
        address: String? = null,
        balance: Long? = null
    ): MessageConsequences? = withContext(Dispatchers.IO) {
        val params = mutableListOf<EmulateMessageToWalletRequestParamsInner>()
        if (address != null) {
            params.add(EmulateMessageToWalletRequestParamsInner(address, balance))
        }
        val request = EmulateMessageToWalletRequest(boc, params)
        withRetry {
            emulation(testnet).emulateMessageToWallet(request)
        }
    }

    suspend fun emulate(
        cell: Cell,
        testnet: Boolean,
        address: String? = null,
        balance: Long? = null
    ): MessageConsequences? {
        return emulate(cell.base64(), testnet, address, balance)
    }

    suspend fun sendToBlockchainWithBattery(
        boc: Cell,
        tonProofToken: String,
        testnet: Boolean,
    ) = sendToBlockchainWithBattery(boc.base64(), tonProofToken, testnet)

    suspend fun sendToBlockchainWithBattery(
        boc: String,
        tonProofToken: String,
        testnet: Boolean,
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isOkStatus(testnet)) {
            return@withContext false
        }

        val request = io.batteryapi.models.EmulateMessageToWalletRequest(boc)

        withRetry {
            battery(testnet).sendMessage(tonProofToken, request)
            true
        } ?: false
    }

    suspend fun sendToBlockchain(
        boc: String,
        testnet: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isOkStatus(testnet)) {
            return@withContext false
        }

        val request = SendBlockchainMessageRequest(boc)
        withRetry {
            blockchain(testnet).sendBlockchainMessage(request)
            true
        } ?: false
    }

    suspend fun sendToBlockchain(
        cell: Cell,
        testnet: Boolean
    ) = sendToBlockchain(cell.base64(), testnet)

    suspend fun getAccountSeqno(
        accountId: String,
        testnet: Boolean,
    ): Int = withContext(Dispatchers.IO) {
        wallet(testnet).getAccountSeqno(accountId).seqno
    }

    suspend fun resolveAccount(
        value: String,
        testnet: Boolean,
    ): Account? = withContext(Dispatchers.IO) {
        if (value.isValidTonAddress()) {
            return@withContext getAccount(value, testnet)
        }
        return@withContext resolveDomain(value.lowercase().trim(), testnet)
    }

    private suspend fun resolveDomain(domain: String, testnet: Boolean): Account? {
        return getAccount(domain, testnet) ?: getAccount(domain.unicodeToPunycode(), testnet)
    }

    private suspend fun getAccount(accountId: String, testnet: Boolean): Account? {
        return withRetry {
            accounts(testnet).getAccount(accountId)
        }
    }

    suspend fun pushSubscribe(
        locale: Locale,
        firebaseToken: String,
        deviceId: String,
        accounts: List<String>
    ): Boolean {
        val url = "${config.tonapiMainnetHost}/v1/internal/pushes/plain/subscribe"

        val accountsArray = JSONArray()
        for (account in accounts) {
            val jsonAccount = JSONObject()
            jsonAccount.put("address", account)
            accountsArray.put(jsonAccount)
        }

        val json = JSONObject()
        json.put("locale", locale.toString())
        json.put("device", deviceId)
        json.put("token", firebaseToken)
        json.put("accounts", accountsArray)

        return withRetry {
            val response = tonAPIHttpClient.postJSON(url, json.toString())
            response.isSuccessful
        } ?: false
    }

    suspend fun pushUnsubscribe(
        deviceId: String,
        account: String
    ): Boolean {
        val url = "${config.tonapiMainnetHost}/v1/internal/pushes/plain/unsubscribe"

        val jsonAccount = JSONObject()
        jsonAccount.put("address", account)

        val json = JSONObject()
        json.put("device", deviceId)
        json.put("accounts", JSONArray().apply {
            put(jsonAccount)
        })

        return withRetry {
            val response = tonAPIHttpClient.postJSON(url, json.toString())
            response.isSuccessful
        } ?: false
    }

    suspend fun pushTonconnectSubscribe(
        token: String,
        appUrl: String,
        accountId: String,
        firebaseToken: String,
        sessionId: String?,
        commercial: Boolean = true,
        silent: Boolean = true
    ): Boolean {
        val url = "${config.tonapiMainnetHost}/v1/internal/pushes/tonconnect"

        val json = JSONObject()
        json.put("app_url", appUrl)
        json.put("account", accountId)
        json.put("firebase_token", firebaseToken)
        sessionId?.let { json.put("session_id", it) }
        json.put("commercial", commercial)
        json.put("silent", silent)
        val data = json.toString().replace("\\/", "/").trim()

        return withRetry {
            tonAPIHttpClient.postJSON(url, data, ArrayMap<String, String>().apply {
                set("X-TonConnect-Auth", token)
            }).isSuccessful
        } ?: false
    }

    fun pushTonconnectUnsubscribe(
        token: String,
        appUrl: String,
        accountId: String,
        firebaseToken: String,
    ): Boolean {
        return try {
            val url = "${config.tonapiMainnetHost}/v1/internal/pushes/tonconnect"

            val json = JSONObject()
            json.put("app_url", appUrl)
            json.put("account", accountId)
            json.put("firebase_token", firebaseToken)
            val data = json.toString().replace("\\/", "/")

            tonAPIHttpClient.postJSON(url, data, ArrayMap<String, String>().apply {
                set("X-TonConnect-Auth", token)
            }).isSuccessful
        } catch (e: Throwable) {
            false
        }
    }

    fun getPushFromApps(
        token: String,
        accountId: String,
    ): JSONArray {
        return try {
            val url = "${config.tonapiMainnetHost}/v1/messages/history?account=$accountId"
            val response = tonAPIHttpClient.get(url, ArrayMap<String, String>().apply {
                set("X-TonConnect-Auth", token)
            })
            val json = JSONObject(response)
            json.getJSONArray("items")
        } catch (e: Throwable) {
            JSONArray()
        }
    }

    fun getBrowserApps(testnet: Boolean, locale: Locale): JSONObject {
        return internalApi.getBrowserApps(testnet, locale)
    }

    fun getFiatMethods(testnet: Boolean, locale: Locale): JSONObject {
        return internalApi.getFiatMethods(testnet, locale)
    }

    fun getTransactionEvents(accountId: String, testnet: Boolean, eventId: String): AccountEvent? {
        return try {
            accounts(testnet).getAccountEvent(accountId, eventId)
        } catch (e: Throwable) {
            null
        }
    }

    fun loadChart(
        token: String,
        currency: String,
        startDate: Long,
        endDate: Long
    ): List<ChartEntity> {
        try {
            val url = "${config.tonapiMainnetHost}/v2/rates/chart?token=$token&currency=$currency&end_date=$endDate&start_date=$startDate"
            val array = JSONObject(tonAPIHttpClient.get(url)).getJSONArray("points")
            return (0 until array.length()).map { index ->
                ChartEntity(array.getJSONArray(index))
            }
        } catch (e: Throwable) {
            return listOf(ChartEntity(0, 0f))
        }
    }

    suspend fun getServerTime(testnet: Boolean): Int = withContext(Dispatchers.IO) {
        try {
            liteServer(testnet).getRawTime().time
        } catch (e: Throwable) {
            (System.currentTimeMillis() / 1000).toInt()
        }
    }

    suspend fun resolveCountry(): String? = withContext(Dispatchers.IO) {
        if (cachedCountry == null) {
            cachedCountry = internalApi.resolveCountry()
        }
        cachedCountry
    }

    suspend fun reportNtfSpam(
        nftAddress: String,
        scam: Boolean
    ) = withContext(Dispatchers.IO) {
        val url = config.scamEndpoint + "/v1/report/$nftAddress"
        val data = "{\"is_scam\":$scam}"
        val response = tonAPIHttpClient.postJSON(url, data)
        if (!response.isSuccessful) {
            throw Exception("Failed creating proof: ${response.code}")
        }
        response.body?.string() ?: throw Exception("Empty response")
    }

    companion object {

        const val BRIDGE_URL = "https://bridge.tonapi.io/bridge"

        val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        private val socketFactoryTcpNoDelay = SSLSocketFactoryTcpNoDelay()

        private fun baseOkHttpClientBuilder(): OkHttpClient.Builder {
            return OkHttpClient().newBuilder()
                .retryOnConnectionFailure(false)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .pingInterval(5, TimeUnit.SECONDS)
                .followSslRedirects(true)
                .followRedirects(true)
                // .sslSocketFactory(socketFactoryTcpNoDelay.sslSocketFactory, socketFactoryTcpNoDelay.trustManager)
                // .socketFactory(SocketFactoryTcpNoDelay())
        }

        private fun createTonAPIHttpClient(
            context: Context,
            tonApiV2Key: String,
            allowDomains: List<String>
        ): OkHttpClient {
            return baseOkHttpClientBuilder()
                .addInterceptor(AcceptLanguageInterceptor(context.locale))
                .addInterceptor(AuthorizationInterceptor.bearer(tonApiV2Key, allowDomains))
                .build()
        }

        private fun createBatteryAPIHttpClient(
            context: Context,
        ): OkHttpClient {
            return baseOkHttpClientBuilder()
                 .addInterceptor(AcceptLanguageInterceptor(context.locale))
                .build()
        }
    }
}