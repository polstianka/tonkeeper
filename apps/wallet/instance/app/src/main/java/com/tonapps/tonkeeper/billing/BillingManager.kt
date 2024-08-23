package com.tonapps.tonkeeper.billing

import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

class BillingManager {

    // Example
    private val _purchasesFlow = MutableStateFlow<List<Purchase>?>(null)
    val purchasesFlow = _purchasesFlow.asStateFlow().filterNotNull()

    private val purchasesUpdatedListener = { purchases: List<Purchase> ->
        _purchasesFlow.value = purchases
    }


}