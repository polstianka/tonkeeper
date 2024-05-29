package com.tonapps.tonkeeper.fragment.send.recipient

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.wallet.localization.Localization
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.extensions.clipboardText
import com.tonapps.tonkeeper.fragment.send.SendScreenEffect
import com.tonapps.tonkeeper.fragment.send.pager.PagerScreen
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.collectFlow
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

        nextButton = view.findViewById(R.id.next)
        nextButton.setOnClickListener {
            sendFeature.nextPage()
        }
        nextButton.pinToBottomInsets()

        collectFlow(sendFeature.onReadyView) {
            openKeyboard()
        }
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