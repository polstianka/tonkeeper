package com.tonapps.wallet.data.tonconnect

import android.content.Context
import android.util.Log
import com.tonapps.blockchain.ton.TonNetwork
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.extensions.prefs
import com.tonapps.security.CryptoBox
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletProof
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.ProofDomainEntity
import org.ton.crypto.base64
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.tonconnect.entities.DAppEntity
import com.tonapps.wallet.data.tonconnect.entities.DAppItemEntity
import com.tonapps.wallet.data.tonconnect.entities.DAppManifestEntity
import com.tonapps.wallet.data.tonconnect.entities.reply.DAppAddressItemEntity
import com.tonapps.wallet.data.tonconnect.entities.reply.DAppEventSuccessEntity
import com.tonapps.wallet.data.tonconnect.entities.reply.DAppProofItemReplySuccess
import com.tonapps.wallet.data.tonconnect.entities.reply.DAppReply
import com.tonapps.wallet.data.tonconnect.entities.reply.DAppSuccessEntity
import com.tonapps.wallet.data.tonconnect.source.LocalDataSource
import com.tonapps.wallet.data.tonconnect.source.RemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.block.AddrStd
import org.ton.block.StateInit

class TonConnectRepository(
    private val scope: CoroutineScope,
    private val context: Context,
    private val api: API,
    private val walletRepository: WalletRepository,
) {

    private val localDataSource = LocalDataSource(context)
    private val remoteDataSource = RemoteDataSource(api)
    private val prefs = context.prefs("tonconnect")
    private val events = EventsHelper(prefs, walletRepository, api)

    private val _appsFlow = MutableStateFlow<List<DAppEntity>?>(null)
    val appsFlow = _appsFlow.asStateFlow().filterNotNull()

    val eventsFlow = events.flow(appsFlow).onEach {
        if (it.method == "disconnect") {
            disconnect(it.app)
        }
    }.filterNotNull().flowOn(Dispatchers.IO).shareIn(scope, SharingStarted.Eagerly, 1)

    init {
        scope.launch(Dispatchers.IO) {
            _appsFlow.value = localDataSource.getApps()
        }
    }

    fun setPushEnabled(walletId: Long, url: String, enabled: Boolean) {
        val app = localDataSource.getApp(walletId, url) ?: return
        if (app.enablePush != enabled) {
            setPushEnabled(app, enabled)
        }
    }

    fun setPushEnabled(app: DAppEntity, enabled: Boolean) {
        if (app.enablePush != enabled) {
            val newApp = app.copy(enablePush = enabled)
            localDataSource.updateApp(newApp)
            _appsFlow.value = _appsFlow.value?.map {
                if (it.clientId == app.clientId) newApp else it
            }
        }
    }

    fun disconnect(app: DAppEntity) {
        localDataSource.deleteApp(app.walletId, app.url)
        _appsFlow.value = _appsFlow.value?.filter { app.clientId != it.clientId }
    }

    fun getApps(urls: List<String>, wallet: WalletEntity): List<DAppEntity> {
        return urls.mapNotNull { getApp(it, wallet) }.distinctBy { it.publicKeyHex }
    }

    fun getApp(url: String, wallet: WalletEntity): DAppEntity? {
        val apps = localDataSource.getApps()
        return apps.find {
            it.url.startsWith(url) && it.walletId == wallet.id
        }
    }

    suspend fun getManifest(sourceUrl: String): DAppManifestEntity? = withContext(Dispatchers.IO) {
        try {
            val local = localDataSource.getManifest(sourceUrl)
            if (local == null) {
                val remote = remoteDataSource.loadManifest(sourceUrl)
                localDataSource.setManifest(sourceUrl, remote)
                remote
            } else {
                local
            }
        } catch (e: Throwable) {
            null
        }
    }

    suspend fun newApp(
        manifest: DAppManifestEntity,
        accountId: String,
        testnet: Boolean,
        clientId: String,
        walletId: Long,
        enablePush: Boolean,
    ): DAppEntity = withContext(Dispatchers.IO) {
        val keyPair = CryptoBox.keyPair()
        val app = DAppEntity(
            url = manifest.url,
            accountId = accountId,
            testnet = testnet,
            clientId = clientId,
            keyPair = keyPair,
            walletId = walletId,
            enablePush = enablePush,
            manifest = manifest,
        )
        localDataSource.addApp(app)
        val oldValue = _appsFlow.value ?: emptyList()
        _appsFlow.value = oldValue.plus(app)
        app
    }

    suspend fun send(
        app: DAppEntity,
        body: String,
    ) = withContext(Dispatchers.IO) {
        val encrypted = app.encrypt(body)
        api.tonconnectSend(app.publicKeyHex, app.clientId, base64(encrypted))
    }

    suspend fun send(
        app: DAppEntity,
        body: JSONObject,
    ) = send(app, body.toString())

    suspend fun sendError(
        requestId: String,
        app: DAppEntity,
        errorCode: Int,
        errorMessage: String
    ) {
        val error = JSONObject()
        error.put("code", errorCode)
        error.put("message", errorMessage)

        val json = JSONObject()
        json.put("id", requestId)
        json.put("error", error)

        send(app, json.toString())
    }

    suspend fun send(
        requestId: String,
        app: DAppEntity,
        result: String
    ) {
        val data = DAppSuccessEntity(requestId, result)
        send(app, data.toJSON())
    }

    suspend fun subscribePush(
        wallet: WalletEntity,
        app: DAppEntity,
        firebaseToken: String
    ) {
        val proofToken = walletRepository.getTonProofToken(wallet.id) ?: return
        val url = app.url.removeSuffix("/")
        api.pushTonconnectSubscribe(
            token = proofToken,
            appUrl = url,
            accountId = wallet.address,
            firebaseToken = firebaseToken,
            sessionId = app.clientId,
            commercial = true,
            silent = false,
        )
    }

    suspend fun subscribePush(
        app: DAppEntity,
        firebaseToken: String
    ) {
        val wallet = walletRepository.getWallet(app.walletId) ?: return
        subscribePush(wallet, app, firebaseToken)
    }

    suspend fun updatePushToken(firebaseToken: String) = withContext(Dispatchers.IO) {
        val apps = localDataSource.getApps()
        for (app in apps) {
            subscribePush(app, firebaseToken)
        }
    }

    suspend fun connect(
        wallet: WalletEntity,
        privateKey: PrivateKeyEd25519,
        manifest: DAppManifestEntity,
        clientId: String,
        requestItems: List<DAppItemEntity>,
        firebaseToken: String?,
    ): DAppEventSuccessEntity = withContext(Dispatchers.IO) {
        val enablePush = firebaseToken != null
        val app = newApp(manifest, wallet.accountId, wallet.testnet, clientId, wallet.id, enablePush)
        val items = createItems(app, wallet, privateKey, requestItems)
        val res = DAppEventSuccessEntity(items)
        send(app, res.toJSON())
        firebaseToken?.let {
            subscribePush(wallet, app, it)
        }
        res.copy()
    }

    suspend fun autoConnect(
        wallet: WalletEntity,
    ): DAppEventSuccessEntity = withContext(Dispatchers.IO) {
        val items = mutableListOf<DAppReply>()
        items.add(createAddressItem(
            accountId = wallet.accountId,
            testnet = wallet.testnet,
            publicKey = wallet.publicKey,
            stateInit = wallet.contract.stateInit
        ))
        DAppEventSuccessEntity(items)
    }

    private fun createItems(
        app: DAppEntity,
        wallet: WalletEntity,
        privateKey: PrivateKeyEd25519,
        items: List<DAppItemEntity>
    ): List<DAppReply> {
        val result = mutableListOf<DAppReply>()
        for (requestItem in items) {
            if (requestItem.name == DAppItemEntity.TON_ADDR) {
                result.add(createAddressItem(
                    accountId = wallet.accountId,
                    testnet = wallet.testnet,
                    publicKey = wallet.publicKey,
                    stateInit = wallet.contract.stateInit
                ))
            } else if (requestItem.name == DAppItemEntity.TON_PROOF) {
                result.add(createProofItem(
                    payload = requestItem.payload ?: "",
                    domain = app.domain,
                    address = wallet.contract.address,
                    privateWalletKey = privateKey,
                    stateInit = wallet.contract.getStateCell().base64()
                ))
            }
        }
        return result
    }

    private fun createProofItem(
        payload: String,
        domain: ProofDomainEntity,
        address: AddrStd,
        privateWalletKey: PrivateKeyEd25519,
        stateInit: String,
    ): DAppProofItemReplySuccess {
        val proof = WalletProof.sign(
            address,
            privateWalletKey,
            payload,
            domain,
            stateInit
        )
        return DAppProofItemReplySuccess(proof = proof)
    }

    private fun createAddressItem(
        accountId: String,
        testnet: Boolean,
        publicKey: PublicKeyEd25519,
        stateInit: StateInit
    ): DAppAddressItemEntity {
        return DAppAddressItemEntity(
            address = accountId,
            network = if (testnet) TonNetwork.TESTNET else TonNetwork.MAINNET,
            walletStateInit = stateInit,
            publicKey = publicKey
        )
    }
}