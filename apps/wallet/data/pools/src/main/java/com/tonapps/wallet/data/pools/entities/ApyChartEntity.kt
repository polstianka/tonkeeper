package com.tonapps.wallet.data.pools.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApyChartEntity(
    val data: List<ApyChartPoint>
) : Parcelable