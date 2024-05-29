package com.tonapps.wallet.data.token.entities

import android.os.Parcelable
import com.tonapps.wallet.api.entity.BalanceEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class BalanceListEntity(
    val list: List<BalanceEntity>
): Parcelable