package com.tonapps.tonkeeper.billing

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase

data class PurchasesUpdated(
    val result: BillingResult,
    val purchases: List<Purchase>
)