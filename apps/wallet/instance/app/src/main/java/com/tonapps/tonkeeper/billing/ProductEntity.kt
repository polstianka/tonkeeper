package com.tonapps.tonkeeper.billing

import com.android.billingclient.api.ProductDetails
import com.tonapps.wallet.api.entity.IAPPackageEntity
import com.tonapps.wallet.api.entity.IAPPackageId

data class ProductEntity(
    val inAppPackage: IAPPackageEntity,
    val details: ProductDetails,
) {

    val sku: String
        get() = details.productId

    val priceFormat: CharSequence
        get() = details.priceFormatted

    val packType: IAPPackageId
        get() = inAppPackage.id

    val userProceed: Double
        get() = inAppPackage.userProceed
}