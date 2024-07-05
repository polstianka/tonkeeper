package com.tonapps.tonkeeper.ui.screen.settings.passcode

import android.os.Bundle
import android.util.Log
import android.view.View
import com.tonapps.tonkeeper.ui.component.PasscodeView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.widget.HeaderView

class ChangePasscodeScreen: BaseFragment(R.layout.fragment_change_passcode), BaseFragment.SwipeBack {

    private val changePasscodeViewModel: ChangePasscodeViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var passcodeView: PasscodeView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnCloseClick = { finish() }

        passcodeView = view.findViewById(R.id.passcode)
        collectFlow(changePasscodeViewModel.stepFlow, ::setStep)
        collectFlow(changePasscodeViewModel.errorFlow) { applyError() }
    }

    private fun setStep(step: ChangePasscodeViewModel.Step) {
        when (step) {
            ChangePasscodeViewModel.Step.Current -> applyCurrent()
            ChangePasscodeViewModel.Step.New -> applyNew()
            ChangePasscodeViewModel.Step.Confirm -> applyConfirm()
            ChangePasscodeViewModel.Step.Saved -> finish()
        }
    }

    private fun applyCurrent() {
        passcodeView.clear()
        passcodeView.setTitle(Localization.passcode_current)
        passcodeView.doOnCheck = {
            changePasscodeViewModel.checkCurrent(it)
        }
    }

    private fun applyNew() {
        passcodeView.clear()
        passcodeView.setTitle(Localization.passcode_new)
        passcodeView.doOnCheck = {
            changePasscodeViewModel.setNew(it)
        }
    }

    private fun applyConfirm() {
        passcodeView.clear()
        passcodeView.setTitle(Localization.passcode_re_enter)
        passcodeView.doOnCheck = {
            changePasscodeViewModel.save(it)
        }
    }

    private fun applyError() {
        passcodeView.setError()
    }

    companion object {
        fun newInstance() = ChangePasscodeScreen()
    }

}