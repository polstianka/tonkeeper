package com.tonapps.ledger.usb

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import kotlinx.io.IOException
import java.nio.ByteBuffer
import kotlin.math.min

fun UsbDeviceConnection.writeByUsbRequest(endpoint: UsbEndpoint, bytes: ByteArray) {
    val length = bytes.size
    val usbRequest = UsbRequestCompat(this, endpoint)
    try {
        val buffer = ByteBuffer.wrap(bytes.copyOfRange(0, length))
        if (length < UsbRequestCompat.MAX_USB_FS_BUFFER_SIZE) {
            if (!usbRequest.queueCompat(buffer, length)) {
                throw IOException("Error queueing USB request. (Write)")
            }
            requestWait()
        } else {
            val chunks = buffer.array().asIterable().chunked(UsbRequestCompat.MAX_USB_FS_BUFFER_SIZE / 2)
            for (chunk in chunks) {
                if (!usbRequest.queueCompat(ByteBuffer.wrap(chunk.toByteArray()), chunk.size)) {
                    throw IOException("Error queueing USB request. (Write)")
                }
                requestWait()
            }
        }
    } finally {
        usbRequest.close()
    }
}

fun UsbDeviceConnection.writeByBulk(endpoint: UsbEndpoint, bytes: ByteArray) {
    var count = bytes.size
    var offset = 0
    while (count > 0) {
        val l = min(endpoint.maxPacketSize, count)
        val snd = bulkTransfer(endpoint, bytes, offset, l, 50)
        if (snd < 0) {
            throw IOException("Error sending USB data. (Write)")
        }
        count -= snd
        offset += snd
    }
}
