package com.tonapps.tonkeeper.manager.ledger.scan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.tonapps.tonkeeper.manager.ledger.device.LedgerDevice

class LedgerUSBScan(context: Context): LedgerScan(context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)?.let {
                        updateDevices()
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)?.let {
                        updateDevices()
                    }
                }
            }
        }
    }

    override fun start() {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        context.registerReceiver(receiver, filter)
        updateDevices()
    }

    override fun stop() {
        context.unregisterReceiver(receiver)
    }

    private fun updateDevices() {
        setDevices(getDevices())
    }

    private fun getDevices(): List<LedgerDevice.USB> {
        return try {
            usbManager.deviceList.values.map {
                LedgerDevice.USB(it)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


}