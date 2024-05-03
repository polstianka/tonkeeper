package com.tonapps.wallet.api

import android.content.Context
import android.net.Uri
import android.util.ArrayMap
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.blockchain.ton.extensions.isValid
import com.tonapps.extensions.locale
import com.tonapps.extensions.unicodeToPunycode
import com.tonapps.network.SSEvent
import com.tonapps.network.get
import com.tonapps.network.interceptor.AcceptLanguageInterceptor
import com.tonapps.network.interceptor.AuthorizationInterceptor
import com.tonapps.network.post
import com.tonapps.network.postJSON
import com.tonapps.network.sse
import com.tonapps.wallet.api.entity.AccountDetailsEntity
import com.tonapps.wallet.api.entity.AssetEntity
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.api.entity.SwapDataEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.api.internal.ConfigRepository
import com.tonapps.wallet.api.internal.InternalApi
import io.tonapi.models.Account
import io.tonapi.models.AccountEvent
import io.tonapi.models.AccountEvents
import io.tonapi.models.EmulateMessageToWalletRequest
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
import java.math.BigDecimal
import java.util.Locale
import java.util.concurrent.TimeUnit

class API(
    private val context: Context,
    private val scope: CoroutineScope
) {

    val defaultHttpClient = baseOkHttpClientBuilder().build()

    private val tonAPIHttpClient: OkHttpClient by lazy {
        createTonAPIHttpClient(context, config.tonApiV2Key)
    }

    private val internalApi = InternalApi(context, defaultHttpClient)
    private val configRepository = ConfigRepository(context, scope, internalApi)

    val config: ConfigEntity
        get() = configRepository.configEntity

    private val provider: Provider by lazy {
        Provider(config.tonapiMainnetHost, config.tonapiTestnetHost, tonAPIHttpClient)
    }

    fun accounts(testnet: Boolean) = provider.accounts.get(testnet)

    fun wallet(testnet: Boolean) = provider.wallet.get(testnet)

    fun nft(testnet: Boolean) = provider.nft.get(testnet)

    fun blockchain(testnet: Boolean) = provider.blockchain.get(testnet)

    fun emulation(testnet: Boolean) = provider.emulation.get(testnet)

    fun rates() = provider.rates.get(false)

    fun stonfi(testnet: Boolean) = provider.stonfi.get(testnet)

    fun getWalletAssets(walletAddress: String, testnet: Boolean): List<AssetEntity> {
        val entityList = mutableListOf<AssetEntity>()
        val assetList = stonfi(testnet).getWalletAssets(walletAddress).assetList
        for (asset in assetList) {
            if (asset.blacklisted || asset.community || asset.deprecated) {
                continue
            }
            entityList.add(
                AssetEntity(
                    token = TokenEntity(
                        address = asset.contractAddress,
                        name = asset.displayName,
                        symbol = asset.symbol,
                        imageUri = Uri.parse(asset.imageUrl),
                        decimals = asset.decimals,
                        verification = TokenEntity.Verification.whitelist
                    ),
                    value = BigDecimal(asset.balance ?: "0").movePointLeft(asset.decimals)
                        .toFloat(),
                    walletAddress = asset.walletAddress.orEmpty(),
                    usdPrice = asset.dexPriceUsd?.toFloatOrNull() ?: 0f
                )
            )
        }
        return entityList
    }

    fun simulateSwap(
        offerAddress: String,
        askAddress: String,
        units: String,
        testnet: Boolean
    ): SwapDataEntity {
        val response = stonfi(testnet).simulateSwap(offerAddress, askAddress, units)
        return SwapDataEntity(
            offerUnits = response.offerUnits,
            askUnits = response.askUnits,
            priceImpact = response.priceImpact,
            minReceived = response.minAskUnits,
            routerAddress = response.routerAddress,
            poolAddress = response.poolAddress,
            providerFeeUnits = response.feeUnits,
            feeAddress = response.feeAddress
        )
    }

    fun simulateReverseSwap(
        offerAddress: String,
        askAddress: String,
        units: String,
        testnet: Boolean
    ): SwapDataEntity {
        val response = stonfi(testnet).simulateReverseSwap(offerAddress, askAddress, units)
        return SwapDataEntity(
            offerUnits = response.offerUnits,
            askUnits = response.askUnits,
            priceImpact = response.priceImpact,
            minReceived = response.minAskUnits,
            routerAddress = response.routerAddress,
            poolAddress = response.poolAddress,
            providerFeeUnits = response.feeUnits,
            feeAddress = response.feeAddress
        )
    }

    fun getEvents(
        accountId: String,
        testnet: Boolean,
        beforeLt: Long? = null,
        limit: Int = 20
    ): AccountEvents {
        return accounts(testnet).getAccountEvents(
            accountId = accountId,
            limit = limit,
            beforeLt = beforeLt
        )
    }

    fun getEvent(
        accountId: String,
        testnet: Boolean,
        eventId: String
    ): AccountEvent {
        return accounts(testnet).getAccountEvent(
            accountId = accountId,
            eventId = eventId
        )
    }

    fun getTonBalance(
        accountId: String,
        testnet: Boolean
    ): BalanceEntity {
        val account = accounts(testnet).getAccount(accountId)
        return BalanceEntity(TokenEntity.TON, Coin.toCoins(account.balance), accountId)
    }

    fun getJettonsBalances(
        accountId: String,
        testnet: Boolean,
        currency: String
    ): List<BalanceEntity> {
        val jettonsBalances = accounts(testnet).getAccountJettonsBalances(
            accountId = accountId,
            currencies = currency
        ).balances
        return jettonsBalances.map { BalanceEntity(it) }.filter { it.value > 0 }
    }

    fun resolveAddressOrName(
        query: String,
        testnet: Boolean
    ): AccountDetailsEntity? {
        return try {
            val account = accounts(testnet).getAccount(query)
            AccountDetailsEntity(query, account, testnet)
        } catch (e: Throwable) {
            null
        }
    }

    fun resolvePublicKey(
        pk: PublicKeyEd25519,
        testnet: Boolean
    ): List<AccountDetailsEntity> {
        return try {
            val query = pk.key.hex()
            val wallets = wallet(testnet).getWalletsByPublicKey(query).accounts
            wallets.map { AccountDetailsEntity(query, it, testnet) }
        } catch (e: Throwable) {
            emptyList()
        }
    }

    fun getRates(currency: String, tokens: List<String>): Map<String, TokenRates> {
        return rates().getRates(tokens.joinToString(","), currency).rates
    }

    fun getNft(address: String, testnet: Boolean): NftItem? {
        return try {
            nft(testnet).getNftItemByAddress(address)
        } catch (e: Throwable) {
            null
        }
    }

    fun getNftItems(address: String, testnet: Boolean): List<NftItem> {
        return accounts(testnet).getAccountNftItems(
            accountId = address,
            limit = 1000,
            indirectOwnership = true,
        ).nftItems
    }

    fun getPublicKey(
        accountId: String,
        testnet: Boolean
    ): String {
        return accounts(testnet).getAccountPublicKey(accountId).publicKey
    }

    fun accountEvents(accountId: String, testnet: Boolean): Flow<SSEvent> {
        val endpoint = if (testnet) {
            config.tonapiTestnetHost
        } else {
            config.tonapiMainnetHost
        }
        // val mempool = okHttpClient.sse("$endpoint/v2/sse/mempool?accounts=${accountId}")
        val tx =
            tonAPIHttpClient.sse("$endpoint/v2/sse/accounts/transactions?accounts=${accountId}")
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

    fun tonconnectPayload(): String {
        val url = "${config.tonapiMainnetHost}/v2/tonconnect/payload"
        val json = JSONObject(tonAPIHttpClient.get(url))
        return json.getString("payload")
    }

    fun tonconnectProof(address: String, proof: String): String {
        val url = "${config.tonapiMainnetHost}/v2/wallet/auth/proof"
        val data = "{\"address\":\"$address\",\"proof\":$proof}"
        val response = tonAPIHttpClient.postJSON(url, data)
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

    suspend fun emulate(
        boc: String,
        testnet: Boolean
    ): MessageConsequences = withContext(Dispatchers.IO) {
        val request = EmulateMessageToWalletRequest(boc)
        emulation(testnet).emulateMessageToWallet(request)
    }

    suspend fun emulate(
        cell: Cell,
        testnet: Boolean
    ): MessageConsequences {
        return emulate(cell.base64(), testnet)
    }

    suspend fun sendToBlockchain(
        boc: String,
        testnet: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = SendBlockchainMessageRequest(boc)
            blockchain(testnet).sendBlockchainMessage(request)
            true
        } catch (e: Throwable) {
            false
        }
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
        if (value.isValid()) {
            return@withContext getAccount(value, testnet)
        }
        return@withContext resolveDomain(value.lowercase().trim(), testnet)
    }

    private fun resolveDomain(domain: String, testnet: Boolean): Account? {
        return getAccount(domain, testnet) ?: getAccount(domain.unicodeToPunycode(), testnet)
    }

    private fun getAccount(accountId: String, testnet: Boolean): Account? {
        return try {
            accounts(testnet).getAccount(accountId)
        } catch (e: Throwable) {
            null
        }
    }

    fun pushSubscribe(
        locale: Locale,
        firebaseToken: String,
        deviceId: String,
        accounts: List<String>
    ): Boolean {
        return try {
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

            return tonAPIHttpClient.postJSON(url, json.toString()).isSuccessful
        } catch (e: Throwable) {
            false
        }
    }

    fun pushTonconnectSubscribe(
        token: String,
        appUrl: String,
        accountId: String,
        firebaseToken: String,
        sessionId: String?,
        commercial: Boolean = true,
        silent: Boolean = true
    ): Boolean {
        return try {
            val url = "${config.tonapiMainnetHost}/v1/internal/pushes/tonconnect"

            val json = JSONObject()
            json.put("app_url", appUrl)
            json.put("account", accountId)
            json.put("firebase_token", firebaseToken)
            sessionId?.let { json.put("session_id", it) }
            json.put("commercial", commercial)
            json.put("silent", silent)
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
        val url = "${config.tonapiMainnetHost}/v1/messages/history?account=$accountId"
        val response = tonAPIHttpClient.get(url, ArrayMap<String, String>().apply {
            set("X-TonConnect-Auth", token)
        })

        return try {
            val json = JSONObject(response)
            json.getJSONArray("items")
        } catch (e: Throwable) {
            JSONArray()
        }
    }

    fun getBrowserApps(): JSONObject {
        return internalApi.getBrowserApps()
    }

    fun getTransactionEvents(accountId: String, testnet: Boolean, eventId: String): AccountEvent? {
        return try {
            accounts(testnet).getAccountEvent(accountId, eventId)
        } catch (e: Throwable) {
            null
        }
    }

    companion object {

        const val BRIDGE_URL = "https://bridge.tonapi.io/bridge"

        val JSON = Json { prettyPrint = true }

        private fun baseOkHttpClientBuilder(): OkHttpClient.Builder {
            return OkHttpClient().newBuilder()
                .retryOnConnectionFailure(false)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
        }

        private fun createTonAPIHttpClient(
            context: Context,
            tonApiV2Key: String
        ): OkHttpClient {
            return baseOkHttpClientBuilder()
                .addInterceptor(AcceptLanguageInterceptor(context.locale))
                .addInterceptor(AuthorizationInterceptor.bearer(tonApiV2Key))
                .build()
        }
    }
}