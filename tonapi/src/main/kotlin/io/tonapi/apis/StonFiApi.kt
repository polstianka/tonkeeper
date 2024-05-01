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
import io.tonapi.models.GetAllTokensRequestBody
import io.tonapi.models.GetAllTokensResponse
import okhttp3.OkHttpClient

class StonFiApi(
    basePath: kotlin.String = "https://app.ston.fi",
    client: OkHttpClient = defaultClient
) : ApiClient(basePath, client) {

    fun getAllTokens(walletAddress: String): GetAllTokensResponse {
        val localVariableBody = GetAllTokensRequestBody(
            jsonrpc = "2.0",
            id = 37,
            method = "asset.balance_list",
            params = GetAllTokensRequestBody.Params(
                walletAddress = walletAddress,
                loadCommunity = false
            )
        )
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Content-Type"] = "application/json"
        val cfg = RequestConfig(
            method = RequestMethod.POST,
            path = "/rpc",
            query = mutableMapOf(),
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
        val localVarResponse = request<GetAllTokensRequestBody, GetAllTokensResponse>(cfg)
        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as GetAllTokensResponse
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