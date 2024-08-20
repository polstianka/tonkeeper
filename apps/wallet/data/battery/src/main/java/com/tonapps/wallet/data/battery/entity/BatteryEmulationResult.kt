package com.tonapps.wallet.data.battery.entity

import io.tonapi.models.MessageConsequences

data class BatteryEmulationResult(
    val consequences: MessageConsequences, val withBattery: Boolean
)
