package com.tonapps.tonkeeper.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.ProductType
import com.tonapps.extensions.MutableEffectFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BillingManager(
    context: Context,
    scope: CoroutineScope,
) {

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .build()

    private val _productsFlow = MutableStateFlow<List<ProductDetails>?>(null)
    val productsFlow = _productsFlow.asStateFlow()

    private val _madePurchaseFlow = MutableEffectFlow<Unit>()
    val madePurchaseFlow = _madePurchaseFlow.shareIn(scope, SharingStarted.Lazily, 1)

    init {
        notifyPurchase()
    }

    private fun notifyPurchase() {
        _madePurchaseFlow.tryEmit(Unit)
    }

    suspend fun consumeProduct(purchaseToken: String) = withContext(Dispatchers.IO) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        billingClient.consumePurchase(params)
        notifyPurchase()
    }

    fun getProducts(
        productIds: List<String>,
        productType: String = ProductType.INAPP
    ) {
        if (productIds.isEmpty()) {
            return
        }

        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productsFlow.value = productDetailsList
            } else {
                _productsFlow.value = emptyList()  // In case of an error
            }
        }
    }

    private suspend fun getPendingPurchase(): List<Purchase> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.INAPP)

        val purchasesList = suspendCoroutine<List<Purchase>> { continuation ->
            billingClient.queryPurchasesAsync(params.build()) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchasesList)
                } else {
                    continuation.resume(emptyList()) // Or handle the error appropriately
                }
            }
        }

        return purchasesList.filter { purchase ->
            purchase.purchaseState != Purchase.PurchaseState.PENDING
        }
    }

    // Method to request a purchase
    suspend fun requestPurchase(
        activity: Activity,
        productDetails: ProductDetails
    ): List<Purchase> = billingClient.ready {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            getPendingPurchase()
        }

        emptyList()
    }

    suspend fun restorePurchases(): List<Purchase> = billingClient.ready {
        getPendingPurchase()
    }
}