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
}