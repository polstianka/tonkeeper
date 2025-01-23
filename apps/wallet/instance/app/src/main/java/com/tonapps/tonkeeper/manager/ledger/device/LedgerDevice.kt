package com.tonapps.tonkeeper.manager.ledger.device

import android.hardware.usb.UsbDevice
import com.ledger.live.ble.model.BleDeviceModel

sealed class LedgerDevice {

    data class USB(val device: UsbDevice): LedgerDevice()
    data class BLE(val device: BleDeviceModel): LedgerDevice() {

        val id: String
            get() = device.id
    }
}