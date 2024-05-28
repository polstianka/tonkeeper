package com.tonapps.tonkeeper.fragment.swap.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.Coin.TON_DECIMALS
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.fragment.swap.StonfiConstants
import com.tonapps.tonkeeper.fragment.swap.currency.CurrencyScreenState.ButtonState
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.time.Duration.Companion.seconds

class CurrencyScreenFeature(
    private val api: API,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrencyScreenState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CurrencyScreenEffect>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEffect: SharedFlow<CurrencyScreenEffect> = _uiEffect.asSharedFlow()

    private var isSendTokenPick: Boolean = true

    private var lastEnteredSend: Boolean = false
    private var swap: Boolean = false
    private var job: Job? = null
    private var enteredByManual = false

    private var isSwapAction = false
    private var setup = false

    @OptIn(FlowPreview::class)
    val _test = _uiState
        .debounce(1.seconds)
        .filter { state -> !(state.sendInfo.amount == 0F && state.receiveInfo.amount == 0F) }
        .filter { !setup }
        .filter { !isSwapAction }
        .distinctUntilChanged { old, new ->
            val equalsSendByAmount = old.sendInfo.amount == new.sendInfo.amount
            val equalsReceiveByAmount = old.receiveInfo.amount == new.receiveInfo.amount
            val equalsSendByToken = old.sendInfo.token == new.sendInfo.token
            val equalsReceiveByToken = old.receiveInfo.token == new.receiveInfo.token
            equalsSendByAmount && equalsReceiveByAmount && equalsSendByToken && equalsReceiveByToken
        }
        .onEach {
            job?.cancel()
            job = loadDetails()
        }
        .launchIn(viewModelScope)

    val _loading = _uiState
        .map { state ->
            val hasDetails = state.details.items.isNotEmpty()
            if (hasDetails) {
                setLoading(state.simulation)
            }
            _uiState.update { oldState -> oldState.copy(loadingDetails = state.simulation) }
        }
        .launchIn(viewModelScope)

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        job = null
    }

    fun setSendValue(value: Float) {
        val currentState = _uiState.value.sendInfo.amount
        if (value != currentState) {
            isSwapAction = false
            setup = false
            enteredByManual = true
            lastEnteredSend = true
            _uiState.update { old ->
                old.copy(
                    sendInfo = old.sendInfo.copy(
                        amount = value
                    )
                )
            }
        }
    }

    fun setReceiveValue(value: Float) {
        val currentState = _uiState.value.receiveInfo.amount
        if (value != currentState) {
            setup = false
            isSwapAction = false
            enteredByManual = true
            lastEnteredSend = false
            _uiState.update { old ->
                old.copy(
                    receiveInfo = old.receiveInfo.copy(
                        amount = value
                    )
                )
            }
        }
    }

    fun sendTokenChanged(tokenInfo: TokenInfo?) {
        setup = false
        _uiState.update { oldState ->
            oldState.copy(sendInfo = oldState.sendInfo.copy(token = tokenInfo))
        }
    }

    fun receiveTokenChanged(tokenInfo: TokenInfo?) {
        setup = false
        _uiState.update { oldState ->
            oldState.copy(receiveInfo = oldState.receiveInfo.copy(token = tokenInfo))
        }
    }

    fun sendTokenPick() {
        isSendTokenPick = true
        _uiEffect.tryEmit(
            CurrencyScreenEffect.OpenTokenPicker(
                selected = _uiState.value.sendInfo.token,
                except = _uiState.value.receiveInfo.token,
            )
        )
    }

    fun receiveTokenPick() {
        isSendTokenPick = false
        _uiEffect.tryEmit(
            CurrencyScreenEffect.OpenTokenPicker(
                selected = _uiState.value.receiveInfo.token,
                except = _uiState.value.sendInfo.token,
            )
        )
    }

    fun onTokenPick(tokenInfo: TokenInfo) {
        if (isSendTokenPick) {
            sendTokenChanged(tokenInfo)
        } else {
            receiveTokenChanged(tokenInfo)
        }
    }

    fun toggleDetailsItemsVisibility() {
        _uiState.update { oldState ->
            oldState.copy(
                details = oldState.details.copy(
                    expanded = !oldState.details.expanded
                )
            )
        }
    }

    fun onSwapButtonClick() {
        viewModelScope.launch {
            lastEnteredSend = !lastEnteredSend
            isSwapAction = true
            swap = !swap
            val oldState = _uiState.value
            _uiState.emit(
                oldState.copy(
                    sendInfo = oldState.receiveInfo,
                    receiveInfo = oldState.sendInfo,
                )
            )

            _uiEffect.emit(
                CurrencyScreenEffect.UpdateAmounts(
                    sendAmount = oldState.receiveInfo.amount,
                    receiveAmount = oldState.sendInfo.amount
                )
            )
            loadDetails()
        }
    }

    fun balanceActionClick() {
        val parsedAmount =
            _uiState.value.sendInfo.token?.balance?.filterNot { it.isLetter() }?.trim()
        _uiEffect.tryEmit(
            CurrencyScreenEffect.SetSendAmount(
                amount = parsedAmount?.toFloatOrNull() ?: 0F
            )
        )
    }

    fun onButtonClick() {
        when (_uiState.value.buttonState) {
            ButtonState.CHOOSE_TOKEN -> {
                val sendTokenExist = _uiState.value.sendInfo.token != null
                if (sendTokenExist) {
                    receiveTokenPick()
                } else {
                    sendTokenPick()
                }
            }

            ButtonState.ENTER_AMOUNT -> _uiEffect.tryEmit(
                CurrencyScreenEffect.TakeFocus(CurrencyScreenEffect.TakeFocus.Focusable.SEND)
            )

            else -> _uiEffect.tryEmit(CurrencyScreenEffect.NavigateToConfirm)
        }
    }

    private fun loadDetails() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(simulation = true) }
        val wallet = App.walletManager.getWalletInfo()!!
        val askAddress = _uiState.value.receiveInfo.token?.contractAddress.orEmpty()
        val offerAddress = _uiState.value.sendInfo.token?.contractAddress.orEmpty()

        val offerDecimals = _uiState.value.sendInfo.token?.decimals ?: TON_DECIMALS
        val askDecimals = _uiState.value.receiveInfo.token?.decimals ?: TON_DECIMALS

        val sendAmount = _uiState.value.sendInfo.amount
        val receiveAmount = _uiState.value.receiveInfo.amount

        val offerUnits = Coin.toNano(value = sendAmount, decimals = offerDecimals).toString()
        val askUnits = Coin.toNano(value = receiveAmount, decimals = askDecimals).toString()

        val referralAddress = StonfiConstants.REFERRAL_USER_FRIENDLY
        val slippageTolerance = (settingsRepository.slippage / 100.0).toString()

        val simulationResult = if (lastEnteredSend) {
            api.simulateSwap(
                askAddress = askAddress,
                offerAddress = offerAddress,
                offerUnits = offerUnits,
                referralAddress = referralAddress,
                slippageTolerance = slippageTolerance,
                testnet = wallet.testnet
            )
        } else {
            api.simulateReversedSwap(
                askAddress = askAddress,
                offerAddress = offerAddress,
                askUnits = askUnits,
                referralAddress = referralAddress,
                slippageTolerance = slippageTolerance,
                testnet = wallet.testnet
            )
        }

        if (simulationResult == null) {
            _uiState.update { oldState ->
                oldState.copy(simulation = false)
            }
            return@launch
        }

        val percentFormatter = DecimalFormat("#.##").apply { roundingMode = RoundingMode.CEILING }
        val formattedPriceImpact = simulationResult.priceImpact.toFloatOrNull()?.let { percentFormatter.format(it) }.orEmpty()

        val priceImpact = "$formattedPriceImpact %"
        val minAskUnit = Coin.toCoins(
            value = simulationResult.minAskUnits.toLongOrNull() ?: 0L,
            decimals = askDecimals
        )
        val minReceived = CurrencyFormatter.format(
            _uiState.value.receiveInfo.token?.symbol.orEmpty(),
            minAskUnit
        ).toString()
        val liqProviderFee = CurrencyFormatter.format(
            currency = _uiState.value.receiveInfo.token?.symbol.orEmpty(),
            value = Coin.toCoins(
                value = simulationResult.feeUnits.toLongOrNull() ?: 0L,
                decimals = askDecimals
            ),
        ).toString()
        val blockchainFee = StonfiConstants.BLOCKCHAIN_FEE
        val route =
            "${_uiState.value.sendInfo.token?.symbol} ≈ ${_uiState.value.receiveInfo.token?.symbol}"
        val provider = StonfiConstants.PROVIDER

        val simulationOfferUnits = Coin.toCoins(
            value = simulationResult.offerUnits.toLongOrNull() ?: 0L,
            decimals = offerDecimals
        )
        val simulationAskUnits = Coin.toCoins(
            value = simulationResult.askUnits.toLongOrNull() ?: 0L,
            decimals = askDecimals
        )

        val priceTitleFrom = CurrencyFormatter.format(
            _uiState.value.sendInfo.token?.symbol.orEmpty(),
            1F,
        )
        val priceTitleTo = CurrencyFormatter.format(
            _uiState.value.receiveInfo.token?.symbol.orEmpty(),
            simulationResult.swapRate.toFloatOrNull() ?: 0F,
            if (lastEnteredSend) askDecimals else offerDecimals
        )

        val swapDetails = listOf(
            SwapDetailsItem.Divider,
            SwapDetailsItem.Header(
                title = "$priceTitleFrom ≈ $priceTitleTo",
                loading = false,
            ),
            SwapDetailsItem.Divider,
            SwapDetailsItem.Cell(
                title = Localization.price_impact,
                value = priceImpact,
                additionalInfo = Localization.price_impact_hint,
            ),
            SwapDetailsItem.Cell(
                title = Localization.minimum_received,
                value = minReceived,
                additionalInfo = Localization.minimum_received_hint,
            ),
            SwapDetailsItem.Cell(
                title = Localization.liquidity_provider_fee,
                value = liqProviderFee,
                additionalInfo = Localization.liquidity_provider_fee_hint,
            ),
            SwapDetailsItem.Cell(
                title = Localization.blockchain_fee,
                value = blockchainFee
            ),
            SwapDetailsItem.Cell(
                title = Localization.route,
                value = route
            ),
            SwapDetailsItem.Cell(
                title = Localization.provider,
                value = provider
            ),
        )

        if (isActive) {
            val sendAmountResult = if (lastEnteredSend) sendAmount else simulationOfferUnits
            val receiveAmountResult = simulationAskUnits

            enteredByManual = false
            setup = true
            val oldState = _uiState.value
            _uiState.emit(
                oldState.copy(
                    sendInfo = oldState.sendInfo.copy(amount = sendAmountResult),
                    receiveInfo = oldState.receiveInfo.copy(amount = receiveAmountResult),
                    details = oldState.details.copy(items = swapDetails),
                    simulation = false
                )
            )

            _uiEffect.emit(
                CurrencyScreenEffect.UpdateAmounts(
                    sendAmount = sendAmountResult,
                    receiveAmount = receiveAmountResult
                )
            )
        }
    }

    private fun setLoading(loading: Boolean) {
        _uiState.update { state ->
            val currentDetails = state.details.items
            val resultDetails = currentDetails.map { item ->
                if (item is SwapDetailsItem.Header) {
                    item.copy(loading = loading)
                } else {
                    item
                }
            }
            state.copy(details = state.details.copy(items = resultDetails))
        }
    }

}