package com.tonapps.wallet.data.cards.entity

import android.os.Parcelable
import com.tonapps.wallet.api.holders.HoldersAccountEntity
import com.tonapps.wallet.api.holders.HoldersCardEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardsDataEntity(
    val accounts: List<HoldersAccountEntity>,
    val prepaidCards: List<HoldersCardEntity>,
    val state: String = "{}"
) : Parcelable
