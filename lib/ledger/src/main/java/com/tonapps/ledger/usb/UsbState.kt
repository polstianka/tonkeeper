package com.tonapps.ledger.usb

import android.hardware.usb.UsbDevice

sealed class UsbState {

    data object WaitPermission: UsbState()

    data class Found(val device: UsbDevice): UsbState()
}