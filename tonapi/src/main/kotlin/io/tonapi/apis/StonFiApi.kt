package io.tonapi.apis

import io.tonapi.infrastructure.ApiClient
import io.tonapi.infrastructure.ClientError
import io.tonapi.infrastructure.ClientException
import io.tonapi.infrastructure.RequestConfig
import io.tonapi.infrastructure.RequestMethod
import io.tonapi.infrastructure.ResponseType
import io.tonapi.infrastructure.ServerError
import io.tonapi.infrastructure.ServerException
import io.tonapi.infrastructure.Success
import io.tonapi.models.GetWalletAssetsResponse
import io.tonapi.models.JettonAddressResponse
import io.tonapi.models.SimulateSwapResponse
import okhttp3.OkHttpClient

class StonFiApi(
    basePath: String = "https://api.ston.fi",
    client: OkHttpClient = defaultClient
) : ApiClient(basePath, client) {

    fun getWalletAssets(walletAddress: String): GetWalletAssetsResponse {
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["accept"] = "application/json"
        val cfg = RequestConfig<Unit>(
            method = RequestMethod.GET,
            path = "/v1/wallets/$walletAddress/assets",
            query = mutableMapOf(),
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = null
        )
        val localVarResponse = request<Unit, GetWalletAssetsResponse>(cfg)
        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as GetWalletAssetsResponse
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException(
                    "Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}",
                    localVarError.statusCode,
                    localVarResponse
                )
            }

            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException(
                    "Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}",
                    localVarError.statusCode,
                    localVarResponse
                )
            }
        }
    }

    fun simulateSwap(
        offerAddress: String,
        askAddress: String,
        units: String,
        tolerance: String,
        reverse: Boolean
    ): SimulateSwapResponse {
        val path = if (reverse) "/v1/reverse_swap/simulate" else "/v1/swap/simulate"
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["accept"] = "application/json"
        val cfg = RequestConfig<Unit>(
            method = RequestMethod.POST,
            path = path,
            query = mutableMapOf(
                "offer_address" to listOf(offerAddress),
                "ask_address" to listOf(askAddress),
                "units" to listOf(units),
                "slippage_tolerance" to listOf(tolerance),
            ),
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = null
        )
        val localVarResponse = request<Unit, SimulateSwapResponse>(cfg)
        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as SimulateSwapResponse
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException(
                    "Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}",
                    localVarError.statusCode,
                    localVarResponse
                )
            }

            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException(
                    "Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}",
                    localVarError.statusCode,
                    localVarResponse
                )
            }
        }
    }

    fun getJettonAddress(ownerAddress: String, address: String): JettonAddressResponse {
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["accept"] = "application/json"
        val cfg = RequestConfig<Unit>(
            method = RequestMethod.POST,
            path = "/v1/jetton/$address/address",
            query = mutableMapOf("owner_address" to listOf(ownerAddress)),
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = null
        )
        val localVarResponse = request<Unit, JettonAddressResponse>(cfg)
        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as JettonAddressResponse
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException(
                    "Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}",
                    localVarError.statusCode,
                    localVarResponse
                )
            }

            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException(
                    "Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}",
                    localVarError.statusCode,
                    localVarResponse
                )
            }
        }
    }

}