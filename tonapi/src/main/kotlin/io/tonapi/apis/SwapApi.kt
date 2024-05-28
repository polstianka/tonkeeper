@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.tonapi.apis

import android.util.Log
import io.tonapi.infrastructure.ApiClient
import io.tonapi.infrastructure.ApiResponse
import io.tonapi.infrastructure.ClientError
import io.tonapi.infrastructure.ClientException
import io.tonapi.infrastructure.MultiValueMap
import io.tonapi.infrastructure.RequestConfig
import io.tonapi.infrastructure.RequestMethod
import io.tonapi.infrastructure.ResponseType
import io.tonapi.infrastructure.ServerError
import io.tonapi.infrastructure.ServerException
import io.tonapi.infrastructure.Success
import io.tonapi.models.AssetList
import io.tonapi.models.PairList
import io.tonapi.models.SwapSimulateDetail
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.io.IOException

class SwapApi(
    basePath: kotlin.String = defaultBasePath,
    client: OkHttpClient = ApiClient.defaultClient
) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            "https://api.ston.fi"
        }
    }


    @Suppress("UNCHECKED_CAST")
    @Throws(
        IllegalStateException::class,
        IOException::class,
        UnsupportedOperationException::class,
        ClientException::class,
        ServerException::class
    )
    fun getAssets(): AssetList {
        val localVarResponse = getAssetsWithHttpInfo()

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as AssetList
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


    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun getAssetsWithHttpInfo(): ApiResponse<AssetList?> {
        val localVariableConfig = getAssetsRequestConfig()

        return request<Unit, AssetList>(
            localVariableConfig
        )
    }


    fun getAssetsRequestConfig(): RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/v1/assets",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(
        IllegalStateException::class,
        IOException::class,
        UnsupportedOperationException::class,
        ClientException::class,
        ServerException::class
    )
    fun getMarkets(): PairList {
        val localVarResponse = getMarketsWithHttpInfo()

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as PairList
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

    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    private fun getMarketsWithHttpInfo(): ApiResponse<PairList?> {
        val localVariableConfig = getMarketsRequestConfig()

        return request<Unit, PairList>(
            localVariableConfig
        )
    }

    private fun getMarketsRequestConfig(): RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/v1/markets",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(
        IllegalStateException::class,
        IOException::class,
        UnsupportedOperationException::class,
        ClientException::class,
        ServerException::class
    )
    fun getSwapSimulate(requestQueryParams: MutableMap<String, List<String>>): SwapSimulateDetail {
        val localVarResponse = getSimulateSwapWithHttpInfo(requestQueryParams)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as SwapSimulateDetail
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

    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    private fun getSimulateSwapWithHttpInfo(requestQueryParams: MutableMap<String, List<String>>): ApiResponse<SwapSimulateDetail?> {
        val localVariableConfig = getSimulateSwapRequestConfig(requestQueryParams)

        return request<Unit, SwapSimulateDetail>(
            localVariableConfig
        )
    }

    private fun getSimulateSwapRequestConfig(requestQueryParams: MutableMap<String, List<String>>): RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = requestQueryParams
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/v1/swap/simulate",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(
        IllegalStateException::class,
        IOException::class,
        UnsupportedOperationException::class,
        ClientException::class,
        ServerException::class
    )
    fun getReverseSwapSimulate(requestQueryParams: MutableMap<String, List<String>>): SwapSimulateDetail {
        val localVarResponse = getReverseSimulateSwapWithHttpInfo(requestQueryParams)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as SwapSimulateDetail
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

    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    private fun getReverseSimulateSwapWithHttpInfo(requestQueryParams: MutableMap<String, List<String>>): ApiResponse<SwapSimulateDetail?> {
        val localVariableConfig = getReverseSimulateSwapRequestConfig(requestQueryParams)

        Log.d(
            "asset-get",
            "sswapapi getReverseSimulateSwapWithHttpInfo localVariableConfig: ${localVariableConfig}"
        )

        return request<Unit, SwapSimulateDetail>(
            localVariableConfig
        )
    }

    private fun getReverseSimulateSwapRequestConfig(requestQueryParams: MutableMap<String, List<String>>): RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = requestQueryParams
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/v1/reverse_swap/simulate",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent)
            .build().encodedPathSegments[0]
}
