package com.tonapps.tonkeeper.billing

import com.android.billingclient.api.BillingResult

class BillingException(val result: BillingResult): Exception(result.debugMessage)