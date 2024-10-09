package com.tonapps.tonkeeper.ui.screen.ledger.steps

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.live.ble.BleManager
import com.ledger.live.ble.BleManagerFactory
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.blockchain.ton.extensions.toWalletAddress
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.icu.Coins
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.ledger.ble.BleTransport
import com.tonapps.ledger.devices.Devices
import com.tonapps.ledger.ton.AccountPath
import com.tonapps.ledger.ton.LedgerAccount
import com.tonapps.ledger.ton.LedgerConnectData
import com.tonapps.ledger.ton.TonPayloadFormat
import com.tonapps.ledger.ton.TonTransport
import com.tonapps.ledger.ton.Transaction
import com.tonapps.ledger.transport.TransportStatusException
import com.tonapps.tonkeeper.extensions.isVersionLowerThan
import com.tonapps.tonkeeper.ui.screen.init.list.AccountItem
import com.tonapps.tonkeeper.ui.screen.ledger.steps.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.collectibles.CollectiblesRepository
import com.tonapps.wallet.data.collectibles.entities.NftEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.Localization
import io.ktor.util.reflect.instanceOf
import io.tonapi.models.Account
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uikit.extensions.context
import java.math.BigDecimal
import java.math.BigInteger

data class ProofData(
    val domain: String,
    val timestamp: BigInteger,
    val payload: String,
)

class LedgerConnectionViewModel(
    app: Application,
    private val accountRepository: AccountRepository,
    private val tokenRepository: TokenRepository,
    private val collectiblesRepository: CollectiblesRepository,
    private val settingsRepository: SettingsRepository,
    private val api: API,
) : AndroidViewModel(app) {
    private val bleManager: BleManager = BleManagerFactory.newInstance(context)
    private var pollTonAppJob: Job? = null
    private var disconnectTimeoutJob: Job? = null

    private var _walletId: String? = null
    private var _transaction: Transaction? = null
    private var _proofData: ProofData? = null
    private var _connectedDevice: ConnectedDevice? = null
    private var _tonTransport: TonTransport? = null

    private val _connectionState = MutableEffectFlow<ConnectionState>()

    private val _eventFlow = MutableEffectFlow<LedgerEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val currentStepFlow = _connectionState.map { state ->
        when (state) {
            ConnectionState.Idle -> LedgerStep.CONNECT
            ConnectionState.Scanning -> LedgerStep.CONNECT
            is ConnectionState.Connected -> LedgerStep.OPEN_TON_APP
            is ConnectionState.TonAppOpened -> if (_walletId != null) LedgerStep.CONFIRM_TX else LedgerStep.DONE
            is ConnectionState.Disconnected -> LedgerStep.CONNECT
            ConnectionState.Signed -> LedgerStep.DONE
        }
    }

    val uiItemsFlow = currentStepFlow.map { step ->
        createList(step)
    }

    val displayTextFlow = currentStepFlow.map { state ->
        when {
            _walletId != null && (state == LedgerStep.CONFIRM_TX || state == LedgerStep.DONE) -> "Review"
            else -> "TON ready"
        }
    }

    private val _bluetoothState = MutableSharedFlow<Int>(replay = 1, extraBufferCapacity = 1)
    val bluetoothState = _bluetoothState.asSharedFlow()

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                _bluetoothState.tryEmit(state)
            }
        }
    }

    init {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothReceiver, filter)

        // Get BluetoothManager and BluetoothAdapter
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        // Emit initial state
        val initialState = bluetoothAdapter?.state ?: BluetoothAdapter.ERROR
        _bluetoothState.tryEmit(initialState)
        _connectionState.tryEmit(ConnectionState.Idle)
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(bluetoothReceiver)
    }

    fun setSignData(transaction: Transaction, walletId: String) {
        _transaction = transaction
        _walletId = walletId
    }

    fun setProofData(domain: String, timestamp: BigInteger, payload: String, walletId: String) {
        _proofData = ProofData(domain, timestamp, payload)
        _walletId = walletId
    }

    private fun setConnectedDevice(device: ConnectedDevice?) {
        _connectedDevice = device
    }

    private fun setTonTransport(transport: TonTransport?) {
        _tonTransport = transport

        _eventFlow.tryEmit(LedgerEvent.Ready(_tonTransport != null))
    }

    fun scanOrConnect() {
        if (_walletId != null) {
            viewModelScope.launch {
                try {
                    val ledgerConfig = getLedgerConfig()
                    connect(ledgerConfig.deviceId)
                } catch (e: Exception) {
                    _eventFlow.tryEmit(
                        LedgerEvent.Error(
                            e.message ?: context.getString(Localization.error)
                        )
                    )
                }
            }
        } else {
            scan()
        }
    }

    private fun scan() {
        _connectionState.tryEmit(ConnectionState.Scanning)
        bleManager.startScanning {
            val device = it.first()

            bleManager.stopScanning()
            connect(device.id)
        }
    }

    private fun connect(deviceId: String) {
        bleManager.connect(address = deviceId, onConnectError = {
            pollTonAppJob?.cancel()

            viewModelScope.launch {
                delay(1000)
                Log.d("LEDGER", "Reconnecting to device $deviceId")
                connect(deviceId)
            }

            disconnectTimeoutJob?.cancel()
            disconnectTimeoutJob = viewModelScope.launch {
                delay(4000)
                setTonTransport(null)
                setConnectedDevice(null)
                _connectionState.tryEmit(ConnectionState.Disconnected())
            }
        }, onConnectSuccess = {
            disconnectTimeoutJob?.cancel()
            setConnectedDevice(ConnectedDevice(deviceId, Devices.fromServiceUuid(it.serviceId!!)))
            _connectionState.tryEmit(ConnectionState.Connected)

            waitForTonAppOpen()
        })
    }

    fun disconnect() {
        disconnectTimeoutJob?.cancel()
        pollTonAppJob?.cancel()
        bleManager.disconnect()
        setConnectedDevice(null)
        setTonTransport(null)
        _connectionState.tryEmit(ConnectionState.Disconnected())
    }

    suspend fun getConnectData() {
        if (_tonTransport == null || _connectedDevice == null) {
            return
        }
        try {
            _eventFlow.tryEmit(LedgerEvent.Loading(true))
            val accounts = mutableListOf<LedgerAccount>()
            for (i in 0 until 10) {
                val account = _tonTransport!!.getAccount(AccountPath(i))
                accounts.add(account)
            }

            val ledgerConnectData = LedgerConnectData(
                accounts = accounts,
                deviceId = _connectedDevice!!.deviceId,
                model = _connectedDevice!!.model
            )

            try {
                resolveWallets(ledgerConnectData)
            } catch (e: Exception) {
                _eventFlow.tryEmit(LedgerEvent.Loading(false))
            }
        } catch (e: Exception) {
            _eventFlow.tryEmit(LedgerEvent.Error(context.getString(Localization.error)))
            _eventFlow.tryEmit(LedgerEvent.Loading(false))
        }
    }

    suspend fun signDomainProof() {
        viewModelScope.launch {
            try {
                if (_tonTransport == null || _connectedDevice == null || _walletId == null || _proofData == null) {
                    throw IllegalStateException()
                }

                val version = _tonTransport!!.getVersion()
                val requiredVersion = "2.1.0"

                if (version.isVersionLowerThan(requiredVersion)) {
                    _eventFlow.tryEmit(LedgerEvent.WrongVersion(requiredVersion))
                    return@launch
                }

                val ledgerConfig = getLedgerConfig()

                val proof = _tonTransport!!.signAddressProof(
                    AccountPath(ledgerConfig.accountIndex),
                    _proofData!!.domain,
                    _proofData!!.timestamp,
                    _proofData!!.payload,
                )

                _connectionState.tryEmit(ConnectionState.Signed)
                _eventFlow.tryEmit(LedgerEvent.SignedProof(proof))
            } catch (e: Exception) {
                Log.d("LEDGER", "Error signing transaction", e)
                if (e.instanceOf(TransportStatusException.DeniedByUser::class)) {
                    _eventFlow.tryEmit(LedgerEvent.Rejected)
                } else {
                    _eventFlow.tryEmit(
                        LedgerEvent.Error(
                            e.message ?: context.getString(Localization.error)
                        )
                    )
                }

            }
        }
    }

    suspend fun signTransaction() {
        viewModelScope.launch {
            try {
                if (_tonTransport == null || _connectedDevice == null || _walletId == null || _transaction == null) {
                    throw IllegalStateException()
                }

                val version = _tonTransport!!.getVersion()
                val requiredVersion = getRequiredVersion(_transaction!!)

                if (version.isVersionLowerThan(requiredVersion)) {
                    _eventFlow.tryEmit(LedgerEvent.WrongVersion(requiredVersion))
                    return@launch
                }

                val ledgerConfig = getLedgerConfig()

                val signedBody = _tonTransport!!.signTransaction(
                    AccountPath(ledgerConfig.accountIndex),
                    _transaction!!
                )

                _connectionState.tryEmit(ConnectionState.Signed)
                _eventFlow.tryEmit(LedgerEvent.SignedTransaction(signedBody))
            } catch (e: Exception) {
                Log.d("LEDGER", "Error signing transaction", e)
                if (e.instanceOf(TransportStatusException.DeniedByUser::class)) {
                    _eventFlow.tryEmit(LedgerEvent.Rejected)
                } else if (e.instanceOf(TransportStatusException.BlindSigningDisabled::class)) {
                    _eventFlow.tryEmit(
                        LedgerEvent.Error(context.getString(Localization.ledger_blind_signing_error))
                    )
                } else {
                    _eventFlow.tryEmit(
                        LedgerEvent.Error(e.message ?: context.getString(Localization.error))
                    )
                }

            }
        }
    }

    private fun getRequiredVersion(transaction: Transaction): String {
        val defaultVersion = "2.0.0"

        if (transaction.payload == null) {
            return defaultVersion
        }

        return when (transaction.payload!!::class) {
            TonPayloadFormat.JettonTransfer::class -> {
                defaultVersion
            }

            TonPayloadFormat.Comment::class -> {
                defaultVersion
            }

            else -> {
                "2.1.0"
            }
        }
    }

    private suspend fun getLedgerConfig(): WalletEntity.Ledger {
        val wallet = accountRepository.getWallets().find { it.id == _walletId }
            ?: throw IllegalStateException("Wallet not found")
        val ledgerConfig = wallet.ledger
            ?: throw IllegalStateException("Ledger data not found")
        return ledgerConfig
    }

    private suspend fun resolveWallets(ledgerData: LedgerConnectData) =
        withContext(Dispatchers.IO) {
            val addedDeviceAccountIndexes = accountRepository.getWallets()
                .filter { wallet -> wallet.ledger?.deviceId == ledgerData.deviceId }
                .map { it.ledger!!.accountIndex }

            val deferredAccounts = mutableListOf<Deferred<Account?>>()
            for (account in ledgerData.accounts) {
                deferredAccounts.add(async {
                    api.resolveAccount(account.address.toAccountId(), false)
                })
            }

            val deferredTokens = mutableListOf<Deferred<List<AccountTokenEntity>>>()
            for (account in ledgerData.accounts) {
                deferredTokens.add(async {
                    tokenRepository.get(
                        settingsRepository.currency,
                        account.address.toAccountId(),
                        false
                    ) ?: emptyList()
                })
            }

            val deferredCollectibles = mutableListOf<Deferred<List<NftEntity>>>()
            for (account in ledgerData.accounts) {
                deferredCollectibles.add(async {
                    collectiblesRepository.get(
                        account.address.toAccountId(),
                        false
                    ) ?: emptyList()
                })
            }

            val items = mutableListOf<AccountItem>()
            for ((index, ledgerAccount) in ledgerData.accounts.withIndex()) {
                val account = deferredAccounts[index].await()
                val balance = Coins.of(account.let { it?.balance } ?: 0)
                val tokens = deferredTokens[index].await()
                    .filter { it.balance.value.value > BigDecimal.ZERO }
                val hasTokens = tokens.isNotEmpty()
                val hasCollectibles = deferredCollectibles[index].await().isNotEmpty()
                val alreadyAdded = addedDeviceAccountIndexes.contains(ledgerAccount.path.index)
                val item = AccountItem(
                    address = ledgerAccount.address.toWalletAddress(false),
                    name = account?.name,
                    walletVersion = WalletVersion.V4R2,
                    balanceFormat = CurrencyFormatter.format("TON", balance),
                    tokens = hasTokens,
                    collectibles = hasCollectibles,
                    selected = false,
                    position = ListCell.getPosition(ledgerData.accounts.size, index),
                    ledgerIndex = ledgerAccount.path.index,
                    ledgerAdded = alreadyAdded
                )
                items.add(item)
            }

            _eventFlow.tryEmit(LedgerEvent.Next(connectData = ledgerData, accounts = items))
        }

    private fun waitForTonAppOpen() {
        setTonTransport(null)
        pollTonAppJob?.cancel()
        pollTonAppJob = viewModelScope.launch {
            val tonTransport = TonTransport(BleTransport(bleManager))

            suspend fun isAppOpen() = try {
                tonTransport.isAppOpen()
            } catch (_: Exception) {
                false
            }

            while (!isAppOpen()) {
                Log.d("LEDGER", "Waiting for app to open")
                delay(1000)
            }

            _connectionState.tryEmit(ConnectionState.TonAppOpened)
            setTonTransport(tonTransport)
        }
    }

    private fun createList(currentStep: LedgerStep): List<Item> {
        val uiItems = mutableListOf<Item>()

        uiItems.add(
            Item.Step(
                context.getString(Localization.ledger_connect),
                currentStep !== LedgerStep.CONNECT,
                currentStep == LedgerStep.CONNECT
            )
        )

        uiItems.add(
            Item.Step(
                context.getString(Localization.ledger_open_ton_app),
                currentStep == LedgerStep.DONE || currentStep == LedgerStep.CONFIRM_TX,
                currentStep == LedgerStep.OPEN_TON_APP,
                _walletId == null
            )
        )

        if (_walletId != null) {
            uiItems.add(
                Item.Step(
                    if (_proofData !== null) context.getString(Localization.ledger_confirm_proof) else context.getString(
                        Localization.ledger_confirm_tx
                    ),
                    currentStep == LedgerStep.DONE,
                    currentStep == LedgerStep.CONFIRM_TX
                )
            )
        }

        return uiItems
    }
}