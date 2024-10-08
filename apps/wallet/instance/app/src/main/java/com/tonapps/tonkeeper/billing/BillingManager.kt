package com.tonapps.tonkeeper.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.ProductType
import com.tonapps.extensions.MutableEffectFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BillingManager(
    context: Context,
    scope: CoroutineScope,
) {

    private var billingClient: BillingClient

    private val _purchasesFlow = MutableEffectFlow<List<Purchase>?>()
    val purchasesFlow = _purchasesFlow.asSharedFlow()

    private val _productsFlow = MutableStateFlow<List<ProductDetails>?>(null)
    val productsFlow = _productsFlow.asStateFlow()

    private val _madePurchaseFlow = MutableEffectFlow<Unit>()
    val madePurchaseFlow = _madePurchaseFlow.shareIn(scope, SharingStarted.Lazily, 1)

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

        notifyPurchase()
    }

    fun notifyPurchase() {
        _madePurchaseFlow.tryEmit(Unit)
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

    suspend fun restorePurchases(): List<Purchase> {
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

        val pendingPurchases = purchasesList.filter { purchase ->
            purchase.purchaseState != Purchase.PurchaseState.PENDING
        }

        if (pendingPurchases.isNotEmpty()) {
            _purchasesFlow.tryEmit(pendingPurchases)
        }

        return pendingPurchases
    }
}