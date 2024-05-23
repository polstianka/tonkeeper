package com.tonapps.tonkeeper.fragment.swap.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.domain.GetDefaultSwapSettingsCase
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetResult
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetType
import com.tonapps.tonkeeper.fragment.swap.settings.SwapSettingsResult
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import com.tonapps.wallet.localization.R as LocalizationR

@OptIn(ExperimentalCoroutinesApi::class)
class SwapViewModel(
    private val repository: DexAssetsRepository,
    getDefaultSwapSettingsCase: GetDefaultSwapSettingsCase,
    settingsRepository: SettingsRepository,
    private val ratesRepository: RatesRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val swapSettings = MutableStateFlow(getDefaultSwapSettingsCase.execute())
    private val _pickedSendAsset = MutableStateFlow<DexAsset?>(null)
    private val _pickedReceiveAsset = MutableStateFlow<DexAsset?>(null)
    private val swapPair = combine(_pickedSendAsset, _pickedReceiveAsset) { toSend, toReceive ->
        toSend ?: return@combine null
        toReceive ?: return@combine null
        toSend to toReceive
    }
    private val _events = MutableSharedFlow<SwapEvent>()
    private val sendAmount = MutableStateFlow(BigDecimal.ZERO)
    private val currency = settingsRepository.currencyFlow

    val isLoading = walletRepository.activeWalletFlow
        .flatMapLatest { repository.getIsLoadingFlow(it.address) }
    val events: Flow<SwapEvent>
        get() = _events
    val pickedSendAsset: Flow<DexAsset?>
        get() = _pickedSendAsset
    val pickedReceiveAsset: Flow<DexAsset?>
        get() = _pickedReceiveAsset
    val receiveAmount = combine(
        sendAmount,
        swapPair
    ) { sendAmount, swapPair ->
        val (toSend, toReceive) = swapPair ?: return@combine null
        val amount = sendAmount * toSend.dexUsdPrice / toReceive.dexUsdPrice
        toReceive to amount
    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)
    val simulation = combine(
        swapPair,
        sendAmount,
        swapSettings
    ) { swapPair, amount, settings ->
        swapPair ?: return@combine null
        if (amount == BigDecimal.ZERO) return@combine null
        val pair = amount to settings
        swapPair to pair
    }
        .flatMapLatest { c ->
            val (a, b) = c ?: return@flatMapLatest flowOf(null)
            val (sendAsset, receiveAsset) = a
            val (amount, settings) = b
            repository.emulateSwap(sendAsset, receiveAsset, amount, settings.slippagePercent)
        }
        .shareIn(viewModelScope, started = SharingStarted.Lazily, replay = 1)
    val buttonState = combine(sendAmount, swapPair, simulation) { amount, pair, simulation ->
        when {
            amount == BigDecimal.ZERO -> {
                val text = TextWrapper.StringResource(LocalizationR.string.enter_amount)
                text to false
            }

            pair == null -> {
                val text = TextWrapper.StringResource(LocalizationR.string.choose_token)
                text to false
            }

            simulation == null || simulation is SwapSimulation.Loading -> {
                val text = TextWrapper.StringResource(LocalizationR.string.please_wait)
                text to false
            }

            else -> {
                val text = TextWrapper.StringResource(LocalizationR.string.continue_action)
                text to true
            }
        }
    }


    init {
        observeFlow(walletRepository.activeWalletFlow) { wallet ->
            repository.loadAssets(wallet.address)
        }
        combine(isLoading, walletRepository.activeWalletFlow) { isLoading, wallet ->
            if (isLoading) return@combine
            _pickedSendAsset.emit(repository.getDefaultAsset(wallet.address))
        }.launchIn(viewModelScope)
    }

    fun onSettingsClicked() {
        val settings = swapSettings.value
        val event = SwapEvent.NavigateToSwapSettings(settings)
        emit(_events, event)
    }

    fun onCrossClicked() {
        emit(_events, SwapEvent.NavigateBack)
    }

    fun onSendTokenClicked() {
        val event = SwapEvent.NavigateToPickAsset(PickAssetType.SEND)
        emit(_events, event)
    }

    fun onReceiveTokenClicked() {
        val event = SwapEvent.NavigateToPickAsset(PickAssetType.RECEIVE)
        emit(_events, event)
    }

    fun onSwapTokensClicked() = viewModelScope.launch {
        val toSend = _pickedSendAsset.value
        val toSendAmount = sendAmount.value
        val toReceiveAmount = receiveAmount.first()
        _pickedSendAsset.value = _pickedReceiveAsset.value
        _pickedReceiveAsset.value = toSend
        when {
            toSendAmount == BigDecimal.ZERO -> Unit
            toReceiveAmount == null -> Unit
            else -> {
                sendAmount.value = toReceiveAmount.second
                ignoreNextUpdate = true
                _events.emit(
                    SwapEvent.FillInput(
                        toReceiveAmount.second.setScale(
                            2,
                            RoundingMode.FLOOR
                        ).toPlainString()
                    )
                )
            }
        }
    }

    private var ignoreNextUpdate = false
    fun onSendAmountChanged(amount: BigDecimal) {
        if (sendAmount.value == amount) return
        if (ignoreNextUpdate) {
            ignoreNextUpdate = false
            return
        }
        sendAmount.value = amount
    }

    fun onAssetPicked(result: PickAssetResult) {
        when (result.type) {
            PickAssetType.SEND -> {
                _pickedSendAsset.value = result.asset
            }

            PickAssetType.RECEIVE -> {
                _pickedReceiveAsset.value = result.asset
            }
        }
    }

    fun onSettingsUpdated(result: SwapSettingsResult) {
        swapSettings.value = result.settings
    }

    fun onConfirmClicked() = viewModelScope.launch {
        val (toSend, toReceive) = swapPair.first() ?: return@launch
        val amount = sendAmount.value
        val settings = swapSettings.value
        val simulation = simulation.first() as? SwapSimulation.Result ?: return@launch
        val currency = currency.first()
        val event = SwapEvent.NavigateToConfirm(
            toSend,
            toReceive,
            settings,
            amount,
            simulation,
            currency = currency,
            ratesCurrency = ratesRepository.cache(currency, listOf("TON")),
            ratesUsd = ratesRepository.cache(WalletCurrency.DEFAULT, listOf("TON"))
        )
        _events.emit(event)
    }

    fun onMaxClicked() = viewModelScope.launch {
        val balance = _pickedSendAsset.value?.balance ?: return@launch
        sendAmount.value = balance
        ignoreNextUpdate = true
        _events.emit(
            SwapEvent.FillInput(
                CurrencyFormatter.format(balance, 2)
            )
        )
    }
}