package com.tonapps.tonkeeper.fragment.swap

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.ton.tlb.StonFiTlb
import com.tonapps.tonkeeper.fragment.send.TransactionData
import com.tonapps.tonkeeper.helper.Coin2
import com.tonapps.tonkeeper.helper.flow.TransactionEmulator
import com.tonapps.tonkeeper.helper.flow.TransactionSender
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.RawTokensRepository
import com.tonapps.wallet.data.token.entities.TokenRateEntity
import io.stonfi.infrastructure.ClientException
import io.stonfi.models.DexReverseSimulateSwap200Response
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import uikit.extensions.collectFlow
import java.math.BigInteger

class SwapScreenViewModel(
    walletRepository: WalletRepository,
    settings: SettingsRepository,
    walletManager: WalletManager,
    passcodeRepository: PasscodeRepository,
    rawTokensRepository: RawTokensRepository,
    private val ratesRepository: RatesRepository,
    private val api: API
) : ViewModel() {
    private val tokensFlow2 = combine(
        rawTokensRepository.storageFlow,
        walletRepository.activeWalletFlow,
        ratesRepository.storageFlow,
        settings.currencyFlow,
    ) { storage, wallet, rates, currency ->
        val ratesEntity = rates.get(RatesRepository.Request(currency, emptyList()))?.result
        storage.get(
            RawTokensRepository.Request(
                accountId = wallet.accountId,
                testnet = wallet.testnet,
                currency = currency
            )
        )?.result?.let { tokens ->
            Tokens2(wallet, currency, tokens.list, ratesEntity)
        }
    }.filterNotNull()

    data class Tokens2(
        val wallet: WalletEntity,
        val currency: WalletCurrency,
        val list: List<BalanceEntity>,
        val rates: RatesEntity?
    ) {
        val tokens = list.associateBy { it.token.address }
    }


    /* * */

    data class SimulateSwapResponse(
        val key: String,
        val result: SimulateSwapResult?,
        val error: Throwable?,
    )

    data class SimulateSwapResult(
        val key: String,
        val walletEntity: WalletEntity,
        val response: DexReverseSimulateSwap200Response,
        val tokenToSend: StonFiTokenEntity,
        val tokenToReceive: StonFiTokenEntity
    ) : TransactionEmulator.Request {
        private val minReceiveAmount = Coin2.fromNano(response.minAskUnits) ?: Coin2.ZERO
        private val liquidityFeeAmount = Coin2.fromNano(response.feeUnits) ?: Coin2.ZERO

        val priceImpact = response.priceImpact.toFloat()
        val minReceiveAmountFmt: String get() = minReceiveAmount.format(tokenToReceive.token)
        val liquidityFeeAmountFmt: String get() = liquidityFeeAmount.format(tokenToReceive.token)

        val rateFmt: String get() = "1 " + tokenToSend.token.symbol + " ≈ " + response.swapRate + " " + tokenToReceive.token.symbol

        fun build(): StonFiTlb.MessageData {
            if (tokenToSend.token.isTon) {
                return StonFiTlb.buildSwapTonToJettonTxParams(
                    queryId = TransactionData.getWalletQueryId(),

                    routerAddress = MsgAddressInt.parse(response.routerAddress),
                    userWalletAddress = MsgAddressInt.parse(walletEntity.address),
                    proxyTonWalletAddress = MsgAddressInt.parse(tokenToSend.routerWalletAddress),
                    askJettonWalletAddress = MsgAddressInt.parse(tokenToReceive.routerWalletAddress),
                    offerAmount = Coins.ofNano(tokenToSend.units.value),
                    minAskAmount = Coins.ofNano(minReceiveAmount.value),
                    referralAddress = null,
                    forwardGasAmount = null
                )
            } else if (tokenToReceive.token.isTon) {
                return StonFiTlb.buildSwapJettonToTonTxParams(
                    queryId = TransactionData.getWalletQueryId(),

                    routerAddress = MsgAddressInt.parse(response.routerAddress),
                    userWalletAddress = MsgAddressInt.parse(walletEntity.address),
                    offerJettonWalletAddress = MsgAddressInt.parse(tokenToSend.userWalletAddress),
                    proxyTonWalletAddress = MsgAddressInt.parse(tokenToReceive.routerWalletAddress),
                    offerAmount = Coins.ofNano(tokenToSend.units.value),
                    minAskAmount = Coins.ofNano(minReceiveAmount.value),
                    referralAddress = null,
                    forwardGasAmount = null,
                    gasAmount = null,
                )
            } else {
                return StonFiTlb.buildSwapJettonToJettonTxParams(
                    queryId = TransactionData.getWalletQueryId(),

                    routerAddress = MsgAddressInt.parse(response.routerAddress),
                    userWalletAddress = MsgAddressInt.parse(walletEntity.address),
                    offerJettonWalletAddress = MsgAddressInt.parse(tokenToSend.userWalletAddress),
                    askJettonWalletAddress = MsgAddressInt.parse(tokenToReceive.routerWalletAddress),
                    offerAmount = Coins.ofNano(tokenToSend.units.value),
                    minAskAmount = Coins.ofNano(minReceiveAmount.value),
                    referralAddress = null,
                    forwardGasAmount = null,
                    gasAmount = null,
                )
            }
        }

        private fun createTransfer(): WalletTransfer {
            val data = build()
            val builder = WalletTransferBuilder()
            builder.bounceable = true
            builder.destination = data.to
            builder.body = data.payload
            builder.sendMode = 3
            builder.coins = data.gasAmount
            return builder.build()
        }

        override fun getWallet(): WalletEntity = walletEntity

        override fun getTransfer(): WalletTransfer = createTransfer()
    }

    enum class ButtonState { WaitAmount, WaitToken, Loading, InsufficientBalanceTON, InsufficientBalance, Error, Ready, SimulationError, HighPriceImpact }

    data class StatusState(
        val buttonState: ButtonState,
        val tokenError1: Boolean,
        val tokenError2: Boolean,
        val simulateLoading: Boolean
    )

    data class AssetsState(
        val tokens: Tokens2,
        val token1: TokenEntity?,
        val token2: TokenEntity?,
        val amountInput: Coin2.Input,
        val reverse: Boolean
    ) {
        internal val gasBalance = tokens.tokens[TokenEntity.TON.address]?.nano ?: BigInteger.ZERO

        val accountToken1: BalanceEntity? = getTokenBalanceEntity(token1, tokens)
        private val accountToken2: BalanceEntity? = getTokenBalanceEntity(token2, tokens)
        internal val maxAmountToken1 =
            Coin2.fromNano(accountToken1?.nano ?: BigInteger.ZERO)
        private val maxAmountToken2 =
            Coin2.fromNano(accountToken2?.nano ?: BigInteger.ZERO)

        val balanceToken1: CharSequence?
            get() {
                return token1?.let { selectedToken ->
                    "Balance: " + maxAmountToken1.format(
                        selectedToken
                    )
                }
            }

        val balanceToken2: CharSequence?
            get() {
                return token2?.let { selectedToken ->
                    "Balance: " + maxAmountToken2.format(
                        selectedToken
                    )
                }
            }
    }

    private val _stateFlow = MutableStateFlow<AssetsState?>(null)
    val stateFlow = _stateFlow.asStateFlow().filterNotNull()

    private val _statusFlow = MutableStateFlow(
        StatusState(
            buttonState = ButtonState.WaitAmount,
            tokenError1 = false,
            tokenError2 = false,
            simulateLoading = false
        )
    )

    val statusFlow = _statusFlow.asStateFlow()

    fun setToken1(token: TokenEntity) {
        _stateFlow.value?.let { state ->
            _stateFlow.value = state.copy(
                token1 = token,
                amountInput = if (!state.reverse) Coin2.Input.EMPTY else state.amountInput,
            )
            _simulatedSwapFlow.value = null
        }
    }

    fun setToken2(token: TokenEntity) {
        _stateFlow.value?.let { state ->
            _stateFlow.value = state.copy(
                token2 = token,
                amountInput = if (state.reverse) Coin2.Input.EMPTY else state.amountInput,
            )
            _simulatedSwapFlow.value = null
        }
    }

    fun swapTokens() {
        _stateFlow.value?.let { state ->
            _stateFlow.value = state.copy(
                token1 = state.token2,
                token2 = state.token1,
                reverse = !state.reverse
            )
            _simulatedSwapFlow.value = null
        }
    }

    fun setMaxValue() {
        _stateFlow.value?.let { state ->
            state.accountToken1?.let { token ->
                val decimals = token.token.decimals
                var amount = token.nano
                if (token.token.isTon) {
                    amount -= INVIOLABLE_TONS_NANO
                    if (amount < BigInteger.ZERO) {
                        amount = BigInteger.ZERO
                    }
                }
                _stateFlow.value = state.copy(
                    amountInput = Coin2.Input.parse((Coin2.fromNano(amount)).toString(decimals), decimals),
                    reverse = false
                )
            }
        }
    }

    fun onAmountEnter1(amount: CharSequence?) {
        _stateFlow.value?.let { state ->
            state.token1?.decimals?.let { decimals ->
                _stateFlow.value = state.copy(
                    amountInput = Coin2.Input.parse(amount.toString(), decimals),
                    reverse = false
                )
            }
        }
    }

    fun onAmountEnter2(amount: CharSequence?) {
        _stateFlow.value?.let { state ->
            state.token2?.decimals?.let { decimals ->
                _stateFlow.value = state.copy(
                    amountInput = Coin2.Input.parse(amount.toString(), decimals),
                    reverse = true
                )
            }
        }
    }

    fun getToken1(): TokenEntity? {
        return _stateFlow.value?.token1
    }

    fun getToken2(): TokenEntity? {
        return _stateFlow.value?.token2
    }


    /* * */

    private val _simulatedSwapFlow = MutableStateFlow<SimulateSwapResponse?>(null)
    val simulatedSwapFlow = _simulatedSwapFlow.asStateFlow()

    private val _simulatedSwapLoadingFlow = MutableStateFlow(false)

    private var firstTime = true

    @OptIn(FlowPreview::class)
    private val simulatedSwapLoadingFlow =
        _simulatedSwapLoadingFlow.asStateFlow().debounce {
            if (it || firstTime) {
                firstTime = false
                0L
            } else 500L
        }


    /* * */

    init {
        collectFlow(tokensFlow2) { tokens ->
            _stateFlow.value = _stateFlow.value?.copy(tokens = tokens) ?: AssetsState(
                tokens = tokens,
                token1 = TokenEntity.TON,
                token2 = null,
                amountInput = Coin2.Input.EMPTY,
                reverse = false
            )
        }

        combine(stateFlow, settings.swapSettingsFlow, this::subscribe).launchIn(viewModelScope)
        combine(
            stateFlow,
            simulatedSwapFlow,
            settings.swapSettingsFlow,
            simulatedSwapLoadingFlow
        ) { state, simulated, swapSettings, loading ->
            val sendIsTon = state.token1?.isTon ?: false
            var maxAmount = state.maxAmountToken1.value
            if (sendIsTon) {
                maxAmount -= INVIOLABLE_TONS_NANO
                if (maxAmount < BigInteger.ZERO) {
                    maxAmount = BigInteger.ZERO
                }
            }

            val sendAmount = if (state.reverse) {
                simulated?.result?.tokenToSend?.units?.value
            } else {
                state.amountInput.coin?.value
            } ?: BigInteger.ZERO

            val insufficientBalance = sendAmount > maxAmount
            val tokenError1 = !state.reverse && state.amountInput.isNotEmptyAndParseError || sendAmount > state.maxAmountToken1.value
            val highPriceImpact = (simulated?.result?.priceImpact ?: 0f) > swapSettings.priceImpact
            val tokenError2 = !tokenError1 && !highPriceImpact && state.reverse && state.amountInput.isNotEmptyAndParseError

            val buttonState = when {
                state.gasBalance < INVIOLABLE_TONS_NANO -> ButtonState.InsufficientBalanceTON
                state.amountInput.coin == null || state.amountInput.coin.value == BigInteger.ZERO -> ButtonState.WaitAmount
                state.token1 == null || state.token2 == null -> ButtonState.WaitToken
                simulated == null -> ButtonState.Loading
                simulated.error != null -> ButtonState.SimulationError
                insufficientBalance && sendIsTon -> ButtonState.InsufficientBalanceTON
                insufficientBalance -> ButtonState.InsufficientBalance
                highPriceImpact -> ButtonState.HighPriceImpact
                tokenError1 || tokenError2 -> ButtonState.Error
                simulated.key == requestKey(state, swapSettings) -> ButtonState.Ready
                else -> ButtonState.Loading
            }

            _statusFlow.value = StatusState(
                buttonState = buttonState,
                tokenError1 = tokenError1,
                tokenError2 = tokenError2,
                simulateLoading = loading
            )
        }.launchIn(viewModelScope)
    }


    /* * */

    private var lastSimulationTime: Long = 0L
    private var subscriptionScope: CoroutineScope? = null

    private fun subscribe(state: AssetsState, swapSettings: SettingsRepository.SwapSettings) {
        unsubscribe()
        if (state.token1 != null && state.token2 != null && state.amountInput.coin != null) {
            _simulatedSwapLoadingFlow.value = true

            subscriptionScope = CoroutineScope(Dispatchers.IO)
            subscriptionScope!!.launch {
                val currentTime = System.currentTimeMillis()
                val timeSinceLastSimulation = currentTime - lastSimulationTime
                if (timeSinceLastSimulation < TIME_LIMIT) {
                    delay(TIME_LIMIT - timeSinceLastSimulation)
                }
                while (true) {
                    _simulatedSwapLoadingFlow.value = true
                    lastSimulationTime = System.currentTimeMillis()

                    val key = requestKey(state, swapSettings)
                    try {
                        _simulatedSwapFlow.value = SimulateSwapResponse(result = simulate(
                            key,
                            state,
                            state.token1,
                            state.token2,
                            state.amountInput.coin,
                            swapSettings.slippage.toString()
                        ), key = key, error = null)
                        _simulatedSwapLoadingFlow.value = false
                        delay(DELAY_NORMAL)
                        continue
                    } catch (t: ClientException) {
                        if (_simulatedSwapFlow.value?.key != key) {
                            _simulatedSwapFlow.value = SimulateSwapResponse(key = key, result = null, error = t)
                        }
                        _simulatedSwapLoadingFlow.value = false
                        delay(DELAY_NORMAL)
                        continue
                    } catch (_: CancellationException) {
                    } catch (t: Throwable) {
                        if (_simulatedSwapFlow.value?.key != key) {
                            _simulatedSwapFlow.value = null
                        }
                    }
                    _simulatedSwapLoadingFlow.value = false
                    delay(DELAY_ERROR)
                }
            }
        } else {
            _simulatedSwapLoadingFlow.value = false
            _simulatedSwapFlow.value = null
        }
    }

    private fun unsubscribe() {
        subscriptionScope?.cancel()
        subscriptionScope = null
    }

    private suspend fun simulate(
        key: String,
        state: AssetsState,
        tokenToSell: TokenEntity,
        tokenToBuy: TokenEntity,
        units: Coin2,
        slippageTolerance: String
    ): SimulateSwapResult = withContext(Dispatchers.IO) {
        val rates = getRemoteTokenRates(state)
        val response = if (state.reverse) api.stonFiApi.dex.dexReverseSimulateSwap(
            offerAddress = tokenToSell.stonFiAddress,
            askAddress = tokenToBuy.stonFiAddress,
            units = units.toNanoString(),
            slippageTolerance = slippageTolerance
        ) else api.stonFiApi.dex.dexSimulateSwap(
            offerAddress = tokenToSell.stonFiAddress,
            askAddress = tokenToBuy.stonFiAddress,
            units = units.toNanoString(),
            slippageTolerance = slippageTolerance
        )

        SimulateSwapResult(
            key = key,
            walletEntity = state.tokens.wallet,
            tokenToSend = getLocalOrRemoteAccountToken(
                state.tokens,
                response.routerAddress,
                rates,
                tokenToSell,
                response.offerUnits
            ),
            tokenToReceive = getLocalOrRemoteAccountToken(
                state.tokens,
                response.routerAddress,
                rates,
                tokenToBuy,
                response.askUnits
            ),
            response = response
        )
    }

    override fun onCleared() {
        unsubscribe()
        super.onCleared()
    }

    /* * */

    private val _simulatedSwapToConfirmFlow = MutableStateFlow<SimulateSwapResult?>(null)
    val simulatedSwapToConfirmFlow = _simulatedSwapToConfirmFlow.asStateFlow()

    fun prepareConfirmPage() {
        _simulatedSwapToConfirmFlow.value = _simulatedSwapFlow.value?.result
    }

    /* * */

    data class TokensRates(val currency: WalletCurrency, val rates: Map<String, TokenRateEntity>)

    private val walletsCache = mutableMapOf<String, StonFiTokenEntity>()

    data class StonFiTokenEntity(
        val token: TokenEntity,
        val userWalletAddress: String,
        val routerWalletAddress: String,
        var rate: TokenRateEntity? = null,
        val units: Coin2
    )

    private suspend fun getRemoteTokenRates(state: AssetsState): TokensRates =
        withContext(Dispatchers.IO) {
            val tokens = listOfNotNull(state.token1, state.token2)
            if (tokens.isEmpty()) {
                TokensRates(currency = state.tokens.currency, rates = emptyMap())
            } else {
                val currency = state.tokens.currency
                val tokenAddresses = tokens.map { it.address }

                ratesRepository.load(currency, tokenAddresses.toMutableList())
                val rates = ratesRepository.getRates(currency, tokenAddresses)
                val rates2 = tokens.associate {
                    it.address to TokenRateEntity(
                        currency = currency,
                        fiat = 0f,
                        rate = rates.getRate(it.address),
                        rateDiff24h = rates.getDiff24h(it.address)
                    )
                }
                TokensRates(currency = state.tokens.currency, rates = rates2)
            }
        }

    private suspend fun getLocalOrRemoteAccountToken(
        tokens: Tokens2,
        routerAddress: String,
        rates: TokensRates,
        token: TokenEntity,
        units: String
    ): StonFiTokenEntity = withContext(Dispatchers.IO) {
        val accountTokenOpt = tokens.tokens[token.address]
        val rateOpt = rates.rates[token.address]
        val amount = Coin2.fromNano(units) ?: Coin2.ZERO

        val key = tokenWalletKey(tokens.wallet.address, token.address, routerAddress)

        walletsCache[key]?.copy(rate = rateOpt /*?: accountTokenOpt?.rate*/, units = amount)
            ?: run {
                val userWalletAddress = if (accountTokenOpt?.token?.isTon != true) {
                    accountTokenOpt?.walletAddress ?: api.getJettonsWalletAddress(
                        jettonId = token.stonFiProxyAddress,
                        accountId = tokens.wallet.address,
                        testnet = tokens.wallet.testnet
                    )
                } else "TON"

                val routerWalletAddress = api.getJettonsWalletAddress(
                    jettonId = token.stonFiProxyAddress,
                    accountId = routerAddress,
                    testnet = tokens.wallet.testnet
                )

                StonFiTokenEntity(
                    token = token,
                    userWalletAddress = userWalletAddress,
                    routerWalletAddress = routerWalletAddress,
                    rate = rateOpt /*?: accountTokenOpt?.rate*/,
                    units = amount
                )
            }
    }

    /* * */

    val transactionSender = TransactionSender(api, passcodeRepository, walletManager, viewModelScope)

    fun send(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionSender.send(context, _simulatedSwapToConfirmFlow.value!!)
        }
    }


    /* * */

    private companion object {
        private val INVIOLABLE_TONS_NANO = BigInteger.valueOf(300_000_000)

        private const val TIME_LIMIT = 1000L
        private const val DELAY_NORMAL = 5000L
        private const val DELAY_ERROR = 1000L

        private fun tokenWalletKey(
            walletAddress: String,
            tokenAddress: String,
            routerAddress: String
        ): String {
            return walletAddress + "_" + tokenAddress + "_" + routerAddress
        }

        private fun requestKey(
            state: AssetsState,
            swapSettings: SettingsRepository.SwapSettings
        ): String {
            return state.token1?.stonFiAddress + "_" + state.token2?.stonFiAddress + "_" + state.amountInput.input + "_" + swapSettings.slippage.toString() + "_" + state.reverse.toString()
        }

        private fun getTokenBalanceEntity(
            tokenOpt: TokenEntity?,
            tokens: Tokens2
        ): BalanceEntity? {
            return tokenOpt?.let { token -> tokens.tokens[token.address] }
        }
    }
}