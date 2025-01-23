package com.tonapps.tonkeeper.ui.screen.external.ledger

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.manager.ledger.device.LedgerDevice
import com.tonapps.tonkeeper.manager.ledger.scan.LedgerUSBScan
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uikit.base.BaseFragment

class NewLedgerScreen: BaseFragment(R.layout.fragment_ledger_pair), BaseFragment.Modal {

    private val usbManager: UsbManager by lazy {
        requireContext().getSystemService(Context.USB_SERVICE) as UsbManager
    }

    private val usbScan: LedgerUSBScan by lazy {
        LedgerUSBScan(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usbScan.devicesFlow.onEach { devices ->
            devices.firstOrNull()?.let {
                connect(it)
            }
        }.launchIn(lifecycleScope)
    }

    private fun connect(device: LedgerDevice) {
        Log.d("LedgerNewLog", "Device: $device")
        if (device is LedgerDevice.USB) {
            connectUSB(device.device)
        }
    }

    private fun connectUSB(device: UsbDevice) {
        if (usbManager.hasPermission(device)) {
            usbManager.requestPermission(device)
            Log.d("LedgerNewLog", "Device connected")
        } else {
            Log.d("LedgerNewLog", "Request permission")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        usbScan.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usbScan.stop()
    }

}