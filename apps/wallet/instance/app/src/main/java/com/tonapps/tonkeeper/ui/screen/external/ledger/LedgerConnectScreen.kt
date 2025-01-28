package com.tonapps.tonkeeper.ui.screen.external.ledger

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.extensions.openAppSettings
import com.tonapps.tonkeeper.ui.base.BaseWalletScreen
import com.tonapps.tonkeeper.ui.base.ScreenContext
import com.tonapps.tonkeeper.ui.component.ledger.LedgerTaskView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.dialog.alert.AlertDialog
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.hasPermission
import uikit.extensions.processAnnotation
import uikit.widget.ColumnLayout

class LedgerConnectScreen: BaseWalletScreen<ScreenContext.None>(R.layout.fragment_ledger_connect, ScreenContext.None), BaseFragment.Modal {

    override val viewModel: LedgerConnectViewModel by viewModel()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            setBluetoothConnection()
        } else {
            showBluetoothPermissionsAlert()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (isBluetoothEnabled()) {
            setBluetoothConnection()
        }
    }

    private val onCloseClick = View.OnClickListener { finish() }

    private lateinit var bodyView: ColumnLayout
    private lateinit var tabUsbView: View
    private lateinit var tabBluetoothView: View
    private lateinit var nextButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bodyView = view.findViewById(R.id.body)

        tabUsbView = view.findViewById(R.id.tab_usb)
        tabUsbView.setOnClickListener { setUsbConnection() }

        tabBluetoothView = view.findViewById(R.id.tab_bluetooth)
        tabBluetoothView.setOnClickListener { setBluetoothConnection() }

        nextButton = view.findViewById(R.id.next)

        view.findViewById<View>(R.id.cancel).setOnClickListener(onCloseClick)
        view.findViewById<View>(R.id.close).setOnClickListener(onCloseClick)

        collectFlow(viewModel.connectionTypeFlow, ::onConnectionType)
        collectFlow(viewModel.tasksFlow, ::applyTasks)

        setUsbConnection()
    }

    private fun clearTasks() {
        if (bodyView.childCount > 1) {
            bodyView.removeViews(1, bodyView.childCount - 2)
        }
    }

    private fun applyTasks(tasks: List<Ledger.Task>) {
        clearTasks()

        val taskViewParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 4.dp, 0, 4.dp)
        }

        for (task in tasks) {
            val taskView = LedgerTaskView(requireContext()).apply {
                label = getString(task.label).processAnnotation(context)
                // setState(task.state)
            }
            bodyView.addView(taskView, taskViewParams)
        }
    }

    private fun onConnectionType(type: Ledger.ConnectionType) {
        when (type) {
            Ledger.ConnectionType.USB -> {
                tabUsbView.setBackgroundResource(uikit.R.drawable.bg_button_tertiary)
                tabBluetoothView.background = null
            }
            Ledger.ConnectionType.BLE -> {
                tabUsbView.background = null
                tabBluetoothView.setBackgroundResource(uikit.R.drawable.bg_button_tertiary)
            }
            else -> {
                tabUsbView.background = null
                tabBluetoothView.background = null
            }
        }
    }

    private fun setUsbConnection() {
        viewModel.setConnectionType(Ledger.ConnectionType.USB)
    }

    private fun setBluetoothConnection() {
        if (!isBluetoothPermissionGranted()) {
            requestPermissionLauncher.launch(blePermissions)
        } else if (!isBluetoothEnabled()) {
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            viewModel.setConnectionType(Ledger.ConnectionType.BLE)
        }
    }

    private fun isBluetoothPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.BLUETOOTH) &&
                    hasPermission(Manifest.permission.BLUETOOTH_ADMIN) &&
                    hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }

    private fun showBluetoothPermissionsAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle(Localization.bluetooth_permissions_alert_title)
            .setMessage(Localization.bluetooth_permissions_alert_message)
            .setPositiveButton(Localization.bluetooth_permissions_alert_open_settings) {  requireContext().openAppSettings() }
            .setNegativeButton(Localization.cancel)
            .show()
    }

    companion object {
        private val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        private fun LedgerTaskView.setState(state: Ledger.State) {
            when (state) {
                Ledger.State.Default -> setDefault()
                Ledger.State.WaitingForConnection -> setLoading()
                else -> setDone()
            }
        }

        private fun Context.openInstallTonApp() {
            val ledgerLiveUrl = "ledgerlive://myledger?installApp=TON"
            val ledgerLiveStoreUrl = "https://play.google.com/store/apps/details?id=com.ledger.live"

            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ledgerLiveUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ledgerLiveStoreUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(storeIntent)
            }
        }
    }

}