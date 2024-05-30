package com.tonapps.tonkeeper.ui.screen.swapnative.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.StringFormatter
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.ui.screen.buysell.main.BuySellScreen
import com.tonapps.tonkeeper.ui.screen.swapnative.SwapBaseScreen
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.ChooseTokenScreen
import com.tonapps.tonkeeper.ui.screen.swapnative.confirm.SwapConfirmScreen
import com.tonapps.tonkeeper.ui.screen.swapnative.main.SwapNativeViewModel.Companion.DEFAULT_INPUT_AMOUNT_VALUE
import com.tonapps.tonkeeper.ui.screen.swapnative.settings.SwapSettingsScreen
import com.tonapps.tonkeeper.view.SwapFromContainerView
import com.tonapps.tonkeeper.view.SwapToContainerView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.hideKeyboard
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.ProgressButton
import uikit.widget.SkeletonLayout

class SwapNativeScreen : SwapBaseScreen(R.layout.fragment_swap_native), BaseFragment.BottomSheet,
    TokenSelectionListener, SlippageSelectionListener, SwapConfirmListener {

    private val swapNativeViewModel: SwapNativeViewModel by viewModel()

    private val args: SwapNativeArgs by lazy { SwapNativeArgs(requireArguments()) }

    private lateinit var headerView: HeaderView
    private lateinit var nextButton: ProgressButton
    private lateinit var mainView: View
    private lateinit var skeletonView: SkeletonLayout

    private lateinit var swapFromContainerView: SwapFromContainerView
    private lateinit var swapToContainerView: SwapToContainerView

    private lateinit var switchTokens: AppCompatImageView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        handleViews()
        handleViewModel()

        // swapNativeViewModel.getRemoteAssets()

    }

    private fun initializeViews(view: View) {
        headerView = view.findViewById(R.id.header)
        nextButton = view.findViewById(R.id.continue_progress_button)
        /*webView.clipToPadding = false
        webView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))*/
        // listView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.cornerMedium))

        swapFromContainerView = view.findViewById(R.id.swap_from_container)
        swapToContainerView = view.findViewById(R.id.swap_to_container)

        mainView = view.findViewById(R.id.main_view)
        skeletonView = view.findViewById(R.id.skeleton_view)

        switchTokens = view.findViewById(R.id.switch_tokens)
    }

    private fun handleViews() {

        postDelayed(2000) {
            displayMainView()
        }

        // headerView.contentMatchParent()
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = {
            SwapSettingsScreen.newInstance(swapNativeViewModel.selectedSlippageFlow.value)
                .also { screen ->
                    postDelayed(500) { getCurrentFocus()?.hideKeyboard() }
                    screen.setBottomSheetDismissListener(this)
                    navigation?.add(screen)
                }
        }

        swapFromContainerView.doOnFromAssetItemClick = {
            ChooseTokenScreen.newInstance(null).also { chooseTokenScreen ->
                postDelayed(500) { getCurrentFocus()?.hideKeyboard() }
                chooseTokenScreen.setBottomSheetDismissListener(this)
                navigation?.add(chooseTokenScreen)
            }
        }

        swapFromContainerView.doOnMaxBalanceClick = {
            swapFromContainerView.sellAmountInput.setText(
                (swapNativeViewModel.selectedFromToken.value?.balance ?: 0).toString()
            )
        }

        swapFromContainerView.doAfterFromAmountInputTextChanged = {
            swapNativeViewModel.onFromAmountChanged(
                if (it.isNullOrEmpty() || it.isBlank()) DEFAULT_INPUT_AMOUNT_VALUE
                else it.toString()
            )
        }

        swapToContainerView.doOnToAssetItemClick = {
            swapNativeViewModel.selectedFromToken.value?.also {
                ChooseTokenScreen.newInstance(it.contractAddress).also { chooseTokenScreen ->
                    postDelayed(500) { getCurrentFocus()?.hideKeyboard() }
                    chooseTokenScreen.setBottomSheetDismissListener(this)
                    navigation?.add(chooseTokenScreen)
                }
            }
        }
        swapToContainerView.doAfterToAmountInputTextChanged = {
            swapNativeViewModel.onToAmountChanged(
                if (it.isNullOrEmpty() || it.isBlank()) DEFAULT_INPUT_AMOUNT_VALUE
                else it.toString()
            )
        }
        swapToContainerView.onPriceImpactInfoClicked = { view, message ->
            navigation?.toast(message)
        }
        swapToContainerView.onMinReceivedInfoClicked = { view, message ->
            navigation?.toast(message)
        }
        swapToContainerView.onProviderFeeInfoClicked = { view, message ->
            navigation?.toast(message)
        }


        switchTokens.setOnClickListener {
            switchTokens()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            swapNativeViewModel.selectedFromToken.collect { fromAsset ->
                swapFromContainerView.apply {

                    swapNativeViewModel.isProgrammaticSet = true
                    swapFromContainerView.sellAmountInput.setText("")
                    swapToContainerView.buyAmountInput.setText("")
                    swapNativeViewModel.isProgrammaticSet = false

                    if (fromAsset == null) {
                        // reset
                        postDelayed(ANIMATE_LAYOUT_CHANGE_DELAY) {
                            sellTokenTitle.setText(getString(Localization.choose))
                            sellTokenIcon.visibility = View.GONE
                        }
                        sellTokenIcon.clear(requireContext())

                        sellTokenBalance.text = "0"
                        sellTokenBalance.visibility = View.GONE
                        selectMaxSellBalance.visibility = View.GONE

                        swapFromContainerView.isInputEnabled = false
                    } else {
                        displayMainView()

                        postDelayed(ANIMATE_LAYOUT_CHANGE_DELAY) {
                            sellTokenTitle.setText(fromAsset.symbol)
                            //sellTokenTitle.text = fromAsset.symbol
                            sellTokenIcon.visibility = View.VISIBLE

                            sellTokenBalance.text =
                                if (fromAsset.hiddenBalance)
                                    "${getString(Localization.balance)} $HIDDEN_BALANCE"
                                else if (fromAsset.balance == 0.0) {
                                    String.format(getString(Localization.balance_format_int), 0)
                                } else {
                                    String.format(
                                        getString(Localization.balance_format),
                                        fromAsset.balance
                                    )
                                }
                            sellTokenBalance.visibility = View.VISIBLE

                            selectMaxSellBalance.visibility = View.VISIBLE
                        }

                        sellTokenIcon.setImageURI(fromAsset.imageUrl?.toUri())

                        swapFromContainerView.isInputEnabled = true
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            swapNativeViewModel.selectedToToken.collect { toAsset ->
                swapToContainerView.apply {
                    if (toAsset == null) {
                        // reset
                        postDelayed(ANIMATE_LAYOUT_CHANGE_DELAY) {
                            buyTokenTitle.text =
                                getString(Localization.choose)
                            buyTokenIcon.visibility = View.GONE
                        }
                        buyTokenIcon.clear(requireContext())

                        swapNativeViewModel.isProgrammaticSet = true
                        swapToContainerView.buyAmountInput.setText("")
                        swapNativeViewModel.isProgrammaticSet = false

                        buyTokenBalance.text = "0"
                        buyTokenBalance.visibility = View.GONE

                        swapToContainerView.isInputEnabled = false
                    } else {
                        // populate
                        postDelayed(ANIMATE_LAYOUT_CHANGE_DELAY) {
                            buyTokenTitle.text = toAsset.symbol
                            buyTokenIcon.visibility = View.VISIBLE

                            buyTokenBalance.text = if (toAsset.hiddenBalance)
                                "${getString(com.tonapps.wallet.localization.R.string.balance)} $HIDDEN_BALANCE"
                            else if (toAsset.balance == 0.0) {
                                String.format(getString(Localization.balance_format_int), 0)
                            } else {
                                String.format(
                                    getString(Localization.balance_format),
                                    toAsset.balance
                                )
                            }
                            buyTokenBalance.visibility = View.VISIBLE
                        }

                        buyTokenIcon.setImageURI(toAsset.imageUrl?.toUri())

                        swapToContainerView.isInputEnabled = true
                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            swapNativeViewModel.swapDetailsFlow.collect { swapSimulateResult ->

                swapToContainerView.apply {
                    swapDetailContainer.visibility = View.GONE

                    swapSimulateResult?.swapSimulateEntity?.apply {

                        val sellSymbol = swapNativeViewModel.selectedFromToken.value?.symbol ?: ""
                        val buySymbol = swapNativeViewModel.selectedToToken.value?.symbol ?: ""

                        val priceImpactFloat = getPriceImpactAsFloat()
                        val priceImpactColor = getPriceImpactColor(priceImpactFloat)

                        swapTitleTv.text = "1 ${sellSymbol} ≈ ${
                            StringFormatter.truncateToFourDecimalPlaces(swapRate)
                        } ${buySymbol}"
                        swapTitleTv.setTextColor(priceImpactColor)
                        priceImpactTv.text = getFormattedPriceImpact()
                        priceImpactTv.setTextColor(priceImpactColor)
                        minReceivedTv.text = "${
                            Coin.parseBigInt(minAskUnits.toString(), toDecimals!!).toPlainString()
                        }"
                        providerFeeTv.text = "${
                            Coin.parseBigInt(feeUnits.toString(), toDecimals!!).toPlainString()
                        } ${buySymbol}"
                        blockchainFeeTv.text = "0.08 - 0.25 TON"
                        routeTv.text = "${sellSymbol.uppercase()} » ${buySymbol.uppercase()}"

                        swapDetailContainer.visibility = View.VISIBLE

                        updateInputAmountsFromApi(
                            Coin.parseBigInt(offerUnits.toString(), fromDecimals!!, false)
                                .toPlainString(),
                            Coin.parseBigInt(askUnits.toString(), toDecimals!!, false)
                                .toPlainString(),
                            swapSimulateResult.isReverse
                        )
                    }

                }
            }
        }

        swapToContainerView.apply {
            swapTitleContaienr.setOnClickListener {

                val rotationAngle = if (swapDetailSubContainer.visibility == View.GONE) 0f else 180f
                toggleSwapDetails.animate()
                    .rotation(rotationAngle)
                    .setDuration(200)
                    .start()

                if (swapDetailSubContainer.visibility == View.GONE) {
                    swapDetailSubContainer.visibility = View.VISIBLE
                } else {
                    swapDetailSubContainer.visibility = View.GONE
                }
            }
        }

        nextButton.onClick = { view, isEnabled ->
            when (swapNativeViewModel.screenStateFlow.value.continueState) {
                is SwapNativeScreenState.ContinueState.INSUFFICIENT_TON_BALANCE -> {
                    navigation?.add(BuySellScreen.newInstance())
                    finish()
                }

                is SwapNativeScreenState.ContinueState.NEXT -> {
                    val swapConfirmArgs = swapNativeViewModel.generateConfirmArgs()
                    if (swapConfirmArgs != null) {
                        postDelayed(500) { getCurrentFocus()?.hideKeyboard() }

                        val screen = SwapConfirmScreen.newInstance(swapConfirmArgs)
                        screen.setSwapConfirmListener(this)
                        navigation?.add(screen)
                    } else {
                        // error
                    }
                }

                else -> {}
            }
        }

    }

    private fun newUiState(screenState: SwapNativeScreenState) = screenState.also { state ->
        nextButton.toggleProgressDisplay(state.continueState is SwapNativeScreenState.ContinueState.LOADING)
        state.continueState.text?.also { text ->

            if (state.continueState is SwapNativeScreenState.ContinueState.INSUFFICIENT_BALANCE) {
                nextButton.setText(
                    String.format(
                        getString(text),
                        swapNativeViewModel.selectedFromToken.value?.symbol ?: ""
                    )
                )
            } else {
                nextButton.setText(requireContext().getString(text))
            }
        }
        nextButton.setEnabled(state.continueState.enabled)

        state.showMainLoading.also { show ->
            mainView.isInvisible = show
            skeletonView.isVisible = show
            if (!show) {
                postDelayed(500) {
                    nextButton.isInvisible = false
                }
            } else nextButton.isInvisible = true
        }
    }

    private fun onEffect(screenEffect: SwapNativeScreenEffect) = screenEffect.also { effect ->
        when (effect) {
            is SwapNativeScreenEffect.Finish -> {
                if (effect.toast != null) {
                    navigation?.toast(getString(effect.toast))
                }
                finish()
            }
        }
    }

    private fun displayMainView() {
        // mainView.visibility = View.VISIBLE
        // loadingView.visibility = View.GONE
    }

    private fun updateInputAmountsFromApi(offerAmount: String, askAmount: String, isReverse: Boolean) {
        swapNativeViewModel.isProgrammaticSet = true
        if(isReverse) swapFromContainerView.sellAmountInput.setText(offerAmount)
        if(!isReverse) swapToContainerView.buyAmountInput.setText(askAmount)
        swapNativeViewModel.isProgrammaticSet = false
    }

    private fun switchTokens() {
        swapNativeViewModel.apply {
            stopPeriodicSwapSimulate()
            cancelPreviousSwapDetailRequests()

            val from = selectedFromToken.value
            val fromAmount = selectedFromTokenAmount.value
            val to = selectedToToken.value
            val toAmount = selectedToTokenAmount.value

            setSelectedFromToken(to, true)
            setSelectedToToken(from, true)

            swapNativeViewModel.isProgrammaticSet = true
            swapFromContainerView.sellAmountInput.setText(toAmount)
            swapToContainerView.buyAmountInput.setText(fromAmount)
            swapNativeViewModel.isProgrammaticSet = false

            triggerSimulateSwap(false)
        }
    }

    private fun handleViewModel() = swapNativeViewModel.apply {
        viewLifecycleOwner.lifecycleScope.launch {
            swapNativeViewModel.screenStateFlow.collect { screenState ->
                newUiState(screenState)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            swapNativeViewModel.screenEffectFlow.collect { effect ->
                onEffect(effect)
            }
        }
    }

    companion object {

        const val ANIMATE_LAYOUT_CHANGE_DELAY = 200L

        fun newInstance(
            /*uri: Uri,*/
            address: String,
            fromToken: String,
            toToken: String? = null
        ): SwapNativeScreen {
            val fragment = SwapNativeScreen()
            fragment.arguments = SwapNativeArgs(/*uri,*/ address, fromToken, toToken).toBundle()
            return fragment
        }
    }

    override fun onSellTokenSelected(contractAddress: String) {
        viewLifecycleOwner.lifecycleScope.launch {

            swapNativeViewModel.setSelectedFromToken(
                swapNativeViewModel.getAssetByAddress(
                    contractAddress
                )
            )

            // Check if buy token is swappable.
            if (swapNativeViewModel.selectedToToken.value != null) {
                val isBuyTokenSwappable =
                    swapNativeViewModel.selectedFromToken.value?.swapableAssets?.contains(
                        swapNativeViewModel.selectedToToken.value!!.contractAddress
                    ) ?: false

                if (!isBuyTokenSwappable)
                    swapNativeViewModel.setSelectedToToken(null)
            }

        }
    }

    override fun onBuyTokenSelected(contractAddress: String) {
        viewLifecycleOwner.lifecycleScope.launch {

            swapNativeViewModel.setSelectedToToken(
                swapNativeViewModel.getAssetByAddress(
                    contractAddress
                )
            )

        }
    }

    override fun onSlippageSelected(amount: Float) {
        swapNativeViewModel.selectedSlippageFlow.value = amount
    }

    override fun onSwapConfirmSuccess() {
        finish()
    }


}