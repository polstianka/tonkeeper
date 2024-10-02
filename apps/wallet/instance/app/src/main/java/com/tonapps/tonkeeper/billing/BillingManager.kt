package com.tonapps.tonkeeper.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.tonapps.extensions.ErrorForUserException
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.wallet.api.entity.IAPPackageEntity
import com.tonapps.wallet.data.account.entities.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext

class BillingManager(context: Context): PurchasesUpdatedListener {

    private val _purchasesFlow = MutableStateFlow<List<Purchase>?>(null)
    val purchasesFlow = _purchasesFlow.asStateFlow().filterNotNull()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    suspend fun requestPurchase(
        activity: Activity,
        wallet: WalletEntity,
        productDetails: ProductDetails
    ) = billingClient.ready { client ->
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .setObfuscatedAccountId(wallet.id)
            .build()

        client.launchBillingFlow(activity, billingFlowParams)
    }

    suspend fun consumeProduct(purchaseToken: String) {
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        billingClient.consumePurchase(params)
    }

    suspend fun restorePurchases(): List<PurchaseHistoryRecord> = billingClient.ready { client ->
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = client.queryPurchaseHistory(params)
        if (result.billingResult.isSuccess) {
            result.purchaseHistoryRecordList ?: emptyList()
        } else {
            throw ErrorForUserException.of("Failed to restore purchases: ${result.billingResult.debugMessage}")
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        purchases?.let { _purchasesFlow.tryEmit(it) }
    }

    suspend fun getProducts(input: List<IAPPackageEntity>): List<ProductEntity> = withContext(Dispatchers.IO) {
        val products = billingClient.getProducts(input.map { it.productId })
        val output = mutableListOf<ProductEntity>()
        for (productDetails in products) {
            val entity = input.find { it.productId == productDetails.productId } ?: continue
            output.add(ProductEntity(entity, productDetails))
        }
        output.toList()
    }
}