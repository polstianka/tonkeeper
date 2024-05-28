package com.tonapps.tonkeeper.ui.screen.swapnative.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.StringFormatter
import com.tonapps.tonkeeper.core.signer.SingerResultContract
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.ui.screen.swapnative.confirm.SwapConfirmScreen
import com.tonapps.tonkeeper.ui.screen.swapnative.SwapBaseScreen
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.ChooseTokenScreen
import com.tonapps.tonkeeper.ui.screen.swapnative.settings.SwapSettingsScreen
import com.tonapps.tonkeeper.view.SwapFromContainerView
import com.tonapps.tonkeeper.view.SwapToContainerView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.hideKeyboard
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.ProgressButton

class SwapNativeScreen : SwapBaseScreen(R.layout.fragment_swap_native), BaseFragment.BottomSheet,
    TokenSelectionListener, SlippageSelectionListener, SwapConfirmListener {

    private val swapNativeViewModel: SwapNativeViewModel by viewModel()

    private val args: SwapNativeArgs by lazy { SwapNativeArgs(requireArguments()) }

    private lateinit var headerView: HeaderView
    private lateinit var nextButton: ProgressButton

    private lateinit var swapFromContainerView: SwapFromContainerView
    private lateinit var swapToContainerView: SwapToContainerView

    private lateinit var switchTokens: AppCompatImageView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        handleViews()
        handleViewModel()

        swapNativeViewModel.getRemoteAssets()

        nextButton.isEnabled = false

    }

    private fun initializeViews(view: View) {
        headerView = view.findViewById(R.id.header)
        nextButton = view.findViewById(R.id.continue_progress_button)

        swapFromContainerView = view.findViewById(R.id.swap_from_container)
        swapToContainerView = view.findViewById(R.id.swap_to_container)

        switchTokens = view.findViewById(R.id.switch_tokens)
    }

    private fun handleViews() {

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
                if (it.isNullOrEmpty()) "0"
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
                if (it.isNullOrEmpty()) "0"
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
                    if (fromAsset == null) {
                        // reset
                        postDelayed(ANIMATE_LAYOUT_CHANGE_DELAY) {
                            sellTokenTitle.setText(getString(com.tonapps.wallet.localization.R.string.choose))
                            sellTokenIcon.visibility = View.GONE
                        }
                        sellTokenIcon.clear(requireContext())

                        sellTokenBalance.text = "0"
                        sellTokenBalance.visibility = View.GONE
                        selectMaxSellBalance.visibility = View.GONE

                    } else {
                        // poppulate
                        postDelayed(ANIMATE_LAYOUT_CHANGE_DELAY) {
                            sellTokenTitle.setText(fromAsset.symbol)
                            //sellTokenTitle.text = fromAsset.symbol
                            sellTokenIcon.visibility = View.VISIBLE
                        }
                        sellTokenIcon.setImageURI(fromAsset.imageUrl?.toUri())

                        sellTokenBalance.text =
                            if (fromAsset.hiddenBalance)
                                "${getString(com.tonapps.wallet.localization.R.string.balance)} $HIDDEN_BALANCE"
                            else {
                                String.format(
                                    getString(com.tonapps.wallet.localization.R.string.balance_format),
                                    fromAsset.balance
                                )
                            }
                        sellTokenBalance.visibility = View.VISIBLE

                        selectMaxSellBalance.visibility = View.VISIBLE
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
                                getString(com.tonapps.wallet.localization.R.string.choose)
                            buyTokenIcon.visibility = View.GONE
                        }
                        buyTokenIcon.clear(requireContext())

                        swapNativeViewModel.isProgrammaticSet = true
                        swapToContainerView.buyAmountInput.setText("0")
                        swapNativeViewModel.isProgrammaticSet = false

                        buyTokenBalance.text = "0"
                        buyTokenBalance.visibility = View.GONE
                    } else {
                        // populate
                        postDelayed(ANIMATE_LAYOUT_CHANGE_DELAY) {
                            buyTokenTitle.text = toAsset.symbol
                            buyTokenIcon.visibility = View.VISIBLE
                        }
                        buyTokenIcon.setImageURI(toAsset.imageUrl?.toUri())

                        buyTokenBalance.text = if (toAsset.hiddenBalance)
                            "${getString(com.tonapps.wallet.localization.R.string.balance)} $HIDDEN_BALANCE"
                        else {
                            String.format(
                                getString(com.tonapps.wallet.localization.R.string.balance_format),
                                toAsset.balance
                            )
                        }
                        buyTokenBalance.visibility = View.VISIBLE
                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            swapNativeViewModel.swapDetailsFlow.collect { swapDetails ->

                swapToContainerView.apply {
                    swapDetailContainer.visibility = View.GONE

                    swapDetails?.apply {

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
                                .toPlainString()
                        )
                    }

                }
            }
        }

        swapToContainerView.apply {
            toggleSwapDetails.setOnClickListener {

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

        nextButton.setText(requireContext().getString(com.tonapps.wallet.localization.R.string.continue_action))
        nextButton.onClick = { view, isEnabled ->
            val swapConfirmArgs = swapNativeViewModel.generateConfirmArgs()
            if (swapConfirmArgs != null) {
                val screen = SwapConfirmScreen.newInstance(swapConfirmArgs)
                screen.setSwapConfirmListener(this)
                navigation?.add(screen)
            } else {
                // todo show error
            }
        }

    }

    private fun updateInputAmountsFromApi(offerAmount: String, askAmount: String) {
        swapNativeViewModel.isProgrammaticSet = true
        swapFromContainerView.sellAmountInput.setText(offerAmount)
        swapToContainerView.buyAmountInput.setText(askAmount)
        swapNativeViewModel.isProgrammaticSet = false
    }

    private fun switchTokens() {
        navigation?.toast("switch tokens!")
        // todo P handle simulate and edge cases!!!

        // TODO P disable submit button

        swapNativeViewModel.apply {
            val sell = selectedFromToken.value
            val sellAmount = selectedFromTokenAmount.value
            val buy = selectedToToken.value
            val buyAmount = selectedToTokenAmount.value

            setSelectedSellToken(buy)
            setSelectedBuyToken(sell)

            swapNativeViewModel.isProgrammaticSet = true
            swapFromContainerView.sellAmountInput.setText(buyAmount)
            swapToContainerView.buyAmountInput.setText(sellAmount)
            swapNativeViewModel.isProgrammaticSet = false
            triggerSimulateSwap(false)
        }

    }

    private fun handleViewModel() = swapNativeViewModel.apply {
        viewLifecycleOwner.lifecycleScope.launch {
            openSignerAppFlow.collect { openSignerAppAction ->
                signerLauncher.launch(
                    SingerResultContract.Input(
                        openSignerAppAction.body,
                        openSignerAppAction.publicKey
                    )
                )
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

            swapNativeViewModel.setSelectedSellToken(
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
                    swapNativeViewModel.setSelectedBuyToken(null)
            }


            // todo remove
            navigation?.toast(
                "selected sell token: ${
                    swapNativeViewModel.getAssetByAddress(
                        contractAddress
                    )?.symbol
                }"
            )
        }
    }

    override fun onBuyTokenSelected(contractAddress: String) {
        viewLifecycleOwner.lifecycleScope.launch {

            swapNativeViewModel.setSelectedBuyToken(
                swapNativeViewModel.getAssetByAddress(
                    contractAddress
                )
            )

            // todo remove
            navigation?.toast(
                "selected buy token: ${
                    swapNativeViewModel.getAssetByAddress(
                        contractAddress
                    )?.symbol
                }"
            )
        }
    }

    override fun onSlippageSelected(amount: Float) {
        swapNativeViewModel.selectedSlippageFlow.value = amount
    }

    private val signerLauncher = registerForActivityResult(SingerResultContract()) {
        if (it == null) {
            // feature.setFailedResult()
            Log.d(
                "swap-log",
                "# signer result failed"
            )
        } else {
            swapNativeViewModel.sendSignature(it)
            Log.d(
                "swap-log",
                "# signer result success"
            )
        }
    }

    override fun onSwapConfirmSuccess() {
        finish()
    }


}