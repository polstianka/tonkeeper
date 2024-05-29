package com.tonapps.wallet.data.account

import android.content.Context
import android.net.Uri
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.MessageBodyEntity
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.entities.WalletEvent
import com.tonapps.wallet.data.account.entities.WalletLabel
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer
import org.ton.mnemonic.Mnemonic

class WalletRepository(
    private val context: Context,
    private val scope: CoroutineScope,
    private val legacyManager: WalletManager,
    private val api: API
) {

    private val extras = Extras(context, api)

    private val _walletsFlow = MutableStateFlow<List<WalletEntity>?>(null)
    val walletsFlow = _walletsFlow.asStateFlow().filterNotNull()

    private val _activeWalletFlow = MutableStateFlow<WalletEntity?>(null)
    val activeWalletFlow = _activeWalletFlow.asStateFlow().filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    val realtimeEventsFlow = activeWalletFlow.flatMapLatest { wallet ->
        api.accountEvents(wallet.accountId, wallet.testnet).map { event ->
            val isBoc = event.json.has("boc")
            if (isBoc) {
                WalletEvent.Boc(wallet, event.json)
            } else {
                WalletEvent.Transaction(wallet, event.json)
            }
        }
    }.catch { }.flowOn(Dispatchers.IO).shareIn(scope, SharingStarted.Eagerly, 1)

    init {
        scope.launch(Dispatchers.IO) {
            updateWallets()
        }
    }

    fun removeCurrent() {
        scope.launch {
            val wallet = activeWalletFlow.firstOrNull() ?: return@launch
            legacyManager.clear(wallet.id)
            updateWallets()
        }
    }

    suspend fun getTonProofToken(walletId: Long): String? = withContext(Dispatchers.IO) {
        val value = extras.getTonProofToken(walletId)
        if (value != null) {
            return@withContext value
        }
        try {
            val wallet = getWallet(walletId) ?: return@withContext null
            if (!wallet.hasPrivateKey) {
                return@withContext null
            }
            val newValue = createTonProofToken(wallet)
            extras.setTonProofToken(walletId, newValue)
            newValue
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun createTonProofToken(wallet: WalletEntity): String {
        val secretKey = getPrivateKey(wallet.id)
        val contract = wallet.contract
        val address = contract.address
        val payload = api.tonconnectPayload()
        val proof = WalletProof.signTonkeeper(
            address = address,
            secretKey = secretKey,
            payload = payload,
            stateInit = contract.getStateCell().base64()
        )
        return api.tonconnectProof(address.toAccountId(), Json.encodeToString(proof))
    }

    suspend fun getWallet(accountId: String): WalletEntity? {
        val legacyWallet = legacyManager.getWallets().find { it.accountId == accountId } ?: return null
        return WalletEntity(legacyWallet)
    }

    suspend fun getWalletByAddress(address: String): WalletEntity? {
        val legacyWallet = legacyManager.getWallets().find { it.address == address } ?: return null
        return WalletEntity(legacyWallet)
    }

    suspend fun getWallet(id: Long): WalletEntity? {
        val legacyWallet = legacyManager.getWallets().find { it.id == id } ?: return null
        return WalletEntity(legacyWallet)
    }

    fun chooseWallet(id: Long) {
        scope.launch {
            setActiveWallet(id)
        }
    }

    suspend fun setActiveWallet(id: Long) = withContext(Dispatchers.IO) {
        val activeWalletId = legacyManager.getActiveWallet()
        if (activeWalletId == id) {
            return@withContext
        }
        legacyManager.setActiveWallet(id)?.let {
            _activeWalletFlow.value = WalletEntity(it)
        }
    }

    suspend fun editLabel(
        id: Long,
        name: String,
        emoji: String,
        color: Int
    ) {
        val legacyWallet = legacyManager.edit(id, name, emoji, color) ?: return
        val walletEntity = WalletEntity(legacyWallet)
        _activeWalletFlow.value = walletEntity
        _walletsFlow.value = _walletsFlow.value?.map {
            if (it.id == id) {
                walletEntity
            } else {
                it
            }
        }
    }

    fun saveLabel(
        name: String,
        emoji: String,
        color: Int
    ) {
        scope.launch(Dispatchers.IO) {
            editLabel(name, emoji, color)
        }
    }

    suspend fun editLabel(
        name: String,
        emoji: String,
        color: Int
    ) {
        val id = legacyManager.getActiveWallet()
        editLabel(id, name, emoji, color)
    }

    private suspend fun updateWallets() {
        val activeWalletId = legacyManager.getActiveWallet()
        val wallets = getWallets()
        _walletsFlow.value = wallets
        _activeWalletFlow.value = wallets.find { it.id == activeWalletId }
    }

    suspend fun createNewWallet(label: WalletLabel) {
        val mnemonic = Mnemonic.generate()
        val legacy = legacyManager.addWallet(
            mnemonic = mnemonic,
            name = label.name,
            emoji = label.emoji,
            color = label.color,
            testnet = false,
            source = WalletSource.Default,
            version = WalletVersion.V4R2
        )

        updateWallets()
    }

    suspend fun addWatchWallet(
        publicKey: PublicKeyEd25519,
        label: WalletLabel,
        version: WalletVersion,
        source: WalletSource
    ) {
        val legacy = legacyManager.addWatchWallet(
            publicKey = publicKey,
            name = label.name,
            emoji = label.emoji,
            color = label.color,
            singer = false,
            version = version,
            source = source
        )

        updateWallets()
    }

    suspend fun addWallets(
        mnemonic: List<String>,
        publicKey: PublicKeyEd25519,
        versions: List<WalletVersion>,
        name: String? = null,
        emoji: CharSequence,
        color: Int,
        testnet: Boolean
    ) {
        for (version in versions) {
            val nameWithVersion = if (versions.size > 1) {
                "$name ${version.title}"
            } else {
                name
            }
            legacyManager.addWallet(
                mnemonic = mnemonic,
                publicKey = publicKey,
                version = version,
                name = nameWithVersion ?: "Wallet",
                emoji = emoji,
                color = color,
                testnet = testnet,
                source = WalletSource.Default
            )
        }

        updateWallets()
    }

    suspend fun addSignerWallet(
        publicKey: PublicKeyEd25519,
        name: String,
        emoji: CharSequence,
        color: Int,
        source: WalletSource
    ) {
        legacyManager.addWatchWallet(
            publicKey = publicKey,
            name = name,
            emoji = emoji,
            color = color,
            singer = true,
            version = WalletVersion.V4R2,
            source = source
        )

        updateWallets()
    }

    suspend fun getMnemonic(id: Long): Array<String> = withContext(Dispatchers.IO) {
        val mnemonic = legacyManager.getMnemonic(id)
        mnemonic.toTypedArray()
    }

    suspend fun getPrivateKey(id: Long): PrivateKeyEd25519 = withContext(Dispatchers.IO) {
        legacyManager.getPrivateKey(id)
    }

    suspend fun messageBody(
        wallet: WalletEntity,
        validUntil: Long,
        transfers: List<WalletTransfer>
    ): MessageBodyEntity {
        val seqno = api.getAccountSeqno(wallet.accountId, wallet.testnet)
        val body = wallet.createBody(seqno, validUntil, transfers)
        return MessageBodyEntity(seqno, body)
    }

    suspend fun clear() {
        legacyManager.clearAll()
        _walletsFlow.value = null
        _activeWalletFlow.value = null
    }

    suspend fun createSignedMessage(
        wallet: WalletEntity,
        privateKeyEd25519: PrivateKeyEd25519,
        validUntil: Long,
        transfers: List<WalletTransfer>
    ): Cell {
        val data = messageBody(wallet, validUntil, transfers)
        return wallet.sign(privateKeyEd25519, data.seqno, data.body)
    }

    private suspend fun getWallets(): List<WalletEntity> {
        val list = mutableListOf<WalletEntity>()
        val legacyWallets = legacyManager.getWallets()
        for (legacyWallet in legacyWallets) {
            list.add(WalletEntity(legacyWallet))
        }
        return list
    }

    fun walletAddress(jettonMaster: String, owner: String): String {
        // https://api.ston.fi/v1/jetton/${jettonMaster}/address?owner_address=${owner}
        val url = Uri.parse("${API.STONFI_API_URL}/jetton")
            .buildUpon()
            .appendPath(jettonMaster)
            .appendPath("address")
            .appendQueryParameter("owner_address", owner)
            .build()
            .toString()
        val response = api.stonfiApi(url)
        return response.getString("address")
    }

    suspend fun walletSeqno(wallet: WalletEntity): Int {
        return api.getAccountSeqno(wallet.accountId, wallet.testnet)
    }
 }