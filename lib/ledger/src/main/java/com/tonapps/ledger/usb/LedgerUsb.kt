package com.tonapps.ledger.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.core.app.PendingIntentCompat
import com.tonapps.ledger.ton.TonTransport
import com.tonapps.ledger.transport.TransportStatusException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.lang.IllegalStateException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LedgerUsb(
    private val context: Context,
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
) {

    fun deviceFlow() = callbackFlow {
        findConnectedDevice()?.let {
            trySend(it)
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                        findConnectedDevice()?.let {
                            trySend(it)
                        }
                    }
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        val device = findConnectedDevice()
                        if (device == null) {
                            close(Throwable("Device detached"))
                        } else {
                            trySend(device)
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        context.registerReceiver(receiver, filter)
        awaitClose { context.safeUnregisterReceiver(receiver) }
    }

    private fun findConnectedDevice() = usbManager.deviceList.values.firstOrNull {
        it.vendorId == LEDGER_VENDOR_ID
    }

    suspend fun connectDevice(device: UsbDevice): UsbTransport {
        if (!usbManager.hasPermission(device)) {
            val permissionGranted = requestPermission(device)
            if (!permissionGranted) {
                throw SecurityException("Permission denied")
            }
        }
        return openTransport(device)
    }

    suspend fun createTonTransport(
        usbTransport: UsbTransport,
        attempt: Int = 0
    ): TonTransport {
        val tonTransport = TonTransport(usbTransport)
        if (!tonTransport.isLocked()) {
            return tonTransport
        }
        if (attempt >= 30) {
            throw TransportStatusException.LockedDevice()
        }
        delay(2000)
        return createTonTransport(usbTransport, attempt + 1)
    }

    private fun openTransport(device: UsbDevice): UsbTransport {
        val usbInterface = device.getInterface(0)
        val connection = usbManager.openDevice(device) ?: throw Exception("Can't open device")
        val claimed = connection.claimInterface(usbInterface, true)
        if (!claimed) {
            connection.close()
            throw Exception("Can't claim interface")
        }
        var deviceOut: UsbEndpoint? = null
        var deviceIn: UsbEndpoint? = null
        for (i in 0 until usbInterface.endpointCount) {
            val endpoint = usbInterface.getEndpoint(i)
            if (usbInterface.getEndpoint(i).direction == UsbConstants.USB_DIR_IN) {
                deviceIn = endpoint
            } else {
                deviceOut = endpoint
            }
        }
        if (deviceOut == null && deviceIn == null) {
            connection.releaseInterface(usbInterface)
            connection.close()
            throw Exception("Can't find endpoints")
        }
        return UsbTransport(connection, deviceOut!!, deviceIn!!)
    }

    private suspend fun requestPermission(device: UsbDevice) = suspendCancellableCoroutine { continuation ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                context.safeUnregisterReceiver(this)
                continuation.resume(granted)
            }
        }
        val intent = Intent(PERMISSION_ACTION).apply {
            `package` = context.packageName
        }
        val permissionIntent = PendingIntentCompat.getBroadcast(context, 0, intent, 0, true)

        registerReceiverCompact(receiver)
        usbManager.requestPermission(device, permissionIntent)
        continuation.invokeOnCancellation {
            context.safeUnregisterReceiver(receiver)
        }
    }

    private fun registerReceiverCompact(receiver: BroadcastReceiver) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(PERMISSION_ACTION), Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter(PERMISSION_ACTION))
        }
    }

    private companion object {
        private const val PERMISSION_ACTION = "com.tonapps.ledger.USB_LEDGER_PERMISSION"
        private const val LEDGER_VENDOR_ID = 11415

        private fun Context.safeUnregisterReceiver(receiver: BroadcastReceiver) {
            try {
                unregisterReceiver(receiver)
            } catch (ignored: Throwable) { }
        }
    }
}