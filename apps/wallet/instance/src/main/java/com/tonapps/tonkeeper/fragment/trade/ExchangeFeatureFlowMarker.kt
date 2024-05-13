package com.tonapps.tonkeeper.fragment.trade

import uikit.navigation.FeatureFlowMarker

interface ExchangeFeatureFlowMarker : FeatureFlowMarker {
    companion object {
        val instance = object : ExchangeFeatureFlowMarker {}
    }
}