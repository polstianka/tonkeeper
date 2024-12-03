package com.tonapps.tonkeeper.ui.screen.dev

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.extensions.locale
import com.tonapps.security.Security
import com.tonapps.tonkeeper.core.DevSettings
import com.tonapps.tonkeeper.extensions.copyToClipboard
import com.tonapps.tonkeeper.extensions.showToast
import com.tonapps.tonkeeper.ui.base.BaseWalletScreen
import com.tonapps.tonkeeper.ui.base.ScreenContext
import com.tonapps.tonkeeper.ui.screen.dev.list.launcher.LauncherAdapter
import com.tonapps.tonkeeper.view.TransactionDetailView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.LinearLayoutManager
import com.tonapps.uikit.list.ListCell
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.dialog.alert.AlertDialog
import uikit.widget.HeaderView
import uikit.widget.item.ItemSwitchView

class DevScreen: BaseWalletScreen<ScreenContext.None>(R.layout.fragment_dev, ScreenContext.None), BaseFragment.BottomSheet {

    override val viewModel: DevViewModel by viewModel()

    private lateinit var iconsView: RecyclerView
    private lateinit var blurView: ItemSwitchView
    private lateinit var tonConnectLogsView: ItemSwitchView
    private lateinit var importMnemonicAgainView: View
    private lateinit var logView: View
    private lateinit var logDataView: AppCompatEditText
    private lateinit var logCopy: Button
    private lateinit var importPasscodeView: View
    private lateinit var importDAppsView: View
    private lateinit var systemFontSizeView: ItemSwitchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val headerView = view.findViewById<HeaderView>(R.id.header)
        headerView.doOnCloseClick = { finish() }

        val deviceView = view.findViewById<AppCompatTextView>(R.id.device)
        deviceView.text = getDeviceLines().joinToString("\n")

        iconsView = view.findViewById(R.id.icons)
        iconsView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL)
        iconsView.adapter = LauncherAdapter()

        blurView = view.findViewById(R.id.blur)
        blurView.setChecked(DevSettings.blurEnabled, false)
        blurView.doOnCheckedChanged = { isChecked, byUser ->
            if (byUser) {
                DevSettings.blurEnabled = isChecked
                toastAfterChange()
            }
        }

        systemFontSizeView = view.findViewById(R.id.ignore_system_font_size)
        systemFontSizeView.setChecked(DevSettings.ignoreSystemFontSize, false)
        systemFontSizeView.doOnCheckedChanged = { isChecked, byUser ->
            if (byUser) {
                DevSettings.ignoreSystemFontSize = isChecked
                toastAfterChange()
            }
        }

        tonConnectLogsView = view.findViewById(R.id.tc_logs)
        tonConnectLogsView.setChecked(DevSettings.tonConnectLogs, false)
        tonConnectLogsView.doOnCheckedChanged = { isChecked, byUser ->
            if (byUser) {
                DevSettings.tonConnectLogs = isChecked
                toastAfterChange()
            }
        }

        importMnemonicAgainView = view.findViewById(R.id.import_mnemonic_again)
        importMnemonicAgainView.setOnClickListener { importMnemonicAgain(false) }
        importMnemonicAgainView.setOnLongClickListener { importMnemonicAgain(true); true }

        importPasscodeView = view.findViewById(R.id.import_passcode)
        importPasscodeView.setOnClickListener { importPasscode() }

        importDAppsView = view.findViewById(R.id.import_dapps)
        importDAppsView.setOnClickListener { importDApps() }

        logView = view.findViewById(R.id.log)
        logDataView = view.findViewById(R.id.log_data)

        view.findViewById<Button>(R.id.log_close).setOnClickListener {
            logView.visibility = View.GONE
        }

        view.findViewById<View>(R.id.import_legacy).setOnClickListener {
            valuesFromLegacy()
        }

        logCopy = view.findViewById(R.id.log_copy)
    }

    private fun toastAfterChange() {
        requireContext().showToast("Restart app to apply changes")
    }

    private fun valuesFromLegacy() {
        navigation?.migrationLoader(true)
        viewModel.getLegacyStorage {
            showLog(it)
            navigation?.migrationLoader(false)
        }
    }

    private fun importDApps() {
        navigation?.migrationLoader(true)
        viewModel.importApps {
            showLog(it)
            navigation?.migrationLoader(false)
        }
    }

    private fun importPasscode() {
        navigation?.migrationLoader(true)
        viewModel.importPasscode {
            navigation?.migrationLoader(false)
        }
    }

    private fun importMnemonicAgain(withDisplayMnemonic: Boolean) {
        navigation?.migrationLoader(true)
        viewModel.importMnemonicAgain(withDisplayMnemonic) {
            showLog(it)
            navigation?.migrationLoader(false)
        }
    }

    private fun showLog(message: String) {
        logView.visibility = View.VISIBLE
        logDataView.setText(message)

        logCopy.setOnClickListener {
            requireContext().copyToClipboard(message, true)
        }
    }

    private fun getDeviceLines(): List<String> {
        val list = mutableListOf<String>()
        list.add("Context locale: ${requireContext().locale.language}")
        list.add("Android version: ${android.os.Build.VERSION.RELEASE} (API level ${android.os.Build.VERSION.SDK_INT})")
        list.add("Device model: ${android.os.Build.MODEL}")
        list.add("Screen size: ${resources.displayMetrics.widthPixels}x${resources.displayMetrics.heightPixels}")
        list.add("Device rooted: ${booleanToYesOrNo(Security.isDeviceRooted())}")
        list.add("Device strongbox: ${booleanToYesOrNo(Security.isSupportStrongBox(requireContext()))}")
        list.add("ADB enabled: ${booleanToYesOrNo(Security.isAdbEnabled(requireContext()))}")
        return list
    }

    private fun booleanToYesOrNo(value: Boolean): String {
        return if (value) "yes" else "no"
    }

    companion object {

        fun newInstance() = DevScreen()
    }
}