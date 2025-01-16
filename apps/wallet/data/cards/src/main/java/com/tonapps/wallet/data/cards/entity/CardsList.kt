package com.tonapps.wallet.data.cards.entity

import com.tonapps.icu.Coins

data class CardsList(
    val cards: List<CardEntity>,
    val totalFiat: Coins
)
