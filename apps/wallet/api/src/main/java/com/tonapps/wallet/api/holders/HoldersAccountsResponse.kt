package com.tonapps.wallet.api.holders

import com.squareup.moshi.Json

data class HoldersAccountsResponse(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "list") val list: List<HoldersAccountEntity>,
    @Json(name = "prepaidCards") val prepaidCards: List<HoldersCardEntity>,
    @Json(name = "error") val error: String?
)
