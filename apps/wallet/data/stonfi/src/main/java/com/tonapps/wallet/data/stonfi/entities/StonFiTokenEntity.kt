package com.tonapps.wallet.data.stonfi.entities

import android.os.Parcelable
import com.tonapps.wallet.api.entity.TokenEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class StonFiTokenEntity(
    val token: TokenEntity,
    val tokens: List<TokenEntity>
) : Parcelable
