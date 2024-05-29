package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model

import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.LayoutByCountry

data class SelectedFiatModel(
    val visibleCurrency: String,
    val layoutByCountry: LayoutByCountry?
)
