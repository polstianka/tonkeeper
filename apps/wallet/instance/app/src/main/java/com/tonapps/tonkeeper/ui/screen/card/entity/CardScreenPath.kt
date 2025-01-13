package com.tonapps.tonkeeper.ui.screen.card.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class CardScreenPath: Parcelable {
    @Parcelize
    data class Account(val accountId: String) : CardScreenPath()
    @Parcelize
    data class Prepaid(val cardId: String) : CardScreenPath()
    @Parcelize
    data object Create : CardScreenPath()
    @Parcelize
    data object Main : CardScreenPath()
    @Parcelize
    data object Support : CardScreenPath()
}