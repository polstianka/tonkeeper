package com.tonapps.wallet.data.battery

import org.koin.dsl.module

val batteryModule = module {
    single { BatteryRepository(get(), get(), get()) }
}