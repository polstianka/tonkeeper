package com.tonapps.tonkeeper.fragment.swap.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.domain.GetDefaultSwapSettingsCase
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetResult
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetType
import com.tonapps.tonkeeper.fragment.swap.settings.SwapSettingsResult
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
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
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import com.tonapps.wallet.localization.R as LocalizationR

@OptIn(ExperimentalCoroutinesApi::class)
class SwapViewModel(
    private val repository: DexAssetsRepository,
    getDefaultSwapSettingsCase: GetDefaultSwapSettingsCase,
    private val walletRepository: WalletRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val swapSettings = MutableStateFlow(getDefaultSwapSettingsCase.execute())
    private val _pickedSendAsset = MutableStateFlow<DexAssetBalance?>(null)
    private val _pickedReceiveAsset = MutableStateFlow<DexAssetBalance?>(null)
    private val swapPair = combine(_pickedSendAsset, _pickedReceiveAsset) { toSend, toReceive ->
        toSend ?: return@combine null
        toReceive ?: return@combine null
        toSend to toReceive
    }
    private val _events = MutableSharedFlow<SwapEvent>()
    private val sendAmount = MutableStateFlow(BigDecimal.ZERO)

    val isLoading = walletRepository.activeWalletFlow
        .flatMapLatest { repository.isLoading }
    val events: Flow<SwapEvent>
        get() = _events
    val pickedSendAsset: Flow<DexAssetBalance?>
        get() = _pickedSendAsset
    val pickedReceiveAsset: Flow<DexAssetBalance?>
        get() = _pickedReceiveAsset
    val receiveAmount = combine(
        sendAmount,
        swapPair
    ) { sendAmount, swapPair ->
        val (toSend, toReceive) = swapPair ?: return@combine null
        val amount = sendAmount * toSend.rate.rate / toReceive.rate.rate
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
        viewModelScope.launch { repository.loadAssets() }
        val flow = combine(
            isLoading,
            walletRepository.activeWalletFlow,
            settingsRepository.currencyFlow
        ) { isLoading, wallet, currency ->
            Triple(isLoading, wallet, currency)
        }
        observeFlow(flow) { (isLoading, wallet, currency) ->
            if (isLoading) return@observeFlow
            val defaultToken =
                repository.getTotalBalancesFlow(wallet.address, wallet.testnet, currency)
                    .first()
                    .first { it.tokenEntity.isTon }
            _pickedSendAsset.value = defaultToken
        }
    }

    fun onSettingsClicked() {
        val settings = swapSettings.value
        val event = SwapEvent.NavigateToSwapSettings(settings)
        emit(_events, event)
    }

    fun onCrossClicked() {
        emit(_events, SwapEvent.NavigateBack)
    }

    fun onSendTokenClicked() = viewModelScope.launch {
        val (toSend, toReceive) = collectPickedAssets()
        val event = SwapEvent.NavigateToPickAsset(PickAssetType.SEND, toSend, toReceive)
        emit(_events, event)
    }

    private suspend fun collectPickedAssets(): Pair<TokenEntity?, TokenEntity?> {
        val pickedAssets = mutableListOf<TokenEntity>()
        val toSend = _pickedSendAsset.value
        val toReceive = _pickedReceiveAsset.value
        toSend?.let { pickedAssets.add(it.tokenEntity) }
        toReceive?.let { pickedAssets.add(it.tokenEntity) }
        return toSend?.tokenEntity to toReceive?.tokenEntity
    }

    fun onReceiveTokenClicked() = viewModelScope.launch {
        val (toSend, toReceive) = collectPickedAssets()
        val event = SwapEvent.NavigateToPickAsset(PickAssetType.RECEIVE, toSend, toReceive)
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
        val event = SwapEvent.NavigateToConfirm(
            toSend,
            toReceive,
            settings,
            amount,
            simulation
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