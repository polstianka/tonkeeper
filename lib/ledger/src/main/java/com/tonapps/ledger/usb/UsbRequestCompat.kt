package com.tonapps.ledger.usb

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbRequest
import android.os.Build
import java.nio.ByteBuffer

class UsbRequestCompat(
    connection: UsbDeviceConnection,
    endpoint: UsbEndpoint
): UsbRequest() {

    companion object {
        const val MAX_USB_FS_BUFFER_SIZE = 16384
    }

    init {
        if (!initialize(connection, endpoint)) {
            close()
            throw Exception("Failed to initialize UsbRequest")
        }
    }

    fun queueCompat(buffer: ByteBuffer, length: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            queue(buffer)
        } else {
            queue(buffer, length)
        }
    }
}