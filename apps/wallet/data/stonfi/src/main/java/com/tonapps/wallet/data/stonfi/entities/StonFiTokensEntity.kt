package com.tonapps.wallet.data.stonfi.entities

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class StonFiTokensEntity(
    val list: List<StonFiTokenEntity>
) : Parcelable {

    @IgnoredOnParcel
    val map: Map<String, StonFiTokenEntity> = list.associateBy { it.token.address }
}
