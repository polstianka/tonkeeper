package com.tonapps.wallet.api.holders

import com.squareup.moshi.Json

data class HoldersAccountTokenResponse(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "token") val token: String
)
