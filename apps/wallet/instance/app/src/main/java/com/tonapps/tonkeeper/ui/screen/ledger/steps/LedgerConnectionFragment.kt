package com.tonapps.tonkeeper.ui.screen.ledger.steps

import android.Manifest
import android.animation.ObjectAnimator
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.ledger.steps.list.Adapter
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.dialog.alert.AlertDialog
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.hasPermission

class LedgerConnectionFragment : Fragment(R.layout.fragment_ledger_steps) {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all {
            it.value
        }
        if (allGranted) {
            connectionViewModel.scanOrConnect()
        } else {
            showBluetoothPermissionsAlert()
        }
    }
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}
    private val appSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkPermissionsAndScan()
    }

    private val connectionViewModel: LedgerConnectionViewModel by viewModel(ownerProducer = { requireParentFragment() })

    private val adapter = Adapter(::openInstallTonApp)

    private lateinit var listView: RecyclerView
    private lateinit var bluetoothIconView: AppCompatImageView
    private lateinit var ledgerView: RelativeLayout
    private lateinit var ledgerDisplayView: LinearLayout
    private lateinit var ledgerDisplayText: TextView

    private var blFadeAnim: ObjectAnimator? = null
    private var displayFadeAnim: ObjectAnimator? = null
    private var translationX: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        collectFlow(connectionViewModel.uiItemsFlow, adapter::submitList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bluetoothIconView = view.findViewById(R.id.bluetooth_icon)
        ledgerView = view.findViewById(R.id.ledger_picture)
        ledgerDisplayView = view.findViewById(R.id.ledger_display)
        ledgerDisplayText = view.findViewById(R.id.ledger_display_text)

        listView = view.findViewById(R.id.list)
        listView.adapter = adapter

        collectFlow(connectionViewModel.currentStepFlow, ::animateView)
        collectFlow(connectionViewModel.displayTextFlow) { text ->
            ledgerDisplayText.text = text
        }

        collectFlow(connectionViewModel.bluetoothState) { state ->
            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    connectionViewModel.disconnect()
                    promptEnableBluetooth()
                }

                BluetoothAdapter.STATE_ON -> {
                    checkPermissionsAndScan();
                }
            }
        }
    }

    private fun openInstallTonApp() {
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

    private fun showBluetoothPermissionsAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(Localization.bluetooth_permissions_alert_title)
        builder.setMessage(Localization.bluetooth_permissions_alert_message)
        builder.setPositiveButton(Localization.bluetooth_permissions_alert_open_settings) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            appSettingsLauncher.launch(intent)
        }
        builder.setNegativeButton(Localization.cancel)
        builder.show()
    }

    private fun promptEnableBluetooth() {
        if (isPermissionGranted()) {
            val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            }
        } else {
            requestPermissionLauncher.launch(blePermissions)
        }
    }

    private fun checkPermissionsAndScan() {
        if (isPermissionGranted()) {
            connectionViewModel.scanOrConnect()
        } else {
            requestPermissionLauncher.launch(blePermissions)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.BLUETOOTH) && hasPermission(Manifest.permission.BLUETOOTH_ADMIN) && hasPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun animateView(currentStep: LedgerStep) {
        val animInterpolator = AccelerateDecelerateInterpolator()

        blFadeAnim?.cancel()
        displayFadeAnim?.cancel()
        translationX?.cancel()

        if (currentStep == LedgerStep.CONNECT) {
            blFadeAnim =
                ObjectAnimator.ofFloat(bluetoothIconView, "alpha", bluetoothIconView.alpha, 1f)
                    .apply {
                        duration = 300
                        startDelay = 200
                        interpolator = animInterpolator
                        start()
                    }

            displayFadeAnim =
                ObjectAnimator.ofFloat(ledgerDisplayView, "alpha", ledgerDisplayView.alpha, 0f)
                    .apply {
                        duration = 300
                        interpolator = animInterpolator
                        start()
                    }

            translationX =
                ObjectAnimator.ofFloat(ledgerView, "translationX", ledgerView.translationX, 24f.dp)
                    .apply {
                        duration = 300
                        startDelay = 200
                        interpolator = animInterpolator
                        start()
                    }
        } else {
            blFadeAnim =
                ObjectAnimator.ofFloat(bluetoothIconView, "alpha", bluetoothIconView.alpha, 0f)
                    .apply {
                        duration = 300
                        interpolator = animInterpolator
                        start()
                    }

            displayFadeAnim =
                ObjectAnimator.ofFloat(ledgerDisplayView, "alpha", ledgerDisplayView.alpha, 1f)
                    .apply {
                        duration = 300
                        startDelay = 150
                        interpolator = animInterpolator
                        start()
                    }

            translationX = ObjectAnimator.ofFloat(
                ledgerView,
                "translationX",
                ledgerView.translationX,
                (-42f).dp
            ).apply {
                duration = 350
                interpolator = animInterpolator
                start()
            }
        }
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

        fun newInstance(): LedgerConnectionFragment {
            return LedgerConnectionFragment()
        }
    }
}
