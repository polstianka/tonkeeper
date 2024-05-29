package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("buy")val buy: List<Operation>,
    @SerialName("defaultLayout")val defaultLayout: DefaultLayout,
    @SerialName("layoutByCountry")val layoutByCountry: List<LayoutByCountry>,
    @SerialName("sell")val sell: List<Operation>
)