package com.tonapps.wallet.api

import android.content.Context
import android.util.ArrayMap
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.tonapps.blockchain.ton.contract.BaseWalletContract
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.blockchain.ton.extensions.EmptyPrivateKeyEd25519
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.blockchain.ton.extensions.hex
import com.tonapps.blockchain.ton.extensions.isValidTonAddress
import com.tonapps.blockchain.ton.extensions.toRawAddress
import com.tonapps.extensions.toUriOrNull
import com.tonapps.icu.Coins
import com.tonapps.network.SSEvent
import com.tonapps.network.execute
import com.tonapps.network.get
import com.tonapps.network.post
import com.tonapps.network.postJSON
import com.tonapps.network.requestBuilder
import com.tonapps.network.sse
import com.tonapps.wallet.api.core.SourceAPI
import com.tonapps.wallet.api.entity.AccountDetailsEntity
import com.tonapps.wallet.api.entity.AccountEventEntity
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.api.entity.ChartEntity
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.api.holders.HoldersApi
import com.tonapps.wallet.api.internal.ConfigRepository
import com.tonapps.wallet.api.internal.InternalApi
import io.batteryapi.apis.BatteryApi
import io.batteryapi.apis.BatteryApi.UnitsGetBalance
import io.batteryapi.models.Balance
import io.batteryapi.models.Config
import io.batteryapi.models.RechargeMethods
import io.tonapi.infrastructure.ClientException
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell
import org.ton.crypto.hex
import java.util.Locale

class API(
    private val context: Context,
    private val scope: CoroutineScope
): CoreAPI(context) {

    private val internalApi = InternalApi(context, defaultHttpClient, appVersionName)
    private val configRepository = ConfigRepository(context, scope, internalApi)


    val config: ConfigEntity
        get() = configRepository.configEntity

    val configTestnet: ConfigEntity
        get() = configRepository.configTestnetEntity

    val configFlow: Flow<ConfigEntity>
        get() = configRepository.stream

    private val tonAPIHttpClient: OkHttpClient by lazy {
        tonAPIHttpClient { config }
    }

    val holdersApi = HoldersApi(defaultHttpClient, ::getConfig)

    @Volatile
    private var cachedCountry: String? = null

    fun getConfig(testnet: Boolean): ConfigEntity {
        return if (testnet) configTestnet else config
    }

    suspend fun tonapiFetch(
        url: String,
        options: String
    ): Response = withContext(Dispatchers.IO) {
        val uri = url.toUriOrNull() ?: throw Exception("Invalid URL")
        if (uri.scheme != "https") {
            throw Exception("Invalid scheme. Should be https")
        }
        val host = uri.host ?: throw Exception("Invalid URL")
        if (host != "tonapi.io" && host.endsWith(".tonapi.io")) {
            throw Exception("Invalid host. Should be tonapi.io")
        }

        val builder = Request.Builder().url(url)

        val parsedOptions = JSONObject(options)
        val methodOptions = parsedOptions.optString("method") ?: "GET"
        val headersOptions = parsedOptions.optJSONObject("headers") ?: JSONObject()
        val bodyOptions = parsedOptions.optString("body") ?: ""
        var contentTypeOptions = "application/json"

        for (key in headersOptions.keys()) {
            val value = headersOptions.getString(key)
            if (key.equals("Authorization")) {
                builder.addHeader("X-Authorization", value)
            } else if (key.equals("Content-Type")) {
                contentTypeOptions = value
            } else {
                builder.addHeader(key, value)
            }
        }
        builder.addHeader("Authorization", "Bearer ${config.tonApiV2Key}")

        if (methodOptions.equals("POST", ignoreCase = true)) {
            builder.post(bodyOptions.toRequestBody(contentTypeOptions.toMediaType()))
        }

        tonAPIHttpClient.newCall(builder.build()).execute()
    }

    private val provider: Provider by lazy {
        Provider(config.tonapiMainnetHost, config.tonapiTestnetHost, tonAPIHttpClient)
    }

    private val batteryApi by lazy {
        SourceAPI(BatteryApi(config.batteryHost, tonAPIHttpClient), BatteryApi(config.batteryTestnetHost, tonAPIHttpClient))
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

    fun getBatteryConfig(testnet: Boolean): Config? {
        return withRetry { battery(testnet).getConfig() }
    }

    fun getBatteryRechargeMethods(testnet: Boolean): RechargeMethods? {
        return withRetry { battery(testnet).getRechargeMethods(false) }
    }

    fun getBatteryBalance(
        tonProofToken: String,
        testnet: Boolean,
        units: UnitsGetBalance = UnitsGetBalance.ton
    ): Balance? {
        return withRetry { battery(testnet).getBalance(tonProofToken, units) }
    }

    fun getAlertNotifications() = withRetry {
        internalApi.getNotifications()
    } ?: emptyList()

    private fun isOkStatus(testnet: Boolean): Boolean {
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

    fun realtime(
        accountId: String,
        testnet: Boolean,
        config: ConfigEntity,
        onFailure: ((Throwable) -> Unit)?
    ): Flow<SSEvent> {
        val endpoint = if (testnet) config.tonapiSSETestnetEndpoint else config.tonapiSSEEndpoint
        val url = "$endpoint/sse/traces?account=$accountId&token=${config.tonApiV2Key}"
        return seeHttpClient.sse(url, onFailure = onFailure)
    }

    fun get(url: String): String {
        val headers = ArrayMap<String, String>().apply {
            set("Connection", "close")
        }
        return defaultHttpClient.get(url, headers)
    }

    fun getBurnAddress() = config.burnZeroDomain.ifBlank {
        "UQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAJKZ"
    }

    fun getEvents(
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

    suspend fun getTransactionByHash(
        accountId: String,
        testnet: Boolean,
        hash: String,
        attempt: Int = 0
    ): AccountEventEntity? {
        try {
            val body = accounts(testnet).getAccountEvent(accountId, hash)
            return AccountEventEntity(accountId, testnet, hash, body)
        } catch (e: Throwable) {
            if (attempt >= 10 || e is CancellationException) {
                return null
            } else if (e is ClientException && e.statusCode == 404) {
                delay(2000)
            } else {
                delay(1000)
            }
            return getTransactionByHash(accountId, testnet, hash, attempt + 1)
        }
    }

    suspend fun getSingleEvent(
        eventId: String,
        testnet: Boolean
    ): List<AccountEvent>? = withContext(Dispatchers.IO) {
        val event = withRetry { events(testnet).getEvent(eventId) } ?: return@withContext null
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
        listOf(accountEvent)
    }

    fun getTokenEvents(
        tokenAddress: String,
        accountId: String,
        testnet: Boolean,
        beforeLt: Long? = null,
        limit: Int = 10
    ): AccountEvents {
        return accounts(testnet).getAccountJettonHistoryByID(
            jettonId = tokenAddress,
            accountId = accountId,
            limit = limit,
            beforeLt = beforeLt
        )
    }

    fun getTonBalance(
        accountId: String,
        testnet: Boolean,
        currency: String,
    ): BalanceEntity? {
        val account = getAccount(accountId, testnet, currency) ?: return null
        val initializedAccount = account.status != AccountStatus.uninit && account.status != AccountStatus.nonexist
        return BalanceEntity(
            token = TokenEntity.TON,
            value = Coins.of(account.balance),
            walletAddress = accountId,
            initializedAccount = initializedAccount,
            isRequestMinting = false,
            isTransferable = true,
            lastActivity = account.lastActivity
        )
    }

    fun getJetton(
        accountId: String,
        testnet: Boolean
    ): TokenEntity? {
        val jettonsAPI = jettons(testnet)
        val jetton = withRetry {
            jettonsAPI.getJettonInfo(accountId)
        } ?: return null
        return TokenEntity(jetton)
    }

    fun getJettonCustomPayload(
        accountId: String,
        testnet: Boolean,
        jettonId: String
    ): TokenEntity.TransferPayload? {
        val jettonsAPI = jettons(testnet)
        val payload = withRetry {
            jettonsAPI.getJettonTransferPayload(accountId, jettonId)
        } ?: return null
        return TokenEntity.TransferPayload(tokenAddress = jettonId, payload)
    }

    fun getJettonsBalances(
        accountId: String,
        testnet: Boolean,
        currency: String? = null,
        extensions: List<String>? = null
    ): List<BalanceEntity>? {
        val jettonsBalances = withRetry {
            accounts(testnet).getAccountJettonsBalances(
                accountId = accountId,
                currencies = currency?.let { listOf(it) },
                extensions = extensions,
            ).balances
        } ?: return null
        return jettonsBalances.map { BalanceEntity(it) }.filter { it.value.isPositive }
    }

    fun resolveAddressOrName(
        query: String,
        testnet: Boolean
    ): AccountDetailsEntity? {
        return try {
            val account = getAccount(query, testnet, null) ?: return null
            val details = AccountDetailsEntity(query, account, testnet)
            if (details.walletVersion != WalletVersion.UNKNOWN) {
                details
            } else {
                details.copy(
                    walletVersion = getWalletVersionByAddress(account.address, testnet)
                )
            }
        } catch (e: Throwable) {
            null
        }
    }

    private fun getWalletVersionByAddress(address: String, testnet: Boolean): WalletVersion {
        val pk = getPublicKey(address, testnet) ?: return WalletVersion.UNKNOWN
        return BaseWalletContract.resolveVersion(pk, address.toRawAddress(), testnet)
    }

    fun resolvePublicKey(
        pk: PublicKeyEd25519,
        testnet: Boolean
    ): List<AccountDetailsEntity> {
        return try {
            val query = pk.hex()
            val wallets = withRetry {
                wallet(testnet).getWalletsByPublicKey(query).accounts
            } ?: return emptyList()
            wallets.map { AccountDetailsEntity(query, it, testnet) }.map {
                if (it.walletVersion == WalletVersion.UNKNOWN) {
                    it.copy(
                        walletVersion = BaseWalletContract.resolveVersion(pk, it.address.toRawAddress(), testnet)
                    )
                } else {
                    it
                }
            }
        } catch (e: Throwable) {
            emptyList()
        }
    }

    fun getRates(currencies: List<String>, tokens: List<String>): Map<String, TokenRates>? {
        return withRetry {
            rates().getRates(
                tokens = tokens,
                currencies = currencies
            ).rates
        }
    }

    fun getRates(currency: String, tokens: List<String>): Map<String, TokenRates>? {
        return getRates(listOf(currency, "TON"), tokens)
    }

    fun getNft(address: String, testnet: Boolean): NftItem? {
        return withRetry { nft(testnet).getNftItemByAddress(address) }
    }

    fun getNftItems(
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

    private fun getPublicKey(
        accountId: String,
        testnet: Boolean
    ): PublicKeyEd25519? {
        val hex = withRetry {
            accounts(testnet).getAccountPublicKey(accountId)
        }?.publicKey ?: return null
        return PublicKeyEd25519(hex(hex))
    }

    fun safeGetPublicKey(
        accountId: String,
        testnet: Boolean
    ) = getPublicKey(accountId, testnet) ?: EmptyPrivateKeyEd25519.publicKey()

    fun tonconnectEvents(
        publicKeys: List<String>,
        lastEventId: Long? = null,
        onFailure: ((Throwable) -> Unit)?
    ): Flow<SSEvent> {
        if (publicKeys.isEmpty()) {
            return emptyFlow()
        }
        val value = publicKeys.joinToString(",")
        val url = "${BRIDGE_URL}/events?client_id=$value"
        return seeHttpClient.sse(url, lastEventId, onFailure).filter { it.type == "message" }
    }

    fun tonconnectPayload(): String? {
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

    suspend fun batteryVerifyPurchasePromo(testnet: Boolean, code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            battery(testnet).verifyPurchasePromo(code)
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun tonconnectProof(address: String, proof: String): String {
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
        withRetry {
            tonAPIHttpClient.post(url, body.toRequestBody(mimeType))
        }
    }

    fun estimateGaslessCost(
        tonProofToken: String,
        jettonMaster: String,
        cell: Cell,
        testnet: Boolean,
    ): String? {
        val request = io.batteryapi.models.EstimateGaslessCostRequest(cell.base64(), false)

        return withRetry {
            battery(testnet).estimateGaslessCost(jettonMaster, request, tonProofToken).commission
        }
    }

    fun emulateWithBattery(
        tonProofToken: String,
        cell: Cell,
        testnet: Boolean,
        safeModeEnabled: Boolean,
    ) = emulateWithBattery(tonProofToken, cell.base64(), testnet, safeModeEnabled)

    fun emulateWithBattery(
        tonProofToken: String,
        boc: String,
        testnet: Boolean,
        safeModeEnabled: Boolean,
    ): Pair<MessageConsequences, Boolean>? {
        val host = if (testnet) config.batteryTestnetHost else config.batteryHost
        val url = "$host/wallet/emulate"
        val data = "{\"boc\":\"$boc\",\"safe_mode\":$safeModeEnabled}"

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
        balance: Long? = null,
        safeModeEnabled: Boolean,
    ): MessageConsequences? = withContext(Dispatchers.IO) {
        val params = mutableListOf<EmulateMessageToWalletRequestParamsInner>()
        if (address != null) {
            params.add(EmulateMessageToWalletRequestParamsInner(address, balance))
        }
        val request = EmulateMessageToWalletRequest(
            boc = boc,
            params = params,
            safeMode = safeModeEnabled
        )
        withRetry {
            emulation(testnet).emulateMessageToWallet(request)
        }
    }

    suspend fun emulate(
        cell: Cell,
        testnet: Boolean,
        address: String? = null,
        balance: Long? = null,
        safeModeEnabled: Boolean,
    ): MessageConsequences? {
        return emulate(cell.hex(), testnet, address, balance, safeModeEnabled)
    }

    suspend fun sendToBlockchainWithBattery(
        boc: String,
        tonProofToken: String,
        testnet: Boolean,
        source: String,
        confirmationTime: Double,
    ): SendBlockchainState = withContext(Dispatchers.IO) {
        if (!isOkStatus(testnet)) {
            return@withContext SendBlockchainState.STATUS_ERROR
        }

        val request = io.batteryapi.models.EmulateMessageToWalletRequest(
            boc = boc,
            platform = "android",
            version = appVersionName,
            source = source,
            confirmationTime = confirmationTime
        )

        withRetry {
            battery(testnet).sendMessage(tonProofToken, request)
            SendBlockchainState.SUCCESS
        } ?: SendBlockchainState.UNKNOWN_ERROR
    }

    suspend fun sendToBlockchain(
        boc: String,
        testnet: Boolean,
        source: String,
        confirmationTime: Double,
    ): SendBlockchainState = withContext(Dispatchers.IO) {
        if (!isOkStatus(testnet)) {
            return@withContext SendBlockchainState.STATUS_ERROR
        }

        val request = SendBlockchainMessageRequest(
            boc = boc,
            platform = "android",
            version = appVersionName,
            source = source,
            confirmationTime = confirmationTime
        )
        withRetry {
            blockchain(testnet).sendBlockchainMessage(request)
            SendBlockchainState.SUCCESS
        } ?: SendBlockchainState.UNKNOWN_ERROR
    }

    fun getAccountSeqno(
        accountId: String,
        testnet: Boolean,
    ): Int = withRetry { wallet(testnet).getAccountSeqno(accountId).seqno } ?: 0

    suspend fun resolveAccount(
        value: String,
        testnet: Boolean,
    ): Account? = withContext(Dispatchers.IO) {
        /*if (value.isValidTonAddress()) {
            return@withContext getAccount(value, testnet)
        }
        return@withContext resolveDomain(value.lowercase().trim(), testnet)*/
        getAccount(value, testnet, null)
    }

    /*private suspend fun resolveDomain(domain: String, testnet: Boolean): Account? {
        return getAccount(domain, testnet) ?: getAccount(domain.unicodeToPunycode(), testnet)
    }*/

    private fun getAccount(
        accountId: String,
        testnet: Boolean,
        currency: String?,
    ): Account? {
        var normalizedAccountId = accountId
        if (normalizedAccountId.startsWith("https://")) {
            normalizedAccountId = normalizedAccountId.replace("https://", "")
        }
        if (normalizedAccountId.startsWith("t.me/")) {
            normalizedAccountId = normalizedAccountId.replace("t.me/", "")
            normalizedAccountId = "$normalizedAccountId.t.me"
        }
        if (!normalizedAccountId.isValidTonAddress()) {
            normalizedAccountId = normalizedAccountId.lowercase().trim()
        }
        return withRetry { accounts(testnet).getAccount(normalizedAccountId) }
    }

    fun pushSubscribe(
        locale: Locale,
        firebaseToken: String,
        deviceId: String,
        accounts: List<String>
    ): Boolean {
        if (accounts.isEmpty()) {
            return true
        }
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

    fun pushUnsubscribe(
        deviceId: String,
        accounts: List<String>
    ): Boolean {
        if (accounts.isEmpty()) {
            return true
        }

        val url = "${config.tonapiMainnetHost}/v1/internal/pushes/plain/unsubscribe"

        val accountsArray = JSONArray()
        for (account in accounts) {
            val jsonAccount = JSONObject()
            jsonAccount.put("address", account)
            accountsArray.put(jsonAccount)
        }

        val json = JSONObject()
        json.put("device", deviceId)
        json.put("accounts", accountsArray)

        return withRetry {
            val response = tonAPIHttpClient.postJSON(url, json.toString())
            response.isSuccessful
        } ?: false
    }

    fun getStories(id: String) = internalApi.getStories(id)

    fun pushTonconnectSubscribe(
        token: String,
        appUrl: String,
        accountId: String,
        firebaseToken: String,
        sessionId: String?,
        commercial: Boolean,
        silent: Boolean
    ): Boolean {
        val url = "${config.tonapiMainnetHost}/v1/internal/pushes/tonconnect"

        val json = JSONObject()
        json.put("app_url", appUrl)
        json.put("account", accountId)
        json.put("firebase_token", firebaseToken)
        sessionId?.let { json.put("session_id", it) }
        json.put("commercial", commercial)
        json.put("silent", !silent)
        val data = json.toString().replace("\\/", "/").trim()

        val headers = ArrayMap<String, String>().apply {
            set("X-TonConnect-Auth", token)
            set("Connection", "close")
        }

        val response = withRetry {
            tonAPIHttpClient.postJSON(url, data, headers)
        }

        return response?.isSuccessful ?: false
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
            json.put("commercial", false)
            json.put("silent", true)
            val data = json.toString().replace("\\/", "/")

            val builder = requestBuilder(url)
            builder.delete(data.toRequestBody("application/json".toMediaType()))
            builder.addHeader("X-TonConnect-Auth", token)
            builder.addHeader("Connection", "close")
            tonAPIHttpClient.execute(builder.build()).isSuccessful
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
            val response = withRetry {
                tonAPIHttpClient.get(url, ArrayMap<String, String>().apply {
                    set("X-TonConnect-Auth", token)
                })
            } ?: throw Exception("Empty response")
            val json = JSONObject(response)
            json.getJSONArray("items")
        } catch (e: Throwable) {
            JSONArray()
        }
    }

    fun getBrowserApps(testnet: Boolean, locale: Locale): JSONObject {
        return internalApi.getBrowserApps(testnet, locale)
    }

    fun getFiatMethods(testnet: Boolean, locale: Locale): JSONObject? {
        return withRetry { internalApi.getFiatMethods(testnet, locale) }
    }

    fun getTransactionEvents(accountId: String, testnet: Boolean, eventId: String): AccountEvent? {
        return try {
            accounts(testnet).getAccountEvent(accountId, eventId)
        } catch (e: Throwable) {
            null
        }
    }

    suspend fun getScamDomains(): Array<String> = withContext(Dispatchers.IO) {
        internalApi.getScamDomains()
    }

    fun loadChart(
        token: String,
        currency: String,
        startDate: Long,
        endDate: Long
    ): List<ChartEntity> {
        try {
            val url = "${config.tonapiMainnetHost}/v2/rates/chart?token=$token&currency=$currency&start_date=$startDate&end_date=$endDate"
            val array = JSONObject(tonAPIHttpClient.get(url)).getJSONArray("points")
            return (0 until array.length()).map { index ->
                ChartEntity(array.getJSONArray(index))
            }.asReversed()
        } catch (e: Throwable) {
            return listOf(ChartEntity(0, 0f))
        }
    }

    fun getServerTime(testnet: Boolean) = withRetry {
        liteServer(testnet).getRawTime().time
    } ?: (System.currentTimeMillis() / 1000).toInt()

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
        val response = withRetry {
            tonAPIHttpClient.postJSON(url, data)
        } ?: throw Exception("Empty response")
        if (!response.isSuccessful) {
            throw Exception("Failed creating proof: ${response.code}")
        }
        response.body?.string() ?: throw Exception("Empty response")
    }

    suspend fun reportTX(
        txId: String,
        comment: String?,
        recipient: String,
    ) = withContext(Dispatchers.IO) {
        val url = config.scamEndpoint + "/v1/report/tx/$txId"
        val json = JSONObject()
        json.put("recipient", recipient)
        comment?.let { json.put("comment", it) }
        val data = json.toString()
        val response = withRetry {
            tonAPIHttpClient.postJSON(url, data)
        } ?: throw Exception("Empty response")
        if (!response.isSuccessful) {
            throw Exception("Failed creating proof: ${response.code}")
        }
        response.body?.string() ?: throw Exception("Empty response")
    }

    companion object {
        const val BRIDGE_URL = "https://bridge.tonapi.io/bridge"
    }
}