package com.tonapps.tonkeeper.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.tonapps.extensions.ErrorForUserException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val ProductDetails.priceFormatted: CharSequence
    get() = oneTimePurchaseOfferDetails!!.formattedPrice

val Purchase.walletId: String?
    get() = accountIdentifiers?.obfuscatedAccountId

val BillingResult.isSuccess: Boolean
    get() = responseCode == BillingClient.BillingResponseCode.OK

suspend fun <T> BillingClient.ready(block: suspend (client: BillingClient) -> T): T {
    return if (isReady && connectionState == BillingClient.ConnectionState.CONNECTED) {
        block(this)
    } else {
        block(getReady())
    }
}

private suspend fun BillingClient.getReady(): BillingClient = suspendCancellableCoroutine { continuation ->
    startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(result: BillingResult) {
            if (!continuation.isActive) return
            if (result.isSuccess) {
                continuation.resume(this@getReady)
            } else {
                continuation.resumeWithException(ErrorForUserException.of(result.debugMessage))
            }
        }

        override fun onBillingServiceDisconnected() { }
    })
}

suspend fun BillingClient.getProducts(
    ids: List<String>,
    type: String = ProductType.INAPP
): List<ProductDetails> = ready { client ->
    val productList = ids.map { productId ->
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(type)
            .build()
    }

    val params = QueryProductDetailsParams.newBuilder()
        .setProductList(productList)
        .build()

    val result = client.queryProductDetails(params)
    result.productDetailsList ?: emptyList()
}