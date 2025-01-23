package com.tonapps.tonkeeper.manager.ledger.scan

import android.content.Context
import com.tonapps.tonkeeper.manager.ledger.device.LedgerDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class LedgerScan(
    val context: Context
) {

    private val _devicesFlow = MutableStateFlow<List<LedgerDevice>>(emptyList())
    val devicesFlow = _devicesFlow.asStateFlow()

    fun setDevices(devices: List<LedgerDevice>) {
        _devicesFlow.value = devices
    }

    abstract fun start()

    abstract fun stop()

}