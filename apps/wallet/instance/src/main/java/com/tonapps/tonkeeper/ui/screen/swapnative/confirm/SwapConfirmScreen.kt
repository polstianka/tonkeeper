package com.tonapps.tonkeeper.ui.screen.swapnative.confirm

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.StringFormatter
import com.tonapps.tonkeeper.core.signer.SingerResultContract
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.ui.screen.swapnative.SwapBaseScreen
import com.tonapps.tonkeeper.ui.screen.swapnative.main.SwapConfirmListener
import com.tonapps.tonkeeper.ui.screen.swapnative.main.SwapNativeScreen
import com.tonapps.tonkeeper.view.SwapFromContainerView
import com.tonapps.tonkeeper.view.SwapToContainerView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.ProgressButton

class SwapConfirmScreen : SwapBaseScreen(R.layout.fragment_swap_confirm), BaseFragment.BottomSheet {

    private val swapConfirmViewModel: SwapConfirmViewModel by viewModel()

    private val args: SwapConfirmArgs by lazy { SwapConfirmArgs(requireArguments()) }

    private var swapConfirmListener: SwapConfirmListener? = null

    private lateinit var headerView: HeaderView
    private lateinit var confirmButton: ProgressButton
    private lateinit var cancelButton: AppCompatButton

    private lateinit var swapFromContainerView: SwapFromContainerView
    private lateinit var swapToContainerView: SwapToContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        swapConfirmViewModel.apply {
            selectedFromToken = MutableStateFlow(args.fromAsset!!)
            selectedToToken = MutableStateFlow(args.toAsset!!)
            swapDetailsFlow = MutableStateFlow(args.swapDetail!!)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        handleViews()
        handleViewModel()

    }

    private fun initializeViews(view: View) {
        headerView = view.findViewById(R.id.header)
        confirmButton = view.findViewById(R.id.confirm_progress_button)
        cancelButton = view.findViewById(R.id.cancel)

        swapFromContainerView = view.findViewById(R.id.swap_from_container)
        swapToContainerView = view.findViewById(R.id.swap_to_container)

    }

    private fun handleViews() {
        headerView.doOnActionClick = { finish() }
        cancelButton.setOnClickListener {
            finish()
        }

        swapFromContainerView.setConfirmMode(true)
        swapToContainerView.setConfirmMode(true)

        viewLifecycleOwner.lifecycleScope.launch {
            swapConfirmViewModel.selectedFromToken.collect { fromAsset ->
                swapFromContainerView.apply {
                    if (fromAsset == null) {
                        // reset
                        postDelayed(SwapNativeScreen.ANIMATE_LAYOUT_CHANGE_DELAY) {
                            sellTokenTitle.setText(getString(com.tonapps.wallet.localization.R.string.choose))
                            sellTokenIcon.visibility = View.GONE
                        }
                        sellTokenIcon.clear(requireContext())

                        sellTokenBalance.text = "0"
                        sellTokenBalance.visibility = View.GONE

                    } else {
                        // poppulate
                        postDelayed(SwapNativeScreen.ANIMATE_LAYOUT_CHANGE_DELAY) {
                            sellTokenTitle.setText(fromAsset.symbol)
                            //sellTokenTitle.text = fromAsset.symbol
                            sellTokenIcon.visibility = View.VISIBLE

                            sellTokenBalance.text = swapConfirmViewModel.getfromAssetFiatInput()
                            sellTokenBalance.visibility = View.VISIBLE
                        }
                        sellTokenIcon.setImageURI(fromAsset.imageUrl?.toUri())
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            swapConfirmViewModel.selectedToToken.collect { toAsset ->
                swapToContainerView.apply {
                    if (toAsset == null) {
                        // reset
                        postDelayed(SwapNativeScreen.ANIMATE_LAYOUT_CHANGE_DELAY) {
                            buyTokenTitle.text =
                                getString(com.tonapps.wallet.localization.R.string.choose)
                            buyTokenIcon.visibility = View.GONE
                        }
                        buyTokenIcon.clear(requireContext())

                        buyTokenBalance.text = "0"
                        buyTokenBalance.visibility = View.GONE
                    } else {
                        // populate
                        postDelayed(SwapNativeScreen.ANIMATE_LAYOUT_CHANGE_DELAY) {
                            buyTokenTitle.text = toAsset.symbol
                            buyTokenIcon.visibility = View.VISIBLE

                            buyTokenBalance.text = swapConfirmViewModel.getToAssetFiatInput()
                            buyTokenBalance.visibility = View.VISIBLE
                        }
                        buyTokenIcon.setImageURI(toAsset.imageUrl?.toUri())
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            swapConfirmViewModel.swapDetailsFlow.collect { swapDetails ->
                swapToContainerView.apply {
                    swapDetailContainer.visibility = View.GONE

                    swapDetails?.apply {
                        swapFromContainerView.sellAmountInput.setText(
                            Coin.parseBigInt(
                                swapDetails.offerUnits.toString(),
                                fromDecimals!!,
                                false
                            ).toPlainString()
                        )

                        val sellSymbol = swapConfirmViewModel.selectedFromToken.value?.symbol ?: ""
                        val buySymbol = swapConfirmViewModel.selectedToToken.value?.symbol ?: ""

                        val priceImpactFloat = getPriceImpactAsFloat()
                        val priceImpactColor = getPriceImpactColor(priceImpactFloat)
                        swapTitleTv.text = "1 ${sellSymbol} ≈ ${
                            StringFormatter.truncateToFourDecimalPlaces(swapRate)
                        } ${buySymbol}"
                        swapTitleTv.setTextColor(priceImpactColor)
                        priceImpactTv.text = getFormattedPriceImpact()
                        priceImpactTv.setTextColor(priceImpactColor)
                        minReceivedTv.text =
                            "${
                                Coin.parseBigInt(minAskUnits.toString(), toDecimals!!)
                                    .toPlainString()
                            }"
                        providerFeeTv.text =
                            "${
                                Coin.parseBigInt(feeUnits.toString(), toDecimals!!).toPlainString()
                            } ${buySymbol}"
                        blockchainFeeTv.text = "0.08 - 0.25 TON"
                        routeTv.text = "${sellSymbol.uppercase()} » ${buySymbol.uppercase()}"
                        buyAmountInput.setText(
                            Coin.parseBigInt(
                                askUnits.toString(),
                                toDecimals!!,
                                false
                            ).toPlainString()
                        )

                        swapDetailContainer.visibility = View.VISIBLE
                    }
                }
            }
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

        confirmButton.setText(requireContext().getString(com.tonapps.wallet.localization.R.string.confirm))
        confirmButton.onClick = { view, isEnabled ->
            swapConfirmViewModel.decideSwapType()?.also { swapType ->
                swapConfirmViewModel.confirmSwap(requireContext(), swapType)
            }
        }
    }

    private fun handleViewModel() = swapConfirmViewModel.apply {
        viewLifecycleOwner.lifecycleScope.launch {
            effectFlow.collect { effect ->
                newUiEffect(effect)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            screenStateFlow.collect { state ->
                newUiState(state)
            }
        }
    }


    private fun newUiEffect(effect: SwapConfirmScreenEffect) {
        when (effect) {
            is SwapConfirmScreenEffect.CloseScreen -> {
                if (effect.navigateToHistory) {
                    navigation?.toast(
                        context?.getString(com.tonapps.wallet.localization.R.string.swap_success)
                            ?: ""
                    )

                    navigation?.openURL("tonkeeper://activity")
                    swapConfirmListener?.onSwapConfirmSuccess()
                    finish()
                } else {
                    navigation?.toast(
                        context?.getString(com.tonapps.wallet.localization.R.string.swap_failed)
                            ?: ""
                    )
                }
            }

            is SwapConfirmScreenEffect.OpenSignerApp -> {
                signerLauncher.launch(
                    SingerResultContract.Input(
                        effect.body,
                        effect.publicKey
                    )
                )
            }
        }
    }

    private fun newUiState(screenState: SwapConfirmScreenState) {
        confirmButton.toggleProgressDisplay(screenState.isLoading)

    }

    private val signerLauncher = registerForActivityResult(SingerResultContract()) {
        if (it == null) {
            navigation?.toast(
                context?.getString(com.tonapps.wallet.localization.R.string.swap_sign_failed)
                    ?: ""
            )
            swapConfirmViewModel.screenStateFlow.update { it.copy(isLoading = false) }
            Log.d(
                "swap-log",
                "# signer result failed"
            )
        } else {
            swapConfirmViewModel.sendSignature(it)
            Log.d(
                "swap-log",
                "# signer result success"
            )
        }
    }

    fun setSwapConfirmListener(swapConfirmListener: SwapConfirmListener) {
        this.swapConfirmListener = swapConfirmListener
    }

    companion object {
        fun newInstance(
            swapConfirmArgs: SwapConfirmArgs
        ): SwapConfirmScreen {
            val fragment = SwapConfirmScreen()
            fragment.arguments = swapConfirmArgs.toBundle()
            return fragment
        }
    }


}