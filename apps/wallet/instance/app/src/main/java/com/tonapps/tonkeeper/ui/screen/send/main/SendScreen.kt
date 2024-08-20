package com.tonapps.tonkeeper.ui.screen.send.main

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toUri
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.icu.CurrencyFormatter.withCustomSymbol
import com.tonapps.ledger.ton.Transaction
import com.tonapps.tonkeeper.api.shortAddress
import com.tonapps.tonkeeper.core.AnalyticsHelper
import com.tonapps.tonkeeper.core.signer.SingerResultContract
import com.tonapps.tonkeeper.extensions.clipboardText
import com.tonapps.tonkeeper.extensions.getTitle
import com.tonapps.tonkeeper.fragment.camera.CameraFragment
import com.tonapps.tonkeeper.ui.base.BaseWalletScreen
import com.tonapps.tonkeeper.ui.component.coin.CoinInputView
import com.tonapps.tonkeeper.ui.screen.ledger.sign.LedgerSignScreen
import com.tonapps.tonkeeper.ui.screen.send.contacts.SendContactsScreen
import com.tonapps.tonkeeper.ui.screen.send.main.state.SendAmountState
import com.tonapps.tonkeeper.ui.screen.send.main.state.SendDestination
import com.tonapps.tonkeeper.ui.screen.send.main.state.SendTransaction
import com.tonapps.tonkeeper.ui.screen.signer.qr.SignerQRScreen
import com.tonapps.tonkeeper.view.TransactionDetailView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.fieldActiveBorderColor
import com.tonapps.uikit.color.fieldErrorBorderColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.Wallet
import com.tonapps.wallet.data.collectibles.entities.NftEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.ton.bitstring.BitString
import org.ton.boc.BagOfCells
import uikit.base.BaseFragment
import uikit.dialog.modal.ModalDialog
import uikit.extensions.collectFlow
import uikit.extensions.doKeyboardAnimation
import uikit.extensions.dp
import uikit.extensions.drawable
import uikit.extensions.hideKeyboard
import uikit.navigation.Navigation.Companion.navigation
import uikit.span.ClickableSpanCompat
import uikit.widget.FrescoView
import uikit.widget.HeaderView
import uikit.widget.InputView
import uikit.widget.ProcessTaskView
import uikit.widget.SlideBetweenView
import java.util.UUID

class SendScreen: BaseWalletScreen(R.layout.fragment_send_new), BaseFragment.BottomSheet {

    private val args: SendArgs by lazy { SendArgs(requireArguments()) }
    private val signerQRRequestKey: String by lazy { "send_${UUID.randomUUID()}" }
    private val contractsRequestKey: String by lazy { "contacts_${UUID.randomUUID()}" }

    override val viewModel: SendViewModel by viewModel { parametersOf(args.nftAddress) }

    private val signerResultContract = SingerResultContract()
    private val signerLauncher = registerForActivityResult(signerResultContract) {
        if (it != null) {
            setLoading()
            viewModel.sendSignedMessage(it)
        }
    }

    private val lockDrawable: Drawable by lazy {
        val drawable = requireContext().drawable(UIKitIcon.ic_lock_16)
        drawable.setTint(requireContext().accentGreenColor)
        drawable
    }

    private lateinit var slidesView: SlideBetweenView
    private lateinit var addressInput: InputView
    private lateinit var amountView: CoinInputView
    private lateinit var convertedView: AppCompatTextView
    private lateinit var swapView: View
    private lateinit var statusView: AppCompatTextView
    private lateinit var maxView: View
    private lateinit var commentInput: InputView
    private lateinit var button: Button
    private lateinit var taskContainerView: View
    private lateinit var addressActionsView: View
    private lateinit var confirmButton: Button
    private lateinit var processTaskView: ProcessTaskView
    private lateinit var reviewIconView: FrescoView
    private lateinit var reviewTitleView: AppCompatTextView
    private lateinit var reviewWalletView: TransactionDetailView
    private lateinit var reviewRecipientView: TransactionDetailView
    private lateinit var reviewRecipientAddressView: TransactionDetailView
    private lateinit var reviewRecipientAmountView: TransactionDetailView
    private lateinit var reviewRecipientFeeView: TransactionDetailView
    private lateinit var reviewRecipientCommentView: TransactionDetailView
    private lateinit var reviewSubtitleView: AppCompatTextView
    private lateinit var convertedContainerView: View
    private lateinit var commentEncryptView: AppCompatTextView
    private lateinit var commentDecryptView: AppCompatTextView
    private lateinit var commentRequiredView: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsHelper.trackEvent("send_open")
        navigation?.setFragmentResultListener(signerQRRequestKey) { bundle ->
            val sign = bundle.getString(SignerQRScreen.KEY_URI)?.toUri()?.getQueryParameter("sign")
            if (sign != null) {
                setLoading()
                viewModel.sendSignedMessage(BitString(sign))
            }
        }

        navigation?.setFragmentResultListener(contractsRequestKey) { bundle ->
            val contact = bundle.getParcelableCompat<SendContact>("contact") ?: return@setFragmentResultListener
            addressInput.text = contact.address
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        slidesView = view.findViewById(R.id.slides)
        val createHeaderView = view.findViewById<HeaderView>(R.id.create_header)
        createHeaderView.closeView.background = null
        createHeaderView.doOnActionClick = { finish() }
        createHeaderView.doOnCloseClick = { openCamera() }

        val reviewHeaderView = view.findViewById<HeaderView>(R.id.review_header)
        reviewHeaderView.doOnCloseClick = { showCreate() }

        addressInput = view.findViewById(R.id.address)

        addressActionsView = view.findViewById(R.id.address_actions)

        addressInput.doOnTextChange = { text ->
            reviewRecipientFeeView.setLoading()
            addressInput.loading = true
            viewModel.userInputAddress(text)
            addressActionsView.visibility = if (text.isBlank()) View.VISIBLE else View.GONE
        }

        view.findViewById<View>(R.id.paste).setOnClickListener {
            addressInput.text = requireContext().clipboardText()
        }

        view.findViewById<View>(R.id.address_book).setOnClickListener { openAddressBook() }

        amountView = view.findViewById(R.id.amount)
        amountView.doOnValueChanged = viewModel::userInputAmount
        amountView.doOnTokenChanged = viewModel::userInputToken
        amountView.setOnDoneActionListener { commentInput.requestFocus() }

        convertedView = view.findViewById(R.id.converted)
        convertedView.setOnClickListener { viewModel.swap() }

        swapView = view.findViewById(R.id.swap)
        swapView.setOnClickListener { viewModel.swap() }

        statusView = view.findViewById(R.id.status)

        maxView = view.findViewById(R.id.max)
        maxView.setOnClickListener { setMax() }

        commentInput = view.findViewById(R.id.comment)

        commentEncryptView = view.findViewById(R.id.comment_encrypt)
        commentEncryptView.movementMethod = LinkMovementMethod.getInstance()
        applyCommentEncryptView()

        commentDecryptView = view.findViewById(R.id.comment_decrypt)
        commentDecryptView.movementMethod = LinkMovementMethod.getInstance()
        applyCommentDecryptView()

        commentRequiredView = view.findViewById(R.id.comment_required)

        commentInput.doOnTextChange = { text ->
            if (text.isEmpty()) {
                commentEncryptView.visibility = View.GONE
                commentDecryptView.visibility = View.GONE
                commentInput.hintColor = requireContext().textSecondaryColor
            } else if (viewModel.uiEncryptedCommentAvailableFlow.value && viewModel.uiInputEncryptedComment.value) {
                commentDecryptView.visibility = View.VISIBLE
                commentInput.hintColor = requireContext().accentGreenColor
            } else if (viewModel.uiEncryptedCommentAvailableFlow.value) {
                commentEncryptView.visibility = View.VISIBLE
                commentInput.hintColor = requireContext().textSecondaryColor
            }
            viewModel.userInputComment(text)
        }

        button = view.findViewById(R.id.button)
        button.setOnClickListener { next() }

        reviewIconView = view.findViewById(R.id.review_icon)
        reviewTitleView = view.findViewById(R.id.review_title)
        reviewSubtitleView = view.findViewById(R.id.review_subtitle)
        reviewWalletView = view.findViewById(R.id.review_wallet)
        reviewRecipientView = view.findViewById(R.id.review_recipient)
        reviewRecipientAddressView = view.findViewById(R.id.review_recipient_address)
        reviewRecipientAmountView = view.findViewById(R.id.review_amount)
        reviewRecipientFeeView = view.findViewById(R.id.review_fee)
        reviewRecipientCommentView = view.findViewById(R.id.review_comment)

        taskContainerView = view.findViewById(R.id.task_container)

        confirmButton = view.findViewById(R.id.confirm_button)

        processTaskView = view.findViewById(R.id.process_task)

        convertedContainerView = view.findViewById(R.id.converted_container)

        if (args.isNft) {
            amountView.visibility = View.GONE
            convertedContainerView.visibility = View.GONE
        }

        view.doKeyboardAnimation { offset, _, _ ->
            button.translationY = -offset.toFloat()
            taskContainerView.translationY = -offset.toFloat()
        }

        collectFlow(viewModel.uiInputAddressErrorFlow) {
            addressInput.error = it
            addressInput.loading = false
        }

        collectFlow(viewModel.uiEventFlow, ::onEvent)
        collectFlow(viewModel.uiInputAmountFlow.map { it.value }, amountView::setValue)
        collectFlow(viewModel.uiBalanceFlow, ::setAmountState)
        collectFlow(viewModel.uiInputTokenFlow, ::setToken)
        collectFlow(viewModel.uiInputNftFlow, ::setNft)
        collectFlow(viewModel.uiButtonEnabledFlow, button::setEnabled)
        collectFlow(viewModel.uiTransactionFlow, ::applyTransaction)
        collectFlow(viewModel.walletTypeFlow) { walletType ->
            if (walletType == Wallet.Type.Default || walletType == Wallet.Type.Testnet || walletType == Wallet.Type.Lockup) {
                confirmButton.setText(Localization.confirm)
                confirmButton.setOnClickListener { confirmDefault() }
                return@collectFlow
            }
            confirmButton.setText(Localization.continue_action)
            when (walletType) {
                Wallet.Type.Ledger -> confirmButton.setOnClickListener { openLedger() }
                Wallet.Type.SignerQR -> confirmButton.setOnClickListener { openSignerQR() }
                Wallet.Type.Signer -> confirmButton.setOnClickListener { openSigner() }
                else -> throw IllegalArgumentException("Unsupported wallet type: $walletType")
            }
        }

        collectFlow(viewModel.uiInputEncryptedComment, ::applyCommentEncryptState)
        collectFlow(viewModel.uiRequiredMemoFlow) { memoRequired ->
            if (memoRequired) {
                commentRequiredView.visibility = View.VISIBLE
                commentInput.hint = getString(Localization.required_comment)
            } else {
                commentInput.hint = getString(Localization.comment)
                commentRequiredView.visibility = View.GONE
            }
        }

        initializeArgs(args.targetAddress, args.amountNano, args.text, args.tokenAddress)
    }

    private fun openAddressBook() {
        navigation?.add(SendContactsScreen.newInstance(contractsRequestKey))
        getCurrentFocus()?.hideKeyboard()
    }

    private fun openCamera() {
        navigation?.add(CameraFragment.newInstance())
        getCurrentFocus()?.hideKeyboard()
    }

    fun initializeArgs(
        targetAddress: String?,
        amountNano: Long,
        text: String?,
        tokenAddress: String
    ) {
        viewModel.initializeTokenAndAmount(
            tokenAddress = tokenAddress,
            amountNano = amountNano,
        )

        text?.let { commentInput.text = it }
        targetAddress?.let { addressInput.text = it }
    }

    private fun applyCommentEncryptState(enabled: Boolean) {
        val textIsEmpty = commentInput.text.isBlank()
        val textSecondaryColor = requireContext().textSecondaryColor
        if (enabled) {
            commentInput.hint = getString(Localization.encrypted_comment)
            val greenColor = requireContext().accentGreenColor
            if (textIsEmpty) {
                commentInput.hintColor = textSecondaryColor
            } else {
                commentInput.hintColor = greenColor
            }
            commentInput.activeBorderColor = greenColor
            if (!textIsEmpty) {
                commentDecryptView.visibility = View.VISIBLE
                commentEncryptView.visibility = View.GONE
            }
        } else {
            commentInput.hint = getString(Localization.comment)
            commentInput.hintColor = textSecondaryColor
            commentInput.activeBorderColor = requireContext().fieldActiveBorderColor
            if (!textIsEmpty) {
                commentDecryptView.visibility = View.GONE
                commentEncryptView.visibility = View.VISIBLE
            }
        }

        if (textIsEmpty) {
            commentDecryptView.visibility = View.GONE
            commentEncryptView.visibility = View.GONE
        }
    }

    private fun applyCommentDecryptView() {
        val hint = getString(Localization.comment_encrypted_hint)
        val button = getString(Localization.decrypt_comment)
        val text = "$hint $button"
        val spannableString = SpannableString(text)
        spannableString.setSpan(ClickableSpanCompat(requireContext().accentBlueColor) {
            viewModel.userInputEncryptedComment(false)
        }, hint.length + 1, text.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        commentDecryptView.text = spannableString
    }

    private fun applyCommentEncryptView() {
        val hint = getString(Localization.comment_decrypted_hint)
        val button = getString(Localization.encrypt_comment)
        val text = "$hint $button"
        val spannableString = SpannableString(text)
        spannableString.setSpan(ClickableSpanCompat(requireContext().accentBlueColor) {
            viewModel.userInputEncryptedComment(true)
        }, hint.length + 1, text.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        commentEncryptView.text = spannableString
    }

    private fun onEvent(event: SendEvent) {
        when (event) {
            is SendEvent.Failed -> setFailed()
            is SendEvent.Success -> setSuccess()
            is SendEvent.Loading -> setLoading()
            is SendEvent.Fee -> setFee(event)
            is SendEvent.InsufficientBalance -> showInsufficientBalance()
            is SendEvent.Confirm -> slidesView.next()
        }
    }

    private fun requestLedgerSign(transaction: Transaction, walletId: String) {
        val requestKey = "ledger_sign_request"
        navigation?.setFragmentResultListener(requestKey) { bundle ->
            processTaskView.state = ProcessTaskView.State.LOADING
            val result = bundle.getByteArray(LedgerSignScreen.SIGNED_MESSAGE)
            if (result == null) {
                setDefault()
            } else {
                setLoading()
                viewModel.sendLedgerSignedMessage(BagOfCells(result).first())
            }
        }
        navigation?.add(LedgerSignScreen.newInstance(transaction, walletId, requestKey))
    }

    private fun next() {
        setFee(null)
        addressInput.hideKeyboard()
        viewModel.next()
    }

    private fun showInsufficientBalance() {
        InsufficientBalanceDialog(requireContext()).show()
    }

    private fun setLoading() {
        confirmButton.visibility = View.GONE
        processTaskView.visibility = View.VISIBLE
        processTaskView.state = ProcessTaskView.State.LOADING
    }

    private fun setFailed() {
        processTaskView.state = ProcessTaskView.State.FAILED
        postDelayed(4000, ::setDefault)
    }

    private fun setSuccess() {
        processTaskView.state = ProcessTaskView.State.SUCCESS
        navigation?.openURL("tonkeeper://activity")
        postDelayed(2000, ::finish)
    }

    private fun setDefault() {
        confirmButton.visibility = View.VISIBLE
        processTaskView.visibility = View.GONE
        processTaskView.state = ProcessTaskView.State.LOADING
    }

    private fun showCreate() {
        slidesView.prev()
    }

    private fun applyTransaction(transaction: SendTransaction) {
        reviewWalletView.value = transaction.fromWallet.label.getTitle(requireContext(), reviewWalletView.valueView)
        applyTransactionAccount(transaction.destination)
        applyTransactionAmount(transaction.amount)
        applyTransactionComment(transaction.comment, transaction.encryptedComment)
    }

    private fun applyTransactionComment(comment: String?, encryptedComment: Boolean) {
        if (comment.isNullOrEmpty()) {
            reviewRecipientCommentView.visibility = View.GONE
            return
        }
        reviewRecipientCommentView.visibility = View.VISIBLE
        reviewRecipientCommentView.value = comment
        if (encryptedComment) {
            reviewRecipientCommentView.setTitleRightDrawable(lockDrawable)
        } else {
            reviewRecipientCommentView.setTitleRightDrawable(null)
        }
    }

    private fun applyTransactionAmount(amount: SendTransaction.Amount) {
        if (!amount.value.isPositive) {
            reviewRecipientAmountView.visibility = View.GONE
            return
        }
        reviewRecipientAmountView.visibility = View.VISIBLE
        reviewRecipientAmountView.value = amount.format.withCustomSymbol(requireContext())
        reviewRecipientAmountView.description = amount.convertedFormat.withCustomSymbol(requireContext())
    }

    private fun applyTransactionAccount(destination: SendDestination.Account) {
        val shortAddress = destination.query.shortAddress

        if (destination.name.isNullOrEmpty()) {
            reviewRecipientView.value = shortAddress
            reviewRecipientAddressView.visibility = View.GONE
        } else {
            reviewRecipientView.value = destination.name
            reviewRecipientAddressView.visibility = View.VISIBLE
            reviewRecipientAddressView.value = shortAddress
        }
    }

    private fun setFee(event: SendEvent.Fee?) {
        if (event == null) {
            reviewRecipientFeeView.setLoading()
            confirmButton.isEnabled = false
        } else {
            reviewRecipientFeeView.value = "≈ ${event.format}".withCustomSymbol(requireContext())
            reviewRecipientFeeView.description = "≈ ${event.convertedFormat}".withCustomSymbol(requireContext())
            reviewRecipientFeeView.subtitle = if (event.isBattery) getString(Localization.will_be_paid_with_battery) else null
            reviewRecipientFeeView.setDefault()
            confirmButton.isEnabled = true
        }
    }

    private fun confirmDefault() {
        setLoading()
        viewModel.send(requireContext())
    }

    private fun openLedger() {
        collectFlow(viewModel.ledgerData()) { (walletId, transaction) ->
            requestLedgerSign(transaction, walletId)
        }
    }

    private fun openSignerQR() {
        AnalyticsHelper.trackEvent("send_transaction")

        var text = commentInput.text
        if (text.isEmpty()) {
            text = reviewRecipientView.value?.toString() ?: "..."
        }

        collectFlow(viewModel.signerData()) { (publicKey, unsignedBody) ->
            navigation?.add(SignerQRScreen.newInstance(publicKey, unsignedBody, text, signerQRRequestKey))
        }
    }

    private fun openSigner() {
        AnalyticsHelper.trackEvent("send_transaction")
        collectFlow(viewModel.signerData()) { (publicKey, unsignedBody) ->
            signerLauncher.launch(SingerResultContract.Input(unsignedBody, publicKey))
        }
    }

    private fun setMax() {
        viewModel.setMax()
        amountView.hideKeyboard()
    }

    private fun setAmountState(state: SendAmountState) {
        convertedView.text = state.convertedFormat.withCustomSymbol(requireContext())
        amountView.suffix = state.currencyCode

        if (state.insufficientBalance) {
            statusView.setTextColor(requireContext().fieldErrorBorderColor)
            statusView.setText(Localization.insufficient_balance)
            maxView.visibility = View.GONE
        } else {
            statusView.setTextColor(requireContext().textSecondaryColor)
            statusView.text = state.remainingFormat.withCustomSymbol(requireContext())
            maxView.visibility = View.VISIBLE
        }
    }

    private fun setToken(token: TokenEntity) {
        amountView.setToken(token)
        reviewIconView.setImageURI(token.imageUri, null)
        reviewSubtitleView.text = getString(Localization.jetton_transfer, token.symbol)
    }

    private fun setNft(nft: NftEntity) {
        reviewIconView.setRound(20f.dp)
        reviewIconView.setImageURI(nft.mediumUri, null)
        reviewSubtitleView.setText(Localization.nft_transfer)
    }

    override fun onDragging() {
        super.onDragging()
        context?.hideKeyboard()
    }

    companion object {

        fun newInstance(
            targetAddress: String? = null,
            tokenAddress: String = TokenEntity.TON.address,
            amountNano: Long = 0,
            text: String? = null,
            nftAddress: String? = null
        ): SendScreen {
            val screen = SendScreen()
            screen.setArgs(SendArgs(targetAddress, tokenAddress, amountNano, text, nftAddress ?: ""))
            return screen
        }

        private class InsufficientBalanceDialog(
            context: Context
        ): ModalDialog(context, R.layout.dialog_insufficient_balance) {

            init {
                findViewById<HeaderView>(R.id.header)?.doOnActionClick = { dismiss() }
                findViewById<View>(R.id.ok)?.setOnClickListener { dismiss() }
            }
        }
    }
}