package com.tonapps.tonkeeper.ui.screen.swap

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.swap.view.inputItem.InputItemModel
import com.tonapps.tonkeeper.ui.screen.swap.view.inputItem.InputItemView
import com.tonapps.tonkeeper.ui.screen.swap.view.inputItem.ProgressButtonState
import com.tonapps.tonkeeper.ui.screen.swap.view.inputItem.ReceiveInputItemView
import com.tonapps.tonkeeper.ui.screen.swap.view.inputItem.SendInputItemView
import com.tonapps.tonkeeper.ui.screen.swap.model.assets.Asset
import com.tonapps.tonkeeper.ui.screen.swap.view.progressButton.ProgressButton
import com.tonapps.tonkeeper.ui.screen.swap.screens.choseToken.ChoseTokenScreen
import com.tonapps.tonkeeper.ui.screen.swap.view.swapView.SwapView
import com.tonapps.tonkeeperx.BuildConfig
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.getDimensionPixelSize
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.webview.bridge.BridgeWebView

class SwapScreen : BaseFragment(R.layout.fragment_swap), BaseFragment.BottomSheet {

    private val args: SwapArgs by lazy { SwapArgs(requireArguments()) }

    private val rootViewModel: RootViewModel by activityViewModel()
    private val swapViewModel: SwapViewModel by activityViewModel()

    private lateinit var webView: BridgeWebView
    private lateinit var headerView: HeaderView
    private lateinit var inputItemView: InputItemView
    private lateinit var button: ProgressButton
    private lateinit var swapView: SwapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        swapViewModel.getToken(args.address)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // init view
        headerView = view.findViewById(R.id.header)
        webView = view.findViewById(R.id.web)
        inputItemView = view.findViewById(R.id.inputItemView)
        button = view.findViewById(R.id.progressButton)
        swapView = view.findViewById(R.id.swapView)
        //

        button.setUpLoading(false)

        setUpControlToEditText()

        if (swapViewModel.sendToken.value.token == null) {
            headerView.setSubtitle(resources.getString(com.tonapps.wallet.localization.R.string.loading))
        }
        headerView.contentMatchParent()
        headerView.doOnActionClick = { finish() }

        inputItemView.buttonChangeTokenHandler = {
            swapViewModel.changeToken()
        }

        lifecycleScope.launch {


            val stateButtonProgressJob = launch {
                swapViewModel.stateProgressButton.collectLatest {
                    when (it) {
                        ProgressButtonState.StateEnterAmount -> {
                            updateStateProgress(
                                enabledButton = false,
                                textButton = "EnterAmount",
                                statusDopContent = false,
                                loadingState = false
                            )
                        }

                        ProgressButtonState.StateChooseToken -> {
                            updateStateProgress(
                                enabledButton = false,
                                textButton = "ChooseToken",
                                statusDopContent = false,
                                loadingState = false
                            )
                        }

                        ProgressButtonState.StateContinue -> {
                            updateStateProgress(
                                enabledButton = true,
                                textButton = "Continue",
                                statusDopContent = true,
                                loadingState = false
                            )
                        }

                        ProgressButtonState.StateLoading -> {
                            updateStateProgress(
                                enabledButton = false,
                                textButton = "",
                                statusDopContent = false,
                                loadingState = true
                            )
                        }

                        ProgressButtonState.StateInsufficientBalance -> {
                            updateStateProgress(
                                enabledButton = false,
                                textButton = "Insufficient Balance. Buy $${swapViewModel.sendToken.value.token?.symbol}",
                                statusDopContent = false,
                                loadingState = false
                            )
                        }
                    }
                }
            }

            val tokenListJob = launch {
                swapViewModel.tokenList.collectLatest {
                    if (swapViewModel.clickState) {
                        inputItemView.setChooseViewTypReceive()
                    }
                }
            }

            val sendTokenJob = launch {
                swapViewModel.sendToken.collectLatest {
                    if (swapViewModel.clickState) {
                        inputItemView.setUpInputFieldValueSend(it.sendValue)
                        checkBalanceAbility(swapViewModel.receiveToken.value.token, it.token)
                        if (it.token != null) {
                            headerView.setSubtitle("")
                            val balance =
                                if (it.token.balance != null) it.token.balance.toInt() else 0

                            inputItemView.loadItemSendDataToken(
                                InputItemModel(
                                    uriImage = it.token.image_url.toUri(),
                                    nameToken = it.token.symbol,
                                    balance = balance
                                )
                            )
                        } else {
                            inputItemView.loadItemSendDataToken(null)
                        }
                    }
                }
            }

            val receiveTokenJob = launch {
                swapViewModel.receiveToken.collectLatest {
                    if (swapViewModel.clickState) {
                        inputItemView.setUpInputFieldValueReceive(it.sendValue)
                        checkBalanceAbility(it.token, swapViewModel.sendToken.value.token)
                        if (it.token != null) {
                            val balance =
                                if (it.token.balance != null) it.token.balance.toInt() else 0

                            inputItemView.loadItemReceiveDataToken(
                                InputItemModel(
                                    uriImage = it.token.image_url.toUri(),
                                    nameToken = it.token.symbol,
                                    balance = balance
                                )
                            )

                            if(swapViewModel.sendToken.value != null) {
                                swapView.setOfferAmount("1000000000")
                                swapView.setAskJettonAddress("EQA2kCVNwVsil2EM2mB0SkXytxCqQjS4mttjDpnXmwG9T6bO")
                            }

                        } else {
                            inputItemView.loadItemReceiveDataToken(null)
                        }
                    }
                }
            }
            tokenListJob.join()
            sendTokenJob.join()
            receiveTokenJob.join()
            stateButtonProgressJob.join()
        }

        webView.clipToPadding = false
        webView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))
        webView.loadUrl(getUri().toString())
        webView.jsBridge = StonfiBridge2(
            address = args.address,
            close = ::finish,
            sendTransaction = ::sing
        )

        inputItemView.setUpClickHandlerToSendItem(itemClick = {
            val otherToken = swapViewModel.receiveToken.value.token
            val tokenList = swapViewModel.tokenList.value.toMutableList()
            if (otherToken != null) {
                tokenList.remove(otherToken)
            }
            val sendToken = swapViewModel.sendToken.value
            if (tokenList.isNotEmpty()) {
                navigation?.add(
                    ChoseTokenScreen.newInstance(
                        listItemToken = tokenList,
                        selectedToken = sendToken.token,
                        type = true
                    )
                )
            }
        }, buttonMaxClick = {
            val sendTokenResult = swapViewModel.sendToken.value
            if (sendTokenResult.token != null) {
                if (sendTokenResult.token.balance != null) {
                    inputItemView.setUpInputFieldValueSend(sendTokenResult.token.balance.toInt())
                }
            }
        })

        inputItemView.setUpClickHandlerToReceiveItem {
            val otherToken = swapViewModel.sendToken.value.token
            val tokenList = swapViewModel.tokenList.value.toMutableList()
            tokenList.remove(otherToken)
            val receiveToken = swapViewModel.receiveToken.value
            if (tokenList.isNotEmpty()) {
                navigation?.add(
                    ChoseTokenScreen.newInstance(
                        listItemToken = tokenList,
                        selectedToken = receiveToken.token,
                        type = false
                    )
                )
            }
        }

    }


    private fun setUpControlToEditText() {
        inputItemView.getSendEditText().addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.isNotEmpty()) {
                        val receiveToken = swapViewModel.receiveToken.value.token
                        val sendToken = swapViewModel.sendToken.value.token
                        checkBalanceAbility(receiveToken, sendToken)
                    }
                }
            }
        })
    }

    private fun checkBalanceAbility(receiveToken: Asset?, sendToken: Asset?) {

        val sendValueItem = inputItemView.getSendEditText().text.toString().toDouble()
        if(sendToken != null) {
            if (sendValueItem != 0.0) {
                val balanceSendToken = sendToken.balance?.toIntOrNull()
                if (balanceSendToken == null) {
                    swapViewModel.updateStateProgressButton(ProgressButtonState.StateInsufficientBalance)
                } else {
                    if (balanceSendToken >= sendValueItem.toNanoCoin()) {
                        if(receiveToken != null) {
                            swapViewModel.updateStateProgressButton(ProgressButtonState.StateContinue)
                        } else {
                            swapViewModel.updateStateProgressButton(ProgressButtonState.StateChooseToken)
                        }
                    } else {
                        swapViewModel.updateStateProgressButton(ProgressButtonState.StateInsufficientBalance)
                    }
                }
            } else {
                swapViewModel.updateStateProgressButton(ProgressButtonState.StateEnterAmount)
            }
        } else {
            swapViewModel.updateStateProgressButton(ProgressButtonState.StateChooseToken)
        }
    }

    private fun updateStateProgress(
        enabledButton: Boolean,
        textButton: String,
        statusDopContent: Boolean,
        loadingState: Boolean
    ) {
        button.updateTextInButton(textButton)
        button.updateStateEnabledButton(enabledButton)
        inputItemView.updateStatusVisibleDopContent(statusDopContent)
        button.setUpLoading(loadingState)
    }

    private fun getUri(): Uri {
        val builder = args.uri.buildUpon()
        builder.appendQueryParameter("clientVersion", BuildConfig.VERSION_NAME)
        builder.appendQueryParameter("ft", args.fromToken)
        args.toToken?.let {
            builder.appendQueryParameter("tt", it)
        }
        return builder.build()
    }

    private suspend fun sing(
        request: SignRequestEntity
    ): String {
        return rootViewModel.requestSign(requireContext(), request)
    }

    override fun onDestroyView() {
        webView.destroy()
        swapViewModel.clearDataViewModel()
        super.onDestroyView()
    }

    companion object {

        fun newInstance(
            uri: Uri,
            address: String,
            fromToken: String,
            toToken: String? = null,
        ): SwapScreen {
            val fragment = SwapScreen()
            fragment.arguments = SwapArgs(uri, address, fromToken, toToken).toBundle()
            return fragment
        }
    }
}