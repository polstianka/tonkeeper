package com.tonapps.tonkeeper.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.ProductType
import com.tonapps.extensions.MutableEffectFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    context: Context,
) {

    private var billingClient: BillingClient

    private val _purchasesFlow = MutableEffectFlow<List<Purchase>?>()
    val purchasesFlow = _purchasesFlow.asSharedFlow()

    private val _productsFlow = MutableStateFlow<List<ProductDetails>?>(null)
    val productsFlow = _productsFlow.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            _purchasesFlow.tryEmit(purchases)
        } else {
            _purchasesFlow.tryEmit(null)
        }
    }

    private var isInitialized = false

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isInitialized = true
                }
            }

            override fun onBillingServiceDisconnected() {
                isInitialized = false
            }
        })
    }

    fun getProducts(
        productIds: List<String>,
        productType: String = ProductType.INAPP
    ) {
        if (!isInitialized || productIds.isEmpty()) {
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

    // Method to request a purchase
    fun requestPurchase(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    suspend fun consumeProduct(purchase: Purchase) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient.consumePurchase(params)
    }

    suspend fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.INAPP)

        billingClient.queryPurchasesAsync(params.build()) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val pendingPurchases = purchasesList.filter { purchase ->
                    purchase.purchaseState != Purchase.PurchaseState.PENDING
                }
                if (pendingPurchases.isNotEmpty()) {
                    _purchasesFlow.tryEmit(pendingPurchases)
                }
            }
        }
    }
}