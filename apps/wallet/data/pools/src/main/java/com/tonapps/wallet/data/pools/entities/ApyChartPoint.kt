package com.tonapps.wallet.data.pools.entities

import android.os.Parcelable
import io.tonapi.models.ApyHistory
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class ApyChartPoint(
    val x: Long,
    val y: BigDecimal
) : Parcelable {

    constructor(data: ApyHistory) : this(
        x = data.time.toLong(),
        y = data.apy ?: BigDecimal.ZERO
    )
}