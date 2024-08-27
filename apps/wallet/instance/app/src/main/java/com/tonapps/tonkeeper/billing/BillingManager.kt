package com.tonapps.tonkeeper.billing

import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

class BillingManager {

    // Example
    private val _purchasesFlow = MutableSharedFlow<List<Purchase>>()
    val purchasesFlow = _purchasesFlow.asSharedFlow()

    private val purchasesUpdatedListener = { purchases: List<Purchase> ->
        // _purchasesFlow.value = purchases
    }


}