package com.tonapps.tonkeeper.fragment.trade.domain

import com.tonapps.tonkeeper.fragment.trade.domain.model.BuyMethod

class GetBuyMethodsCase {

    // todo: fix when api is ready
    suspend fun execute(countryCode: String): List<BuyMethod> {
        return listOf(
            BuyMethod(
                id = "1",
                name = "Credit Card",
                iconUrl = ""
            ),
            BuyMethod(
                id = "2",
                name = "Cryptocurrency",
                iconUrl = ""
            ),
            BuyMethod(
                id = "3",
                name = "Google Pay",
                iconUrl = ""
            )
        )
    }
}