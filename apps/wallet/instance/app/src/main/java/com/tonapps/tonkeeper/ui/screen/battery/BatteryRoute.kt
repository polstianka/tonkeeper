package com.tonapps.tonkeeper.ui.screen.battery

sealed class BatteryRoute {
    data object Refill: BatteryRoute()
    data object Settings: BatteryRoute()
}