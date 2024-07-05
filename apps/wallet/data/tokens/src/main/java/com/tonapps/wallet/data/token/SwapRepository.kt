package com.tonapps.wallet.data.token

import android.content.Context
import android.util.Log
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.token.entities.SwapSimulateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SwapRepository(
    private val context: Context,
    private val api: API
) {

    suspend fun simulate(
        offerAddress: String,
        askAddress: String,
        units: String,
        slippageTolerance: String,
        isReverse : Boolean,
        testnet: Boolean
    ): SwapSimulateEntity? = withContext(Dispatchers.IO) {

        val swapSimulateRequest = if (isReverse) {
            async {
                api.getReverseSwapSimulate(
                    offerAddress,
                    askAddress,
                    units,
                    slippageTolerance,
                    testnet
                )
            }
        } else {
            async {
                api.getSwapSimulate(
                    offerAddress,
                    askAddress,
                    units,
                    slippageTolerance,
                    testnet
                )
            }
        }

        val swapSimulateDetail = swapSimulateRequest.await()

        Log.d("asset-get", " new-swap-repo simulate: ${swapSimulateDetail}")

        swapSimulateDetail?.run { SwapSimulateEntity(swapSimulateDetail) }
    }

    suspend fun getWalletAddress(jettonMaster: String, owner: String, testnet: Boolean): String? =
        withContext(Dispatchers.IO) {
            val result = api.getWalletAddress(jettonMaster, owner, testnet)
            // MsgAddressInt.parse(address!!)
            val walletAddress =
                (result?.decoded as? Map<String, String>)?.get("jetton_wallet_address")
            walletAddress
        }


}