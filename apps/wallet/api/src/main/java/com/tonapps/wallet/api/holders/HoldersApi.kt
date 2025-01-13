package com.tonapps.wallet.api.holders

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tonapps.blockchain.ton.contract.BaseWalletContract
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.blockchain.ton.extensions.hex
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.blockchain.ton.proof.TONProof
import com.tonapps.network.WebSocketEvent
import com.tonapps.network.webSocket
import com.tonapps.wallet.api.entity.ConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class HoldersApi(
    private val okHttpClient: OkHttpClient,
    private val getConfig: (testnet: Boolean) -> ConfigEntity
) {

    private fun endpoint(path: String, testnet: Boolean): String {
        val host =
            if (testnet) "https://card-staging.whales-api.com" else getConfig(false).holdersServiceEndpoint
        return host + path
    }

    private fun wsEndpoint(testnet: Boolean): String {
        return endpoint("/v2/updates", testnet).replace("https", "wss")
    }

    private suspend inline fun <reified T> post(
        path: String,
        testnet: Boolean,
        payload: Map<String, Any>
    ): T = withContext(Dispatchers.IO) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(Map::class.java)
        val jsonPayload = jsonAdapter.toJson(payload)
        val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())

        val url = endpoint(path, testnet)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Access-Control-Allow-Origin", "*")
            .addHeader("Access-Control-Allow-Headers", "*")
            .addHeader("Access-Control-Allow-Credentials", "true")
            .build()

        val response = okHttpClient.newCall(request).execute()

        if (response.code == 401) {
            throw IllegalArgumentException("Unauthorized")
        }

        val responseBody = response.body?.string() ?: throw Exception("Empty response body")
        val adapter: JsonAdapter<T> = moshi.adapter(T::class.java)

        val parseResult = adapter.fromJson(responseBody)
            ?: throw IllegalArgumentException("Invalid response")

        parseResult
    }

    private fun getNetwork(testnet: Boolean): String {
        return if (testnet) "ton-testnet" else "ton-mainnet"
    }

    fun wsEvents(token: String, testnet: Boolean, onFailure: ((Throwable) -> Unit)?): Flow<WebSocketEvent> {
        val (flow) = okHttpClient.webSocket(wsEndpoint(testnet), onFailure = onFailure, onOpen = {
            it.send(
                JSONObject(
                    mapOf(
                        "type" to "connect",
                        "token" to token
                    )
                ).toString()
            )
        })

        return flow
    }

    suspend fun fetchAccountsPublic(
        address: String,
        testnet: Boolean
    ): List<HoldersAccountEntity> = withContext(Dispatchers.IO) {
        val response = post<HoldersPublicAccountsResponse>(
            "/v2/public/accounts", testnet, mapOf(
                "walletKind" to "tonkeeper",
                "network" to getNetwork(testnet),
                "address" to address
            )
        )

        if (!response.ok) {
            throw IllegalArgumentException("Error fetching card list: ${response.error}")
        }

        response.accounts
    }

    suspend fun fetchAccountsList(
        token: String,
        testnet: Boolean
    ): HoldersAccountsResponse = withContext(Dispatchers.IO) {
        val response = post<HoldersAccountsResponse>(
            "/v2/account/list", testnet, mapOf(
                "token" to token
            )
        )

        if (!response.ok) {
            throw IllegalArgumentException("Error fetching card list: ${response.error}")
        }

        response
    }

    suspend fun fetchAccountToken(
        contract: BaseWalletContract,
        proof: TONProof.Result,
        testnet: Boolean
    ): String = withContext(Dispatchers.IO) {
        val payload = mapOf(
            "stack" to "ton",
            "network" to getNetwork(testnet),
            "key" to mapOf(
                "kind" to "tonconnect-v2",
                "wallet" to "tonkeeper",
                "config" to mapOf(
                    "address" to contract.address.toAccountId(),
                    "proof" to mapOf(
                        "timestamp" to proof.timestamp,
                        "domain" to mapOf(
                            "lengthBytes" to proof.domain.lengthBytes,
                            "value" to proof.domain.value
                        ),
                        "signature" to proof.signature,
                        "payload" to proof.payload,
                        "walletStateInit" to contract.stateInitCell().base64(),
                        "publicKey" to contract.publicKey.hex()
                    )
                )
            )
        )

        val response =
            post<HoldersAccountTokenResponse>("/v2/user/wallet/connect", testnet, payload)

        if (!response.ok) {
            throw IllegalArgumentException("Error fetching account token")
        }

        response.token
    }

    suspend fun fetchUserState(token: String, testnet: Boolean) = withContext(Dispatchers.IO) {
        val response = post<HoldersUserState>(
            "/v2/user/state", testnet, mapOf(
                "token" to token
            )
        )

        if (!response.ok) {
            throw IllegalArgumentException("Error fetching user state")
        }

        response.toJSON()
    }
}