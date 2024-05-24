package com.tonapps.tonkeeper.fragment.send.confirm

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import com.tonapps.wallet.localization.Localization
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.api.shortAddress
import com.tonapps.tonkeeper.core.signer.SingerResultContract
import com.tonapps.tonkeeper.fragment.send.SendScreenEffect
import com.tonapps.tonkeeper.fragment.send.pager.PagerScreen
import com.tonapps.tonkeeper.view.TransactionDetailView
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.ListCell
import kotlinx.coroutines.flow.map
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.ton.crypto.base64
import org.ton.crypto.hex
import uikit.extensions.collectFlow
import uikit.extensions.drawable
import uikit.extensions.pinToBottomInsets
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import uikit.widget.ProcessTaskView

class ConfirmScreen: PagerScreen<ConfirmScreenState, ConfirmScreenEffect, ConfirmScreenFeature>(R.layout.fragment_send_confirm) {

    companion object {

        fun newInstance() = ConfirmScreen()
    }

    override val feature: ConfirmScreenFeature by viewModel()

    private lateinit var iconView: FrescoView
    private lateinit var titleView: AppCompatTextView
    private lateinit var walletView: TransactionDetailView
    private lateinit var recipientView: TransactionDetailView
    private lateinit var recipientAddressView: TransactionDetailView
    private lateinit var amountView: TransactionDetailView
    private lateinit var feeView: TransactionDetailView
    private lateinit var commentView: TransactionDetailView
    private lateinit var actionView: View
    private lateinit var sendButton: Button
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

        titleView = view.findViewById(R.id.title)

        walletView = view.findViewById(R.id.wallet)
        walletView.title = getString(Localization.wallet)
        walletView.position = com.tonapps.uikit.list.ListCell.Position.FIRST

        recipientView = view.findViewById(R.id.recipient)
        recipientView.title = getString(Localization.recipient)
        recipientView.position = com.tonapps.uikit.list.ListCell.Position.MIDDLE

        recipientAddressView = view.findViewById(R.id.recipient_address)
        recipientAddressView.title = getString(Localization.recipient_address)
        recipientAddressView.position = com.tonapps.uikit.list.ListCell.Position.MIDDLE

        amountView = view.findViewById(R.id.amount)
        amountView.title = getString(Localization.amount)
        amountView.position = com.tonapps.uikit.list.ListCell.Position.MIDDLE

        feeView = view.findViewById(R.id.fee)
        feeView.title = getString(Localization.fee)
        feeView.position = com.tonapps.uikit.list.ListCell.Position.MIDDLE

        commentView = view.findViewById(R.id.comment)
        commentView.title = getString(Localization.comment)
        commentView.position = com.tonapps.uikit.list.ListCell.Position.LAST

        actionView = view.findViewById(R.id.action)
        actionView.pinToBottomInsets()

        sendButton = view.findViewById(R.id.send)

        processView = view.findViewById(R.id.process)

        sendFeature.transaction.observe(viewLifecycleOwner) { transaction ->
            setRecipient(transaction.address!!, transaction.name)
            setComment(transaction.comment)
            feature.setAmount(transaction.amountRaw, transaction.decimals, transaction.tokenAddress, transaction.tokenSymbol)
            iconView.setImageURI(transaction.icon)

            if (transaction.isTon) {
                titleView.setText(Localization.transfer)
            } else {
                titleView.text = getString(Localization.jetton_transfer, transaction.tokenName)
            }
        }

        collectFlow(sendFeature.transactionFlow.map { it.encryptComment }) { encryptComment ->
            if (encryptComment) {
                val drawable = requireContext().drawable(UIKitIcon.ic_lock_16)
                drawable.setTint(requireContext().accentGreenColor)
                commentView.setTitleRightDrawable(drawable)
            } else {
                commentView.setTitleRightDrawable(null)
            }
        }
    }

    private fun sign() {
        feature.sign(sendFeature.transaction.value!!)
    }

    private fun setComment(comment: String?) {
        if (comment.isNullOrEmpty()) {
            commentView.visibility = View.GONE
            feeView.position = ListCell.Position.LAST
        } else {
            commentView.visibility = View.VISIBLE
            commentView.value = comment

            feeView.position = ListCell.Position.MIDDLE
        }
    }

    private fun setRecipient(address: String, name: String?) {
        if (name.isNullOrEmpty()) {
            setAddress(address)
        } else {
            recipientView.value = name
            recipientAddressView.visibility = View.VISIBLE
            recipientAddressView.value = address.shortAddress
        }
    }

    private fun setAddress(address: String) {
        recipientAddressView.visibility = View.GONE
        recipientView.value = address.shortAddress
    }

    override fun newUiState(state: ConfirmScreenState) {
        if (state.processActive) {
            sendButton.visibility = View.GONE
            processView.visibility = View.VISIBLE
            processView.state = state.processState
        } else {
            sendButton.visibility = View.VISIBLE
            processView.visibility = View.GONE
        }

        state.walletLabel?.let {
            walletView.value = it.title
        }

        amountView.value = state.amount
        amountView.description = state.amountInCurrency

        sendButton.isEnabled = state.buttonEnabled

        if (state.fee.isNullOrEmpty()) {
            feeView.setLoading()
        } else {
            feeView.setData(state.fee, state.feeInCurrency)
        }

        sendButton.setOnClickListener {
            if (state.signer) {
                sign()
            } else {
                feature.send(requireContext(), sendFeature.transaction.value!!)
            }
        }
    }

    override fun newUiEffect(effect: ConfirmScreenEffect) {
        super.newUiEffect(effect)
        if (effect is ConfirmScreenEffect.CloseScreen) {
            if (effect.navigateToHistory) {
                navigation?.openURL("tonkeeper://activity")
            }

            sendFeature.sendEffect(SendScreenEffect.Finish)
        } else if (effect is ConfirmScreenEffect.OpenSignerApp) {
            signerLauncher.launch(SingerResultContract.Input(effect.body, effect.publicKey))
        }
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        sendFeature.setHeaderVisible(!visible)
        if (visible) {
            feature.requestFee(sendFeature.transaction.value!!)
        }
    }
}