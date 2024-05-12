package com.tonapps.wallet.localization

typealias Localization = R.string

val SupportedLanguages = listOf(
    Language(),
    Language("en"),
    Language("ru")
)

fun String.getNameResIdForCurrency(): Int {
    return when(lowercase()) {
        "usd" -> Localization.currency_usd_name
        "eur" -> Localization.currency_eur_name
        "rub" -> Localization.currency_rub_name
        "aed" -> Localization.currency_aed_name
        "uah" -> Localization.currency_uah_name
        "kzt" -> Localization.currency_kzt_name
        "uzs" -> Localization.currency_uzs_name
        "gbp" -> Localization.currency_gbp_name
        "chf" -> Localization.currency_chf_name
        "cny" -> Localization.currency_cny_name
        "krw" -> Localization.currency_krw_name
        "idr" -> Localization.currency_idr_name
        "inr" -> Localization.currency_inr_name
        "jpy" -> Localization.currency_jpy_name
        "cad" -> Localization.currency_cad_name
        "ars" -> Localization.currency_ars_name
        "byn" -> Localization.currency_byn_name
        "cop" -> Localization.currency_cop_name
        "etb" -> Localization.currency_etb_name
        "ils" -> Localization.currency_ils_name
        "kes" -> Localization.currency_kes_name
        "ngn" -> Localization.currency_ngn_name
        "ugx" -> Localization.currency_ugx_name
        "ves" -> Localization.currency_ves_name
        "zar" -> Localization.currency_zar_name
        "try" -> Localization.currency_try_name
        "thb" -> Localization.currency_thb_name
        "vnd" -> Localization.currency_vnd_name
        "brl" -> Localization.currency_brl_name
        "gel" -> Localization.currency_gel_name
        "bdt" -> Localization.currency_bdt_name
        "amd" -> Localization.bitcoin

        "ton" -> Localization.toncoin
        "btc" -> Localization.bitcoin
        else -> throw IllegalArgumentException("Unsupported currency: ${this}")
    }
}
