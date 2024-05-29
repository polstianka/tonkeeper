package com.tonapps.tonkeeper.fragment.staking.deposit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.tlb.StakingTlb
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.network.NetworkMonitor
import com.tonapps.tonkeeper.api.icon
import com.tonapps.tonkeeper.fragment.send.TransactionData
import com.tonapps.tonkeeper.helper.flow.AmountInputController
import com.tonapps.tonkeeper.helper.flow.TransactionEmulator
import com.tonapps.tonkeeper.helper.flow.TransactionSender
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.api.entity.pool.PoolEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.pools.StakingPoolsRepository
import com.tonapps.wallet.data.pools.entities.StakingPoolImplementationExt
import com.tonapps.wallet.data.pools.entities.StakingPoolsEntity
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.RawTokensRepository
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import ton.SendMode
import uikit.extensions.collectFlow
import java.math.BigInteger

class DepositScreenViewModel(
    private val settingsRepository: SettingsRepository,
    private val poolsRepository: StakingPoolsRepository,
    walletRepository: WalletRepository,
    settings: SettingsRepository,
    networkMonitor: NetworkMonitor,
    passcodeRepository: PasscodeRepository,
    walletManager: WalletManager,
    private val ratesRepository: RatesRepository,
    rawTokensRepository: RawTokensRepository,
    api: API
) : ViewModel() {
    private val tokensFlow2 = combine(
        rawTokensRepository.storageFlow,
        walletRepository.activeWalletFlow,
        ratesRepository.storageFlow,
        settings.currencyFlow,
    ) { storage, wallet, rates, currency ->
        val ratesEntity = rates.get(RatesRepository.Request(currency, emptyList()))?.result
        storage.get(wallet.accountId, wallet.testnet, currency)?.result?.let { tokens ->
            Tokens2(wallet, currency, tokens.list, ratesEntity)
        }
    }.filterNotNull()

    private data class Tokens2(
        val wallet: WalletEntity,
        val currency: WalletCurrency,
        private val tokens: List<BalanceEntity>,
        val rates: RatesEntity?
    ) {
        val token = tokens.find { it.token.isTon }!!
    }


    private var defaultPoolAddress: String? = null

    fun init(pool: String?) {
        defaultPoolAddress = pool
        collectFlow(poolsFlow) { pools ->
            if (_confirmedPoolFlow.value == null) {
                _confirmedPoolFlow.value = defaultPoolAddress?.let {
                    pools.pools[it]
                } ?: pools.maxApyPool
            }
        }
    }

    /* * */

    private val _selectedImplementationFlow = MutableStateFlow<StakingPoolImplementationExt?>(null)
    private val selectedImplementationFlow =
        _selectedImplementationFlow.asStateFlow().filterNotNull()

    private val _selectedPoolFlow = MutableStateFlow<PoolEntity?>(null)
    val selectedPoolFlow = _selectedPoolFlow.asStateFlow().filterNotNull()

    private val _confirmedPoolFlow = MutableStateFlow<PoolEntity?>(null)
    private val confirmedPoolFlow = _confirmedPoolFlow.asStateFlow().filterNotNull()

    private val _implementationUiItemsFlow = MutableStateFlow<List<BaseListItem>>(emptyList())
    val implementationUiItemsFlow =
        _implementationUiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    private val _poolUiItemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val poolUiItemsFlow = _poolUiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    private fun buildImplementationsItems(
        pools: StakingPoolsEntity,
        amountInputState: AmountScreenState
    ) {
        val implList = pools.implementations.map { it.value }

        val liquidImplList = implList.filter { it.isLiquid }.sortedBy { it.maxApy }.reversed()
        val notLiquidImplList = implList.filter { !it.isLiquid }.sortedBy { it.maxApy }.reversed()


        val uiItems = mutableListOf<BaseListItem>()

        if (liquidImplList.isNotEmpty()) {
            uiItems.add(Item.TitleH3(Localization.staking_liquid))

            for ((index, item) in liquidImplList.withIndex()) {
                uiItems.add(
                    Item.PoolImplementation(
                        position = ListCell.getPosition(liquidImplList.size, index),
                        name = item.implementation.name,
                        iconRes = item.implementation.type.icon,
                        minStakeNano = BigInteger.valueOf(item.minStake),
                        maxApy = item.maxApy,
                        poolsCount = item.pools.size,
                        isMaxApy = item.implementation.type == pools.maxApyPool?.implementation?.type,
                        isChecked = item.implementation.type == amountInputState.pool.implementation.type,
                        onClickListener = { onImplementationClick(item) },
                    )
                )
            }
        }

        if (notLiquidImplList.isNotEmpty()) {
            if (uiItems.isNotEmpty()) {
                uiItems.add(Item.Offset16)
                uiItems.add(Item.TitleH3(Localization.other))
            }
            for ((index, item) in notLiquidImplList.withIndex()) {
                uiItems.add(
                    Item.PoolImplementation(
                        position = ListCell.getPosition(notLiquidImplList.size, index),
                        name = item.implementation.name,
                        iconRes = item.implementation.type.icon,
                        minStakeNano = BigInteger.valueOf(item.minStake),
                        maxApy = item.maxApy,
                        poolsCount = item.pools.size,
                        isMaxApy = item.implementation.type == pools.maxApyPool?.implementation?.type,
                        isChecked = item.implementation.type == amountInputState.pool.implementation.type,
                        onClickListener = { onImplementationClick(item) },
                    )
                )
            }
        }

        _implementationUiItemsFlow.value = uiItems
    }

    private fun buildPoolsItems(
        pools: StakingPoolsEntity,
        impl: StakingPoolImplementationExt,
        amountInputState: AmountScreenState
    ) {
        val poolsList = impl.pools.sortedBy { it.name }

        val uiItems = mutableListOf<Item>()
        for ((index, item) in poolsList.withIndex()) {
            uiItems.add(
                Item.Pool(
                    position = ListCell.getPosition(impl.pools.size, index),
                    iconRes = item.implementation.type.icon,
                    name = item.name,
                    apy = item.apy,
                    isMaxApy = item == pools.maxApyPool,
                    isChecked = item.address == amountInputState.pool.address,
                    onClickListener = { onPoolClick(item) }
                )
            )
        }

        _poolUiItemsFlow.value = uiItems
    }


    val inputAmountController = AmountInputController()

    data class AmountScreenState(
        val input: AmountInputController.State,
        val token: BalanceEntity,
        val wallet: WalletEntity,
        val rates: RatesEntity,
        val pools: StakingPoolsEntity,
        val pool: PoolEntity,
    ) {
        val fiatFmt
            get() = CurrencyFormatter.format(
                rates.currency.code,
                rates.convert(
                    TokenEntity.TON.address,
                    input.input.coin?.toFloat(input.inputState.decimals) ?: 0f
                )
            )
    }

    private val _amountScreenStateFlow = MutableStateFlow<AmountScreenState?>(null)
    val amountScreenStateFlow = _amountScreenStateFlow.asStateFlow().filterNotNull()


    private val _confirmScreenStateFlow = MutableStateFlow<ConfirmScreenState?>(null)
    private val confirmScreenStateFlow = _confirmScreenStateFlow.asStateFlow().filterNotNull()

    data class ConfirmScreenState(
        val walletEntity: WalletEntity,
        val currency: WalletCurrency,
        val token: BalanceEntity,
        val rates: RatesEntity,
        val amount: AmountInputController.State,
        val pool: PoolEntity
    ) : TransactionEmulator.Request {
        fun build(): StakingTlb.MessageData = StakingTlb.buildStakeTxParams(
            poolType = pool.implementation.type,
            poolAddress = MsgAddressInt.parse(pool.address),
            queryId = TransactionData.getWalletQueryId(),
            amount = Coins.ofNano(amount.valueNano)
        )

        private fun createTransfer(): WalletTransfer {
            val data = build()

            val fee = StakingTlb.getDepositFee(pool.implementation.type)
            val value = data.gasAmount.amount.value + fee.amount.value

            val builder = WalletTransferBuilder()
            builder.bounceable = true
            builder.destination = data.to
            builder.body = data.payload
            builder.sendMode = SendMode.PAY_GAS_SEPARATELY.value + SendMode.IGNORE_ERRORS.value
            builder.coins = Coins.ofNano(value)
            return builder.build()
        }

        override fun getWallet(): WalletEntity = walletEntity

        override fun getTransfer(): WalletTransfer = createTransfer()

        val valueFmt = CurrencyFormatter.format("TON", Coin.toCoins(amount.valueNano))
        val valueCurrencyFmt = CurrencyFormatter.formatFiat(
            currency.code,
            rates.convert(TokenEntity.TON.address, Coin.toCoins(amount.valueNano))
        )

        fun getFeeFmt(fee: BigInteger) = "≈ " + CurrencyFormatter.format("TON", Coin.toCoins(fee))

        fun getFeeInCurrencyFmt(fee: BigInteger) = "≈ " + CurrencyFormatter.formatFiat(
            currency.code,
            rates.convert(TokenEntity.TON.address, Coin.toCoins(fee))
        )
    }

    fun openConfirmPage() {
        _amountScreenStateFlow.value?.let { state ->
            _confirmScreenStateFlow.value = ConfirmScreenState(
                walletEntity = state.wallet,
                amount = state.input,
                currency = settingsRepository.currency,
                rates = ratesRepository.cache(
                    settingsRepository.currency,
                    listOf(TokenEntity.TON.address)
                ),
                pool = state.pool,
                token = state.token
            )
            _pageStateFlow.value = _pageStateFlow.value.copy(confirmVisibility = true)
        }
    }


    /* * */

    fun setHeaderDividerPoolsList(value: Boolean) {
        _pageStateFlow.value = _pageStateFlow.value.copy(headerDividerPoolsList = value)
    }




    private val _pageStateFlow = MutableStateFlow(
        PagerState(
            selectorVisibility = false,
            selectorPage = 0,
            selectorPrevPage = -1,
            confirmVisibility = false
        )
    )
    val pageStateFlow = _pageStateFlow.asStateFlow()

    data class PagerState(
        private val headerDividerPoolsList: Boolean = false,
        val confirmVisibility: Boolean,
        val selectorVisibility: Boolean,
        val selectorPage: Int,
        val selectorPrevPage: Int
    ) {
        val showInformationIcon: Boolean get() = !confirmVisibility && !selectorVisibility

        val headerDivider: Boolean get() {
            if (selectorVisibility && selectorPage == 1) {
                return headerDividerPoolsList
            }

            return false
        }
    }

    fun openPoolSelector() {
        _pageStateFlow.value = _pageStateFlow.value.copy(
            selectorVisibility = true,
            selectorPage = 0,
            selectorPrevPage = -1
        )
    }

    fun prevPage() {
        val state = _pageStateFlow.value
        if (state.confirmVisibility) {
            _pageStateFlow.value = state.copy(confirmVisibility = false)
        } else if (state.selectorVisibility) {
            _pageStateFlow.value = when (state.selectorPage) {
                0 -> state.copy(selectorVisibility = false)
                1 -> state.copy(selectorPage = 0, selectorPrevPage = 1)
                2 -> state.copy(
                    selectorPage = if (_selectedImplementationFlow.value?.pools?.size == 1) 0 else 1,
                    selectorPrevPage = 2
                )

                else -> state
            }
        }
    }

    private fun onImplementationClick(impl: StakingPoolImplementationExt) {
        _selectedImplementationFlow.value = impl
        _selectedPoolFlow.value = impl.pools.first()
        _pageStateFlow.value = _pageStateFlow.value.copy(
            selectorPage = if (impl.pools.size == 1) 2 else 1,
            selectorPrevPage = _pageStateFlow.value.selectorPage
        )
    }

    private fun onPoolClick(pool: PoolEntity) {
        _selectedPoolFlow.value = pool
        _pageStateFlow.value = _pageStateFlow.value.copy(
            selectorPage = 2,
            selectorPrevPage = _pageStateFlow.value.selectorPage
        )
    }

    fun onPoolConfirm(pool: PoolEntity) {
        _confirmedPoolFlow.value = pool
        _pageStateFlow.value = _pageStateFlow.value.copy(selectorVisibility = false)
    }


    /* * */

    fun send(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionSender.send(context, _confirmScreenStateFlow.value!!)
        }
    }

    val transactionEmulationFlow =
        TransactionEmulator.makeTransactionEmulatorFlow(api, confirmScreenStateFlow)
    val transactionSender = TransactionSender(api, passcodeRepository, walletManager, viewModelScope)


    /* * */

    val poolsFlow = combine(
        poolsRepository.storageFlow,
        walletRepository.activeWalletFlow,
    ) { storage, wallet ->
        storage.get(
            StakingPoolsRepository.Request(
                accountId = wallet.accountId,
                testnet = wallet.testnet
            )
        )?.result
    }.filterNotNull()

    init {
        combine(
            walletRepository.activeWalletFlow,
            networkMonitor.isOnlineFlow
        ) { wallet, isOnline ->
            if (!isOnline) {
                return@combine null
            }

            poolsRepository.doRequest(
                StakingPoolsRepository.Request(
                    accountId = wallet.accountId,
                    testnet = wallet.testnet
                )
            )
        }.launchIn(viewModelScope)

        combine(tokensFlow2, confirmedPoolFlow) { tokens, pool ->
            var maxAmount = tokens.token.nano - StakingTlb.getDepositFee(pool.implementation.type).amount.value - BigInteger.valueOf(200_000_000)
            if (maxAmount < BigInteger.ZERO) {
                maxAmount = BigInteger.ZERO
            }

            inputAmountController.setInputParams(
                pool.minStake.toBigInteger(),
                maxAmount,
                TokenEntity.TON.decimals,
                TokenEntity.TON.symbol
            )
        }.launchIn(viewModelScope)

        combine(
            poolsFlow,
            tokensFlow2,
            confirmedPoolFlow,
            inputAmountController.outputStateFlow
        ) { pools, tokens, pool, input ->
            _amountScreenStateFlow.value = AmountScreenState(
                input = input,
                token = tokens.token,
                wallet = tokens.wallet,
                rates = ratesRepository.cache(
                    settingsRepository.currency,
                    listOf(TokenEntity.TON.address)
                ),
                pools = pools,
                pool = pool
            )
        }.launchIn(viewModelScope)

        combine(poolsFlow, amountScreenStateFlow, this::buildImplementationsItems).launchIn(
            viewModelScope
        )
        combine(
            poolsFlow,
            selectedImplementationFlow,
            amountScreenStateFlow,
            this::buildPoolsItems
        ).launchIn(viewModelScope)
    }

    val headerTextFlow = combine(
        pageStateFlow,
        _selectedImplementationFlow,
        _selectedPoolFlow
    ) { state, impl, pool ->
        if (state.confirmVisibility) {
            ""
        } else if (!state.selectorVisibility) {
            "Stake"
        } else if (state.selectorPage == 0) {
            "Options"
        } else if (state.selectorPage == 1) {
            impl?.implementation?.name ?: ""
        } else if (state.selectorPage == 2) {
            pool?.name ?: ""
        } else {
            ""
        }
    }
}