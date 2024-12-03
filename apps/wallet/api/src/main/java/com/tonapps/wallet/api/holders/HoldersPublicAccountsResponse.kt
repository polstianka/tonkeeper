package com.tonapps.wallet.api.holders

import com.squareup.moshi.Json

data class HoldersPublicAccountsResponse(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "accounts") val accounts: List<HoldersAccountEntity>,
    @Json(name = "error") val error: String?
)
