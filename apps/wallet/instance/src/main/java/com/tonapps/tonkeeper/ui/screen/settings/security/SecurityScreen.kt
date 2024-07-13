package com.tonapps.tonkeeper.ui.screen.settings.security

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.password.PasscodeBiometric
import com.tonapps.tonkeeper.ui.screen.phrase.PhraseScreen
import com.tonapps.tonkeeper.ui.screen.settings.passcode.ChangePasscodeScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.item.ItemIconView
import uikit.widget.item.ItemSwitchView

class SecurityScreen: BaseFragment(R.layout.fragment_security), BaseFragment.SwipeBack {

    private val securityViewModel: SecurityViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var biometricView: ItemSwitchView
    private lateinit var lockScreenView: ItemSwitchView
    private lateinit var changePasscodeView: ItemIconView
    private lateinit var recoveryPhraseView: ItemIconView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnCloseClick = { finish() }

        biometricView = view.findViewById(R.id.biometric)
        biometricView.checked = securityViewModel.biometric
        biometricView.doOnCheckedChanged = { securityViewModel.biometric = it }

        val biometricDescriptionView = view.findViewById<View>(R.id.biometric_description)
        val biometricVisibility = if (PasscodeBiometric.isAvailableOnDevice(requireContext())) {
            View.VISIBLE
        } else {
            View.GONE
        }
        biometricView.visibility = biometricVisibility
        biometricDescriptionView.visibility = biometricVisibility

        lockScreenView = view.findViewById(R.id.lock_screen)
        lockScreenView.checked = securityViewModel.lockScreen
        lockScreenView.doOnCheckedChanged = { securityViewModel.lockScreen = it }

        changePasscodeView = view.findViewById(R.id.change_passcode)
        changePasscodeView.setOnClickListener { navigation?.add(ChangePasscodeScreen.newInstance()) }

        recoveryPhraseView = view.findViewById(R.id.recovery_phrase)
        recoveryPhraseView.setOnClickListener { openRecoveryPhrase() }

        collectFlow(securityViewModel.hasMnemonicFlow) { hasMnemonic ->
            if (hasMnemonic) {
                actionsWithPhrase()
            } else {
                actionsWithoutPhrase()
            }
        }
    }

    private fun actionsWithPhrase() {
        changePasscodeView.position = ListCell.Position.FIRST
        recoveryPhraseView.position = ListCell.Position.LAST
        recoveryPhraseView.visibility = View.VISIBLE
    }

    private fun actionsWithoutPhrase() {
        changePasscodeView.position = ListCell.Position.SINGLE
        recoveryPhraseView.visibility = View.GONE
    }

    private fun openRecoveryPhrase() {
        securityViewModel.getRecoveryPhrase(requireContext()).catch {
            navigation?.toast(Localization.authorization_required)
        }.onEach {
            navigation?.add(PhraseScreen.newInstance(it))
        }.launchIn(lifecycleScope)
    }

    companion object {
        fun newInstance() = SecurityScreen()
    }
}