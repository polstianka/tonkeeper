package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model

import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.LayoutByCountry
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.uikit.list.ListCell

fun LayoutByCountry.toItem(position: ListCell.Position): Item.CurrencyList {
    return Item.CurrencyList(
        position = position,
        currency = this.currency,
        countryCode = this.countryCode,
        methods = this.methods,
    )
}

fun String.formatNumber(): String {

    // Преобразуем строку в число с плавающей точкой
    val number = this.toDoubleOrNull() ?: return this

    // Форматируем число с двумя знаками после запятой
    val formatted = String.format("%.2f", number)

    // Если после точки только нули, вернем целую часть
    if (formatted.endsWith(".00")) {
        return formatted.substringBefore(".")
    }

    // Убираем лишние нули после запятой
    val result = formatted.trimEnd('0').trimEnd('.')

    return result
}


fun replaceFlagsFromUrl(
    template: String,
    countryCode: String?,
    currencyNm: String?,
    getCurrency: String?,
    address: String?,
    txId: String?
): String {


    val linkPatterns = mapOf(
        "https://api.tonkeeper.com/" to 1,
        "https://buy.neocrypto.net?" to 2,
        "https://exchange.mercuryo.io?" to 3,
        "https://dreamwalkers.io/" to 4,
        "https://onramp.money/main/buy/" to 5
    )

    val curTo = getCurrency

    var result = template

    val linkType = linkPatterns.entries.find { template.startsWith(it.key) }?.value
    val curFrom = if (linkType != null) {
        when (linkType) {
            1 -> {
                countryCode
            }

            3 -> {
                currencyNm
            }

            else -> {
                countryCode
            }
        }
    } else {
        countryCode
    }
    // Создание маппинга флагов и их значений
    val replacements = mapOf(
        "CUR_FROM" to curFrom,
        "ADDRESS" to address,
        "CUR_TO" to curTo,
        "TX_ID" to txId
    )

    // Регулярное выражение для поиска флагов в формате {FLAG}
    val regex = "\\{(CUR_FROM|ADDRESS|CUR_TO|TX_ID)\\}".toRegex()

    // Замена найденных флагов на соответствующие значения
    result = regex.replace(result) { matchResult ->
        val flag = matchResult.groupValues[1]
        replacements[flag]
            ?: matchResult.value // Если значение для флага не передано, оставляем флаг без изменений
    }

    return result
}


enum class DealState {
    BUY, SELL
}