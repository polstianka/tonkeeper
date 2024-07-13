package com.tonapps.tonkeeper.ui.screen.stake.confirm

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.icon
import com.tonapps.tonkeeper.core.signer.SingerResultContract
import com.tonapps.tonkeeper.ui.screen.stake.StakeScreenEffect
import com.tonapps.tonkeeper.ui.screen.stake.pager.PagerScreen
import com.tonapps.tonkeeper.view.TransactionDetailView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.dp
import uikit.extensions.pinToBottomInsets
import uikit.extensions.round
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import uikit.widget.ProcessTaskView
import uikit.widget.SlideActionView

class StakeConfirmScreen : PagerScreen<StakeConfirmScreenState, StakeConfirmScreenEffect, StakeConfirmScreenFeature>(R.layout.fragment_stake_confirm) {

    companion object {

        fun newInstance() = StakeConfirmScreen()
    }

    override val feature: StakeConfirmScreenFeature by viewModel()

    private lateinit var iconView: FrescoView
    private lateinit var amountView: AppCompatTextView
    private lateinit var amountCurView: AppCompatTextView
    private lateinit var depositView: AppCompatTextView
    private lateinit var walletView: TransactionDetailView
    private lateinit var recipientView: TransactionDetailView
    private lateinit var apyView: TransactionDetailView
    private lateinit var feeView: TransactionDetailView
    private lateinit var actionView: View
    private lateinit var sendButton: SlideActionView
    private lateinit var processView: ProcessTaskView

    private val signerLauncher = registerForActivityResult(SingerResultContract()) {
        if (it == null) {
            feature.setFailedResult()
        } else {
            feature.sendSignature(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iconView = view.findViewById(R.id.icon)
        iconView.round(48.dp)

        depositView = view.findViewById(R.id.deposit)
        amountView = view.findViewById(R.id.amount)
        amountCurView = view.findViewById(R.id.amount_cur)

        walletView = view.findViewById(R.id.wallet)
        walletView.title = getString(Localization.wallet)
        walletView.position = com.tonapps.uikit.list.ListCell.Position.FIRST

        recipientView = view.findViewById(R.id.recipient)
        recipientView.title = getString(Localization.recipient)
        recipientView.position = com.tonapps.uikit.list.ListCell.Position.MIDDLE

        apyView = view.findViewById(R.id.apy)
        apyView.title = getString(Localization.apy)
        apyView.position = com.tonapps.uikit.list.ListCell.Position.MIDDLE

        feeView = view.findViewById(R.id.fee)
        feeView.title = getString(Localization.fee)
        feeView.position = com.tonapps.uikit.list.ListCell.Position.LAST

        actionView = view.findViewById(R.id.action)
        actionView.pinToBottomInsets()

        sendButton = view.findViewById(R.id.send)
        processView = view.findViewById(R.id.process)
    }

    private fun sign() {
        feature.sign(stakeFeature.data.value!!)
    }

    override fun newUiState(state: StakeConfirmScreenState) {
        if (state.processActive) {
            sendButton.visibility = View.GONE
            processView.visibility = View.VISIBLE
            processView.state = state.processState
        } else {
            sendButton.visibility = View.VISIBLE
            processView.visibility = View.GONE
        }

        depositView.setText(if (state.isUnstake) Localization.unstake else Localization.deposit)

        state.walletLabel?.let {
            walletView.value = it.title
        }

        sendButton.isEnabled = state.buttonEnabled

        state.poolInfo?.let {
            iconView.setImageResource(it.implementation.icon)
            recipientView.value = it.name
            apyView.isVisible = !state.isUnstake
            apyView.value = "≈ ${CurrencyFormatter.format("%", it.apy)}"
        }

        amountView.text = state.amount
        amountCurView.text = state.amountInCurrency

        if (state.fee.isNullOrEmpty()) {
            feeView.setLoading()
        } else {
            feeView.setData(state.fee, state.feeInCurrency)
        }

        sendButton.doOnDone = {
            if (state.signer) {
                sign()
            } else {
                feature.send(requireContext(), stakeFeature.data.value!!)
            }
        }
    }

    override fun newUiEffect(effect: StakeConfirmScreenEffect) {
        super.newUiEffect(effect)
        if (effect is StakeConfirmScreenEffect.CloseScreenStake) {
            if (effect.navigateToHistory) {
                navigation?.openURL("tonkeeper://activity")
            }

            stakeFeature.sendEffect(StakeScreenEffect.Finish)
        } else if (effect is StakeConfirmScreenEffect.OpenSignerApp) {
            signerLauncher.launch(SingerResultContract.Input(effect.body, effect.publicKey))
        }
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            stakeFeature.setHeaderVisible(false)
            stakeFeature.data.value?.let {
                feature.update(it)
            }
        }
    }
}