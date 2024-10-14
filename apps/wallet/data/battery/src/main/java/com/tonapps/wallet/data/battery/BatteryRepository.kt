package com.tonapps.wallet.data.battery

import android.content.Context
import androidx.collection.ArrayMap
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.extensions.filterList
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.battery.entity.BatteryConfigEntity
import com.tonapps.wallet.data.battery.entity.BatteryBalanceEntity
import com.tonapps.wallet.data.battery.source.LocalDataSource
import com.tonapps.wallet.data.battery.source.RemoteDataSource
import io.tonapi.models.MessageConsequences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell

class BatteryRepository(
    context: Context,
    private val api: API,
    scope: CoroutineScope,
) {
    private val localDataSource = LocalDataSource(context)
    private val remoteDataSource = RemoteDataSource(api)

    private val _balanceUpdatedFlow = MutableEffectFlow<Unit>()
    val balanceUpdatedFlow = _balanceUpdatedFlow.asSharedFlow()

    init {
        _balanceUpdatedFlow.tryEmit(Unit)
        scope.launch(Dispatchers.IO) {
            getConfig(false, ignoreCache = true)
        }
    }

    suspend fun getConfig(
        testnet: Boolean,
        ignoreCache: Boolean = false
    ): BatteryConfigEntity = withContext(Dispatchers.IO) {
        if (ignoreCache) {
            fetchConfig(testnet)
        } else {
            localDataSource.getConfig(testnet) ?: fetchConfig(testnet)
        }
    }

    private suspend fun fetchConfig(testnet: Boolean): BatteryConfigEntity {
        val config = remoteDataSource.fetchConfig(testnet) ?: return BatteryConfigEntity.Empty
        localDataSource.setConfig(testnet, config)
        return config
    }

    suspend fun getBalance(
        tonProofToken: String,
        publicKey: PublicKeyEd25519,
        testnet: Boolean,
        ignoreCache: Boolean = false,
    ): BatteryBalanceEntity = withContext(Dispatchers.IO) {
        val balance = if (ignoreCache) {
            fetchBalance(publicKey, tonProofToken, testnet)
        } else {
            localDataSource.getBalance(publicKey, testnet) ?: fetchBalance(publicKey, tonProofToken, testnet)
        }
        balance
    }

    private suspend fun fetchBalance(
        publicKey: PublicKeyEd25519,
        tonProofToken: String,
        testnet: Boolean
    ): BatteryBalanceEntity {
        val balance = remoteDataSource.fetchBalance(tonProofToken, testnet) ?: return BatteryBalanceEntity.Empty
        localDataSource.setBalance(publicKey, testnet, balance)
        _balanceUpdatedFlow.emit(Unit)
        return balance
    }

    suspend fun emulate(
        tonProofToken: String,
        publicKey: PublicKeyEd25519,
        testnet: Boolean,
        boc: Cell,
        forceRelayer: Boolean = false
    ): Pair<MessageConsequences, Boolean>? = withContext(Dispatchers.IO) {

        val balance = getBalance(
            tonProofToken = tonProofToken,
            publicKey = publicKey,
            testnet = testnet
        ).balance

        if (!forceRelayer && !balance.isPositive) {
            throw IllegalStateException("Zero balance")
        }

        api.emulateWithBattery(tonProofToken, boc, testnet)
    }

    suspend fun getAppliedPromo(
        testnet: Boolean,
    ): String? = withContext(Dispatchers.IO) {
        localDataSource.getAppliedPromo(testnet)
    }

    suspend fun setAppliedPromo(
        testnet: Boolean,
        promo: String?,
    ) = withContext(Dispatchers.IO) {
        localDataSource.setAppliedPromo(testnet, promo)
    }

    suspend fun getPreferGasless(
        testnet: Boolean,
    ): Boolean = withContext(Dispatchers.IO) {
        localDataSource.getPreferGasless(testnet)
    }

    suspend fun setPreferGasless(
        testnet: Boolean,
        value: Boolean,
    ) = withContext(Dispatchers.IO) {
        localDataSource.setPreferGasless(testnet, value)
    }

}