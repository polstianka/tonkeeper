package com.tonapps.tonkeeper.dialog.trade.list

sealed class PaymentType {
    data object CreditCard : PaymentType()

    data class LocalCreditCard(val currency: String) : PaymentType()

    data object Crypto : PaymentType()
}
