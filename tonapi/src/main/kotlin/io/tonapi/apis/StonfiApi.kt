package io.tonapi.apis

import io.tonapi.infrastructure.ApiClient
import io.tonapi.infrastructure.ApiResponse
import io.tonapi.infrastructure.ClientError
import io.tonapi.infrastructure.ClientException
import io.tonapi.infrastructure.RequestConfig
import io.tonapi.infrastructure.RequestMethod
import io.tonapi.infrastructure.ResponseType
import io.tonapi.infrastructure.ServerError
import io.tonapi.infrastructure.ServerException
import io.tonapi.infrastructure.Success
import io.tonapi.models.StonfiJettonParamsRequest
import io.tonapi.models.StonfiRequest
import io.tonapi.models.StonfiJettonResponse
import io.tonapi.models.StonfiSimulateParamsRequest
import io.tonapi.models.StonfiSimulateReversedParamsRequest
import io.tonapi.models.StonfiSwapResponse
import okhttp3.OkHttpClient
import java.io.IOException

class StonfiApi(client: OkHttpClient = ApiClient.defaultClient) : ApiClient(defaultBasePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String = "https://rpc.ston.fi"
    }

    /**
     *
     * Get jetton&#39;s
     * @param loadCommunity return community jettons
     * @return StonfiJettonResponse
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun getJettons(loadCommunity: kotlin.Boolean = false) : StonfiJettonResponse {
        val localVarResponse = getJettonsWithHttpInfo(loadCommunity = loadCommunity)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as StonfiJettonResponse
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     *
     * Get jetton&#39;s
     * @param loadCommunity return community jettons
     * @return ApiResponse<JettonHolders?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun getJettonsWithHttpInfo(loadCommunity: kotlin.Boolean = false) : ApiResponse<StonfiJettonResponse?> {
        val localVariableConfig = getJettonsRequestConfig(loadCommunity = loadCommunity)

        return request<StonfiRequest, StonfiJettonResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation simulateSwap
     *
     * @param loadCommunity return community jettons
     * @return RequestConfig
     */
    fun getJettonsRequestConfig(loadCommunity: kotlin.Boolean = false) : RequestConfig<StonfiRequest> {
        return getStonfiMethodRequestConfig(method = "asset.list", params = StonfiJettonParamsRequest(loadCommunity = loadCommunity))
    }

    /**
     *
     * Simulate reversed swap
     *
     * @param askAddress address to swap
     * @param offerAddress address from swap
     * @param offerUnits amount to swap
     * @param referralAddress referral
     * @param slippageTolerance slippage for swap
     *
     * @return StonfiSwapResponse
     *
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun simulateReversedSwap(askAddress: kotlin.String, offerAddress: kotlin.String, askUnits: kotlin.String, referralAddress: kotlin.String, slippageTolerance: kotlin.String) : StonfiSwapResponse {
        val localVarResponse = simulateReversedSwapWithHttpInfo(askAddress = askAddress, offerAddress = offerAddress, askUnits = askUnits, referralAddress = referralAddress, slippageTolerance = slippageTolerance)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as StonfiSwapResponse
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     *
     * Simulate reversed swap
     *
     * @param askAddress address to swap
     * @param offerAddress address from swap
     * @param offerUnits amount to swap
     * @param referralAddress referral
     * @param slippageTolerance slippage for swap
     *
     * @return ApiResponse<StonfiSwapResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun simulateReversedSwapWithHttpInfo(askAddress: kotlin.String, offerAddress: kotlin.String, askUnits: kotlin.String, referralAddress: kotlin.String, slippageTolerance: kotlin.String) : ApiResponse<StonfiSwapResponse?> {
        val localVariableConfig = getReversedSwapSimulateRequestConfig(askAddress = askAddress, offerAddress = offerAddress, askUnits = askUnits, referralAddress = referralAddress, slippageTolerance = slippageTolerance)

        return request<StonfiRequest, StonfiSwapResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation simulateReversedSwap
     *
     * @param askAddress address to swap
     * @param offerAddress address from swap
     * @param askUnits amount to swap
     * @param referralAddress referral
     * @param slippageTolerance slippage for swap
     * @return RequestConfig
     */
    fun getReversedSwapSimulateRequestConfig(askAddress: kotlin.String, offerAddress: kotlin.String, askUnits: kotlin.String, referralAddress: kotlin.String, slippageTolerance: kotlin.String) : RequestConfig<StonfiRequest> {
        return getStonfiMethodRequestConfig(
            method = "dex.reverse_simulate_swap",
            params = StonfiSimulateReversedParamsRequest(
                askAddress = askAddress,
                offerAddress = offerAddress,
                askUnits = askUnits,
                referralAddress = referralAddress,
                slippageTolerance = slippageTolerance,
            )
        )
    }

    /**
     *
     * Simulate swap
     *
     * @param askAddress address to swap
     * @param offerAddress address from swap
     * @param offerUnits amount to swap
     * @param referralAddress referral
     * @param slippageTolerance slippage for swap
     *
     * @return StonfiSwapResponse
     *
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun simulateSwap(askAddress: kotlin.String, offerAddress: kotlin.String, offerUnits: kotlin.String, referralAddress: kotlin.String, slippageTolerance: kotlin.String) : StonfiSwapResponse {
        val localVarResponse = simulateSwapWithHttpInfo(askAddress = askAddress, offerAddress = offerAddress, offerUnits = offerUnits, referralAddress = referralAddress, slippageTolerance = slippageTolerance)

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as StonfiSwapResponse
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     *
     * Simulate swap
     *
     * @param askAddress address to swap
     * @param offerAddress address from swap
     * @param offerUnits amount to swap
     * @param referralAddress referral
     * @param slippageTolerance slippage for swap
     *
     * @return ApiResponse<StonfiSwapResponse?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun simulateSwapWithHttpInfo(askAddress: kotlin.String, offerAddress: kotlin.String, offerUnits: kotlin.String, referralAddress: kotlin.String, slippageTolerance: kotlin.String) : ApiResponse<StonfiSwapResponse?> {
        val localVariableConfig = getSwapSimulateRequestConfig(askAddress = askAddress, offerAddress = offerAddress, offerUnits = offerUnits, referralAddress = referralAddress, slippageTolerance = slippageTolerance)

        return request<StonfiRequest, StonfiSwapResponse>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation simulateSwap
     *
     * @param askAddress address to swap
     * @param offerAddress address from swap
     * @param offerUnits amount to swap
     * @param referralAddress referral
     * @param slippageTolerance slippage for swap
     * @return RequestConfig
     */
    fun getSwapSimulateRequestConfig(askAddress: kotlin.String, offerAddress: kotlin.String, offerUnits: kotlin.String, referralAddress: kotlin.String, slippageTolerance: kotlin.String) : RequestConfig<StonfiRequest> {
        return getStonfiMethodRequestConfig(
            method = "dex.simulate_swap",
            params = StonfiSimulateParamsRequest(
                askAddress = askAddress,
                offerAddress = offerAddress,
                offerUnits = offerUnits,
                referralAddress = referralAddress,
                slippageTolerance = slippageTolerance,
            )
        )
    }

    /**
     * To obtain the request config of the operation stonfi.method
     *
     * @param method call method
     * @param params params for method
     * @return RequestConfig
     */
    fun getStonfiMethodRequestConfig(method: kotlin.String, params: kotlin.Any) : RequestConfig<StonfiRequest> {
        val localVariableBody = StonfiRequest(
            method = method,
            params = params
        )
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.POST,
            path = "/",
            query = mutableMapOf(),
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }
}