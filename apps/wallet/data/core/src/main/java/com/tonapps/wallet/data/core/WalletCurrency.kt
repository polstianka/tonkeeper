package com.tonapps.wallet.data.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WalletCurrency(
    val code: String,
    val fiat: Boolean
): Parcelable {

    companion object {

        val FIAT = listOf(
            "USD", // United States Dollar
            "EUR", // Euro
            "RUB", // Russian Ruble
            "AED", // United Arab Emirates Dirham
            "UAH", // Ukrainian Hryvnia
            "KZT", // Kazakhstani Tenge
            "UZS", // Uzbekistani sum
            "GBP", // Great Britain Pound
            "CHF", // Swiss Franc
            "CNY", // China Yuan
            "KRW", // South Korean Won
            "IDR", // Indonesian Rupiah
            "INR", // Indian Rupee
            "JPY", // Japanese Yen
            "CAD", // Canadian Dollar
            "ARS", // Argentine Peso
            "BYN", // Belarusian Ruble
            "COP", // Colombian Peso
            "ETB", // Ethiopian Birr
            "ILS", // Israeli Shekel
            "KES", // Kenyan Shilling
            "NGN", // Nigerian Naira
            "UGX", // Ugandan Shilling
            "VES", // Venezuelan Bolivar
            "ZAR", // South African Rand
            "TRY", // Turkish Lira
            "THB", // Thai Baht
            "VND", // Vietnamese Dong
            "BRL", // Brazilian Real
            "BDT", // Bangladeshi Taka
        )

        val CRYPTO = listOf(
            "TON", "BTC",
        )

        const val USD_CODE = "USD"
        const val TON_CODE = "TON"

        val DEFAULT = WalletCurrency(FIAT.first())
        val USD = WalletCurrency(USD_CODE)
        val TON = WalletCurrency(TON_CODE)

        val ALL = FIAT + CRYPTO
    }

    constructor(code: String) : this(code, code in FIAT)
}
