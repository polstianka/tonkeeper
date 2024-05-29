package com.tonapps.tonkeeper.fragment.send.recipient

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.HtmlCompat
import com.tonapps.tonkeeper.extensions.clipboardText
import com.tonapps.tonkeeper.fragment.send.SendScreenEffect
import com.tonapps.tonkeeper.fragment.send.pager.PagerScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.fieldActiveBorderColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.map
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.collectFlow
import uikit.extensions.html
import uikit.extensions.pinToBottomInsets
import uikit.widget.InputView

class RecipientScreen: PagerScreen<RecipientScreenState, RecipientScreenEffect, RecipientScreenFeature>(R.layout.fragment_send_recipient) {

    companion object {
        fun newInstance() = RecipientScreen()
    }

    override val feature: RecipientScreenFeature by viewModel()

    private lateinit var addressInput: InputView
    private lateinit var commentInput: InputView
    private lateinit var nextButton: Button
    private lateinit var warningView: AppCompatTextView
    private lateinit var commentEncryptHintView: AppCompatTextView
    private lateinit var commentEncryptButton: AppCompatTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addressInput = view.findViewById(R.id.address)
        addressInput.doOnTextChange = {
            if (it.contains(" ")) {
                addressInput.text = it.replace(" ", "")
            } else {
                feature.requestAddressCheck(it.trim())
            }
        }
        addressInput.doOnIconClick = {
            addressInput.hideKeyboard()
            sendFeature.sendEffect(SendScreenEffect.OpenCamera)
        }
        addressInput.doOnButtonClick = { paste() }

        commentInput = view.findViewById(R.id.comment)
        commentInput.doOnTextChange = {
            sendFeature.setComment(it)
            checkRequireComment()
        }

        warningView = view.findViewById(R.id.warning)

        commentEncryptHintView = view.findViewById(R.id.comment_encrypt_hint)
        commentEncryptButton = view.findViewById(R.id.comment_encrypt_button)
        commentEncryptButton.setOnClickListener { sendFeature.toggleEncryptComment() }

        nextButton = view.findViewById(R.id.next)
        nextButton.setOnClickListener {
            sendFeature.nextPage()
        }
        nextButton.pinToBottomInsets()

        collectFlow(sendFeature.onReadyView) {
            openKeyboard()
        }

        collectFlow(sendFeature.transactionFlow.map { !it.encryptComment }) { encrypted ->
            if (encrypted) {
                commentEncrypted()
            } else {
                commentDecrypted()
            }
        }
    }

    private fun commentEncrypted() {
        commentEncryptHintView.setText(Localization.comment_decrypted_hint)
        commentEncryptButton.setText(Localization.encrypt_comment)
        commentInput.hint = getString(Localization.comment)
        commentInput.activeBorderColor = requireContext().fieldActiveBorderColor
        commentInput.hintColor = requireContext().textSecondaryColor
    }

    private fun commentDecrypted() {
        commentEncryptHintView.setText(Localization.comment_encrypted_hint)
        commentEncryptButton.setText(Localization.decrypt_comment)
        commentInput.hint = getString(Localization.encrypted_comment)
        commentInput.activeBorderColor = requireContext().accentGreenColor
        commentInput.hintColor = requireContext().accentGreenColor
    }

    fun setAddress(address: String?) {
        addressInput.text = address ?: ""
    }

    fun setComment(comment: String?) {
        commentInput.text = comment ?: ""
    }

    override fun newUiState(state: RecipientScreenState) {
        setAddressState(state.addressState)

        sendFeature.setAddress(state.address)
        sendFeature.setName(state.name)
        sendFeature.setBounce(state.bounce)
    }

    private fun checkRequireComment() {
        val comment = commentInput.text
        if (comment.isEmpty() && feature.isRequireComment) {
            warningView.visibility = View.VISIBLE
            warningView.text = getString(Localization.send_request_comment)
            nextButton.isEnabled = false
        } else {
            warningView.visibility = View.GONE
            nextButton.isEnabled = feature.isValidateAddress
        }
    }

    private fun setAddressState(state: RecipientScreenState.AddressState) {
        nextButton.isEnabled = state == RecipientScreenState.AddressState.VALID

        if (state == RecipientScreenState.AddressState.LOADING) {
            addressInput.loading = true
            addressInput.error = false
            return
        }

        addressInput.loading = false
        addressInput.error = state == RecipientScreenState.AddressState.INVALID

        checkRequireComment()
    }

    private fun paste() {
        val text = context?.clipboardText()
        if (!text.isNullOrEmpty()) {
            setAddress(text)
        }
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (!visible) {
            addressInput.hideKeyboard()
        } else {
            sendFeature.setHeaderTitle(getString(Localization.recipient))
            sendFeature.setHeaderSubtitle(null)
        }
    }

    private fun openKeyboard() {
        if (isVisibleForUser()) {
            addressInput.focus()
        }
    }
}