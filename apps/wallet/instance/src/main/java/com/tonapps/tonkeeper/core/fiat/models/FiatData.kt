package com.tonapps.tonkeeper.core.fiat.models

import org.json.JSONObject

data class FiatData(
    val layoutByCountry: List<FiatLayout>,
    val defaultLayout: FiatLayout,
    val categories: List<FiatCategory>,
    val buy: List<FiatCategory>,
    val sell: List<FiatCategory>
) {

    constructor(string: String) : this(
        JSONObject(string)
    )

    constructor(json: JSONObject) : this(
        layoutByCountry = FiatLayout.parse(json.getJSONArray("layoutByCountry")),
        defaultLayout = FiatLayout(json.getJSONObject("defaultLayout")),
        categories = FiatCategory.parse(json.getJSONArray("categories")),
        buy = FiatCategory.parse(json.getJSONArray("buy")),
        sell = FiatCategory.parse(json.getJSONArray("sell"))
    )

    fun layoutByCountry(countryCode: String): FiatLayout {
        val layout = layoutByCountry.find { it.countryCode == countryCode }
        return layout ?: defaultLayout
    }

    fun getBuyItemsByMethods(methods: List<String>): List<FiatItem> {
        return getTypeItemsByMethods("buy", methods)
    }


    fun getSellItemsByMethods(methods: List<String>): List<FiatItem> {
        return getTypeItemsByMethods("sell", methods)
    }

    private fun getTypeItemsByMethods(type: String, methods: List<String>): List<FiatItem> {
        val result = mutableListOf<FiatItem>()
        categories.forEach { category ->
            if (category.type == type) {
                category.items.forEach { item ->
                    if (methods.any { item.id.contains(it) }) {
                        result.add(item)
                    }
                }
            }
        }
        return result
    }
}