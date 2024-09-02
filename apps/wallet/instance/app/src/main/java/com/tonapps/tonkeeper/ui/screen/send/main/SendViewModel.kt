package com.tonapps.tonkeeper.ui.screen.send.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.ton.extensions.EmptyPrivateKeyEd25519
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.extensions.state
import com.tonapps.icu.Coins
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.totalFees
import com.tonapps.tonkeeper.core.AnalyticsHelper
import com.tonapps.tonkeeper.core.entities.SendMetadataEntity
import com.tonapps.tonkeeper.core.entities.TransferEntity
import com.tonapps.tonkeeper.extensions.with
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.tonkeeper.ui.screen.send.main.helper.SendNftHelper
import com.tonapps.tonkeeper.ui.screen.send.main.state.SendAmountState
import com.tonapps.tonkeeper.ui.screen.send.main.state.SendDestination
import com.tonapps.tonkeeper.ui.screen.send.main.state.SendTransaction
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.account.Wallet
import com.tonapps.wallet.data.battery.BatteryRepository
import com.tonapps.wallet.data.collectibles.CollectiblesRepository
import com.tonapps.wallet.data.collectibles.entities.NftEntity
import com.tonapps.wallet.data.passcode.PasscodeManager
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.bitstring.BitString
import org.ton.block.AddrStd
import org.ton.cell.Cell
import uikit.extensions.collectFlow
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@OptIn(FlowPreview::class)
class SendViewModel(
    app: Application,
    private val nftAddress: String,
    private val accountRepository: AccountRepository,
    private val api: API,
    private val settingsRepository: SettingsRepository,
    private val tokenRepository: TokenRepository,
    private val ratesRepository: RatesRepository,
    private val passcodeManager: PasscodeManager,
    private val collectiblesRepository: CollectiblesRepository,
    private val batteryRepository: BatteryRepository,
): BaseWalletVM(app) {

    private val isNft: Boolean
        get() = nftAddress.isNotBlank()

    data class UserInput(
        val address: String = "",
        val amount: Coins = Coins.ZERO,
        val token: TokenEntity = TokenEntity.TON,
        val comment: String? = null,
        val nft: NftEntity? = null,
        val encryptedComment: Boolean = false,
        val max: Boolean = false,
        val amountCurrency: Boolean = false,
    )

    private val currency = settingsRepository.currency
    private val queryId: BigInteger by lazy { TransferEntity.newWalletQueryId() }

    private val _userInputFlow = MutableStateFlow(UserInput())
    private val userInputFlow = _userInputFlow.asStateFlow()

    private var lastTransferEntity: TransferEntity? = null

    private var isBattery = false

    val walletTypeFlow = accountRepository.selectedWalletFlow.map { it.type }

    private val userInputAddressFlow = userInputFlow
        .map { it.address }
        .distinctUntilChanged()
        .debounce { if (it.isEmpty()) 0 else 600 }

    private val destinationFlow = combine(
        accountRepository.selectedWalletFlow,
        userInputAddressFlow,
    ) { wallet, address ->
        if (address.isEmpty()) {
            return@combine SendDestination.Empty
        }
        getDestinationAccount(address, wallet.testnet)
    }.flowOn(Dispatchers.IO).state(viewModelScope)

    private val tokensFlow = accountRepository.selectedWalletFlow.map { wallet ->
        tokenRepository.get(currency, wallet.accountId, wallet.testnet) ?: emptyList()
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val selectedTokenFlow = combine(
        tokensFlow,
        userInputFlow.map { it.token }.distinctUntilChanged()
    ) { tokens, selectedToken ->
        tokens.find { it.address == selectedToken.address } ?: AccountTokenEntity.EMPTY
    }.distinctUntilChanged().flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.Eagerly, AccountTokenEntity.EMPTY)

    private val ratesTokenFlow = selectedTokenFlow.map { token ->
        ratesRepository.getRates(currency, token.address)
    }.state(viewModelScope)

    val uiInputAddressErrorFlow = destinationFlow.map { it is SendDestination.NotFound }

    private val _uiInputAmountFlow = MutableEffectFlow<Coins>()
    val uiInputAmountFlow = _uiInputAmountFlow.asSharedFlow()

    val uiInputTokenFlow = userInputFlow.map { it.token }.filter { !isNft }.distinctUntilChanged()

    val uiInputNftFlow = userInputFlow.map { it.nft }.distinctUntilChanged().filterNotNull()

    val uiRequiredMemoFlow =
        destinationFlow.map { it as? SendDestination.Account }.map { it?.memoRequired == true }

    val uiExistingTargetFlow = destinationFlow.map { it as? SendDestination.Account }.map { it?.existing == true }

    val uiEncryptedCommentAvailableFlow = combine(
        uiRequiredMemoFlow,
        walletTypeFlow,
        uiExistingTargetFlow,
    ) { requiredMemo, walletType, existingTarget ->
        existingTarget && !requiredMemo && (walletType == Wallet.Type.Default || walletType == Wallet.Type.Testnet || walletType == Wallet.Type.Lockup)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val uiInputEncryptedComment = combine(
        userInputFlow.map { it.encryptedComment }.distinctUntilChanged(),
        uiEncryptedCommentAvailableFlow,
    ) { encryptedComment, available ->
        encryptedComment && available
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val uiInputComment = userInputFlow.map { it.comment }.distinctUntilChanged()

    private val uiInputAmountCurrency = userInputFlow.map { it.amountCurrency }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val inputAmountFlow = userInputFlow.map { it.amount }.distinctUntilChanged()

    private val _uiEventFlow = MutableEffectFlow<SendEvent>()
    val uiEventFlow = _uiEventFlow.asSharedFlow()

    val uiBalanceFlow = combine(
        selectedTokenFlow,
        inputAmountFlow,
        ratesTokenFlow,
        uiInputAmountCurrency,
    ) { token, amount, rates, amountCurrency ->
        val (balance, currencyCode) = if (amountCurrency) {
            Pair(token.fiat, currency.code)
        } else {
            Pair(token.balance.value, token.symbol)
        }

        val remaining = balance - amount

        val convertedCode = if (amountCurrency) token.symbol else currency.code
        val converted = if (amountCurrency) {
            rates.convertFromFiat(token.address, amount)
        } else {
            rates.convert(token.address, amount)
        }

        val remainingToken = if (!amountCurrency) {
            token.balance.value - amount
        } else {
            rates.convertFromFiat(token.address, token.fiat - amount)
        }

        val remainingFormat = CurrencyFormatter.format(token.symbol, remainingToken, 2, RoundingMode.DOWN, false)

        SendAmountState(
            remainingFormat = getString(Localization.remaining_balance, remainingFormat),
            converted = converted.stripTrailingZeros(),
            convertedFormat = CurrencyFormatter.format(convertedCode, converted, 2, RoundingMode.DOWN, false),
            insufficientBalance = if (remaining.isZero) false else remaining.isNegative,
            currencyCode = if (amountCurrency) currencyCode else "",
            amountCurrency = amountCurrency,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SendAmountState())

    val uiButtonEnabledFlow = combine(
        destinationFlow,
        uiBalanceFlow,
        inputAmountFlow,
        uiInputComment,
    ) { recipient, balance, amount, comment ->
        if (recipient !is SendDestination.Account) {
            false
        } else if (recipient.memoRequired && comment.isNullOrEmpty()) {
            false
        } else if (isNft || amount.isPositive) {
            true
        } else if (balance.insufficientBalance) {
            false
        } else {
            false
            // (isNft || (!balance.insufficientBalance && (amount.isPositive || amount.isZero)))
        }
    }

    private val amountTokenFlow = combine(
        selectedTokenFlow,
        inputAmountFlow,
        ratesTokenFlow,
        uiInputAmountCurrency,
    ) { token, amount, rates, amountCurrency ->
        if (!amountCurrency) {
            amount
        } else {
            /*val converted = rates.convertFromFiat(token.address, amount)
            val diff = token.balance.value.diff(converted)
            if (99.7f >= diff || 100.3f >= diff) {
                token.balance.value
            } else {
                converted
            }*/
            rates.convertFromFiat(token.address, amount)
        }
    }

    private val uiTransferAmountFlow = combine(
        amountTokenFlow,
        selectedTokenFlow,
        ratesTokenFlow,
    ) { amount, token, rates ->
        SendTransaction.Amount(
            value = amount,
            converted = rates.convert(token.address, amount),
            format = CurrencyFormatter.format(
                token.symbol,
                amount,
                token.decimals,
                RoundingMode.UP,
                false
            ),
            convertedFormat = CurrencyFormatter.format(
                currency.code,
                rates.convert(token.address, amount),
                token.decimals,
                RoundingMode.UP,
                false
            ),
        )
    }

    val uiTransactionFlow = combine(
        accountRepository.selectedWalletFlow,
        destinationFlow.mapNotNull { it as? SendDestination.Account },
        selectedTokenFlow,
        uiTransferAmountFlow,
        userInputFlow,
    ) { wallet, destination, token, amount, userInput ->
        SendTransaction(
            fromWallet = wallet,
            destination = destination,
            token = token.balance,
            comment = userInput.comment,
            encryptedComment = userInput.encryptedComment,
            amount = amount,
            max = userInput.max
        )
    }

    private val transferFlow = combine(
        accountRepository.selectedWalletFlow.distinctUntilChanged(),
        uiTransactionFlow.distinctUntilChanged(),
        userInputFlow.map { Pair(it.comment, it.encryptedComment) }.distinctUntilChanged(),
        selectedTokenFlow,
    ) { wallet, transaction, (comment, encryptedComment), token ->
        val sendMetadata = getSendParams(wallet)
        val builder = TransferEntity.Builder(wallet)
        builder.setToken(transaction.token)
        builder.setDestination(transaction.destination.address, transaction.destination.publicKey)
        builder.setSeqno(sendMetadata.seqno)
        builder.setQueryId(queryId)
        builder.setComment(comment, encryptedComment)
        builder.setValidUntil(sendMetadata.validUntil)
        if (isNft) {
            val amount =
                getNftTotalAmount(wallet, sendMetadata, transaction.destination.address, comment)
            builder.setNftAddress(nftAddress)
            builder.setBounceable(true)
            builder.setAmount(amount)
            builder.setMax(false)
        } else {
            builder.setMax(transaction.isRealMax(token.balance.value))
            builder.setAmount(transaction.amount.value)
            builder.setBounceable(transaction.destination.isBounce)
        }
        builder.build()
    }.flowOn(Dispatchers.IO).shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    init {
        if (isNft) {
            loadNft()
        }
    }

    fun initializeTokenAndAmount(tokenAddress: String, amountNano: Long) {
        if (amountNano > 0) {
            collectFlow(uiInputTokenFlow.filter {
                it.address.equals(tokenAddress, ignoreCase = true)
            }.take(1)) { token ->
                val amount = Coins.of(amountNano, token.decimals)
                _uiInputAmountFlow.tryEmit(amount)
            }
        }
        userInputTokenByAddress(tokenAddress)
    }

    private suspend fun getNftTotalAmount(
        wallet: WalletEntity,
        sendMetadata: SendMetadataEntity,
        destination: AddrStd,
        comment: String?
    ): Coins = withContext(Dispatchers.IO) {
        SendNftHelper.totalAmount(
            nftAddress = AddrStd.parse(nftAddress),
            api = api,
            wallet = wallet,
            seqno = sendMetadata.seqno,
            destination = destination,
            validUntil = sendMetadata.validUntil,
            comment = comment,
        )
    }

    private suspend fun getDestinationAccount(
        address: String,
        testnet: Boolean
    ) = withContext(Dispatchers.IO) {
        val accountDeferred = async { api.resolveAccount(address, testnet) }
        val publicKeyDeferred = async { api.safeGetPublicKey(address, testnet) }

        val account = accountDeferred.await() ?: return@withContext SendDestination.NotFound
        val publicKey = publicKeyDeferred.await()

        SendDestination.Account(address, publicKey, account)
    }

    private fun isInsufficientBalance(): Boolean {
        val token = selectedTokenFlow.value
        val amount = userInputFlow.value.amount
        val amountCurrency = userInputFlow.value.amountCurrency
        val balance = if (amountCurrency) token.fiat else token.balance.value
        try {
            val percentage = amount.value.divide(balance.value, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP)
            return percentage > BigDecimal("95.00") && percentage <= BigDecimal("99.99")
        } catch (e: Throwable) {
            return false
        }
    }

    fun next() {
        if (isInsufficientBalance()) {
            _uiEventFlow.tryEmit(SendEvent.InsufficientBalance)
            return
        }
        _uiEventFlow.tryEmit(SendEvent.Confirm)
        transferFlow.take(1).map { calculateFee(it) }.map { (coins, isBattery) -> eventFee(coins, isBattery) }.filterNotNull().onEach {
            _uiEventFlow.tryEmit(it)
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }

    private fun loadNft() {
        accountRepository.selectedWalletFlow.take(1).map { wallet ->
            collectiblesRepository.getNft(wallet.accountId, wallet.testnet, nftAddress)?.let {
                val pref = settingsRepository.getTokenPrefs(wallet.id, it.address)
                it.with(pref)
            }
        }.flowOn(Dispatchers.IO).filterNotNull().onEach(::userInputNft).launchIn(viewModelScope)
    }

    private fun shouldAttemptWithRelayer(transfer: TransferEntity): Boolean {
        if ((transfer.isTon && !transfer.isNft) || transfer.wallet.isExternal) {
            return false
        }

        val transactionType = if (transfer.isNft) {
            BatteryTransaction.NFT
        } else {
            BatteryTransaction.JETTON
        }

        return settingsRepository.batteryIsEnabledTx(transfer.wallet.accountId, transactionType)
    }

    private suspend fun calculateFee(
        transfer: TransferEntity,
        retryWithoutRelayer: Boolean = false,
    ): Pair<Coins, Boolean> = withContext(Dispatchers.IO) {
        val fakePrivateKey = if (transfer.commentEncrypted) {
            PrivateKeyEd25519()
        } else {
            EmptyPrivateKeyEd25519
        }
        val wallet = transfer.wallet
        val withRelayer = shouldAttemptWithRelayer(transfer)

        if (withRelayer && !retryWithoutRelayer) {
            try {
                if (api.config.isBatteryDisabled) {
                    throw IllegalStateException("Battery is disabled")
                }
                val tonProofToken = accountRepository.requestTonProofToken(wallet) ?: throw IllegalStateException("Can't find TonProof token")

                val message = transfer.toSignedMessage(fakePrivateKey, true)
                val (consequences, withBattery) = batteryRepository.emulate(
                    tonProofToken = tonProofToken,
                    publicKey = wallet.publicKey,
                    testnet = wallet.testnet,
                    boc = message
                ) ?: throw IllegalStateException("Failed to emulate battery")

                isBattery = withBattery
                Pair(Coins.of(consequences.totalFees), withBattery)
            } catch (e: Throwable) {
                calculateFee(transfer, true)
            }
        } else {
            val message = transfer.toSignedMessage(fakePrivateKey, false)
            val fee = api.emulate(message, transfer.wallet.testnet)?.totalFees ?: 0

            isBattery = false

            Pair(Coins.of(fee), false)
        }
    }

    private fun eventFee(
        coins: Coins,
        isBattery: Boolean
    ): SendEvent.Fee? {
        return try {
            val code = TokenEntity.TON.symbol
            val rates = ratesRepository.getRates(currency, code)
            val converted = rates.convert(code, coins)

            SendEvent.Fee(
                value = coins,
                format = CurrencyFormatter.format(code, coins, TokenEntity.TON.decimals),
                convertedFormat = CurrencyFormatter.format(
                    currency.code,
                    converted,
                    currency.decimals
                ),
                isBattery = isBattery,
            )
        } catch (e: Throwable) {
            null
        }
    }

    fun userInputEncryptedComment(encrypted: Boolean) {
        _userInputFlow.value = _userInputFlow.value.copy(encryptedComment = encrypted)
    }

    fun userInputAmount(amount: Coins) {
        _userInputFlow.value = _userInputFlow.value.copy(amount = amount)
    }

    fun userInputToken(token: TokenEntity) {
        _userInputFlow.value = _userInputFlow.value.copy(token = token)
    }

    fun userInputNft(nft: NftEntity) {
        _userInputFlow.value = _userInputFlow.value.copy(nft = nft)
    }

    fun userInputTokenByAddress(tokenAddress: String) {
        combine(
            accountRepository.selectedWalletFlow.take(1),
            tokensFlow.filter { it.isNotEmpty() }.map { list ->
                list.find { it.address == tokenAddress }
            }.map { it?.balance?.token }
        ) { wallet, token ->
            token ?: tokenRepository.getToken(tokenAddress, wallet.testnet) ?: TokenEntity.TON
        }.take(1).flowOn(Dispatchers.IO).onEach { token ->
            userInputToken(token)
        }.launchIn(viewModelScope)
    }

    fun userInputAddress(address: String) {
        _userInputFlow.update { it.copy(address = address) }
    }

    fun userInputComment(comment: String?) {
        _userInputFlow.update { it.copy(comment = comment?.trim()) }
    }

    fun swap() {
        val balance = uiBalanceFlow.value.copy()
        val amountCurrency =
            _userInputFlow.updateAndGet { it.copy(amountCurrency = !it.amountCurrency) }.amountCurrency
        if (amountCurrency != balance.amountCurrency) {
            _uiInputAmountFlow.tryEmit(balance.converted)
        }
    }

    fun setMax() {
        collectFlow(uiInputAmountCurrency.take(1)) { amountCurrency ->
            val token = selectedTokenFlow.value
            val coins = if (amountCurrency) {
                token.fiat
            } else {
                token.balance.value
            }
            _uiInputAmountFlow.tryEmit(coins)
        }
    }

    private suspend fun getSendParams(
        wallet: WalletEntity,
    ): SendMetadataEntity = withContext(Dispatchers.IO) {
        val seqnoDeferred = async { accountRepository.getSeqno(wallet) }
        val validUntilDeferred = async { accountRepository.getValidUntil(wallet.testnet) }

        val seqno = seqnoDeferred.await()
        val validUntil = validUntilDeferred.await()

        SendMetadataEntity(
            seqno = seqno,
            validUntil = validUntil,
        )
    }

    fun signerData() = combine(
        accountRepository.selectedWalletFlow.take(1),
        transferFlow.take(1)
    ) { wallet, transfer ->
        lastTransferEntity = transfer
        Pair(wallet.publicKey, transfer.getUnsignedBody())
    }

    fun ledgerData() = combine(
        accountRepository.selectedWalletFlow.take(1),
        transferFlow.take(1)
    ) { wallet, transfer ->
        lastTransferEntity = transfer
        Pair(wallet.id, transfer.getLedgerTransaction())
    }

    fun sendSignedMessage(signature: BitString) {
        transferFlow.take(1).map { transfer ->
            Pair(transfer.transferMessage(signature), transfer.wallet)
        }.sendTransfer()
    }

    fun sendLedgerSignedMessage(body: Cell) {
        transferFlow.take(1).map { transfer ->
            Pair(transfer.transferMessage(body), transfer.wallet)
        }.sendTransfer()
    }

    fun send(context: Context) {
        AnalyticsHelper.trackEvent("send_transaction")
        transferFlow.take(1).map { transfer ->
            val wallet = transfer.wallet
            if (!wallet.hasPrivateKey) {
                throw SendException.UnableSendTransaction()
            }
            val isValidPasscode =
                passcodeManager.confirmation(context, context.getString(Localization.app_name))
            if (!isValidPasscode) {
                throw SendException.WrongPasscode()
            }
            val privateKey = accountRepository.getPrivateKey(wallet.id)
            val excessesAddress = if (isBattery) batteryRepository.getConfig(wallet.testnet).excessesAddress else null
            val boc = transfer.toSignedMessage(privateKey, isBattery, excessesAddress)
            Pair(boc, wallet)
        }.sendTransfer()
    }

    private suspend fun sendToBlockchain(message: Cell, wallet: WalletEntity) {
        val success = if (isBattery) {
            val tonProofToken = accountRepository.requestTonProofToken(wallet) ?: throw IllegalStateException("Can't find TonProof token")
            api.sendToBlockchainWithBattery(message, tonProofToken, wallet.testnet)
        } else {
            api.sendToBlockchain(message, wallet.testnet)
        }

        if (!success) {
            throw SendException.FailedToSendTransaction()
        }
    }

    private fun Flow<Pair<Cell, WalletEntity>>.sendTransfer() {
        this.map { (boc, wallet) ->
            sendToBlockchain(boc, wallet)
            AnalyticsHelper.trackEvent("send_success")
        }.catch {
            _uiEventFlow.tryEmit(SendEvent.Failed)
        }.flowOn(Dispatchers.IO).onEach {
            _uiEventFlow.tryEmit(SendEvent.Success)
        }.launchIn(viewModelScope)
    }
}
