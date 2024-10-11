package com.tonapps.tonkeeper.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.tonapps.extensions.MutableEffectFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BillingManager(
    context: Context,
    private var scope: CoroutineScope,
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _productsFlow = MutableStateFlow<List<ProductDetails>?>(null)
    val productsFlow = _productsFlow.asStateFlow()

    private val _madePurchaseFlow = MutableEffectFlow<Unit>()
    val madePurchaseFlow = _madePurchaseFlow.shareIn(scope, SharingStarted.Lazily, 1)

    private var purchasesChannel: Channel<List<Purchase>>? = null

    init {
        notifyPurchase()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        scope.launch {
            purchasesChannel?.send(
                if (result.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                    purchases
                } else {
                    emptyList()
                }
            )
        }
    }

    private fun notifyPurchase() {
        _madePurchaseFlow.tryEmit(Unit)
    }

    suspend fun consumeProduct(purchaseToken: String) = withContext(Dispatchers.IO) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        billingClient.consumePurchase(params)
        notifyPurchase()
    }

    suspend fun getProducts(
        productIds: List<String>,
        productType: String = ProductType.INAPP
    ) = billingClient.ready {
        if (productIds.isEmpty()) {
            return@ready
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

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchasesChannel = Channel()

                try {
                    val purchases = purchasesChannel!!.receive()
                    purchases
                } catch (_: Exception) {
                    emptyList()
                } finally {
                    purchasesChannel?.close()
                    purchasesChannel = null
                }
            }

            else -> {
                emptyList()
            }
        }
    }

    suspend fun restorePurchases(): List<Purchase> = billingClient.ready {
        getPendingPurchase()
    }
}