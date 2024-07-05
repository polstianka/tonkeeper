package com.tonapps.tonkeeper.ui.screen.browser.dapp

import androidx.lifecycle.ViewModel
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.tonconnect.TonConnectRepository
import com.tonapps.wallet.data.tonconnect.entities.DAppEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.take
import uikit.extensions.collectFlow

class DAppViewModel(
    private val url: String,
    private val walletRepository: WalletRepository,
    private val tonConnectRepository: TonConnectRepository
): ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getApp() = walletRepository.activeWalletFlow.mapLatest {
        tonConnectRepository.getApp(url, it)
    }.flowOn(Dispatchers.IO).take(1)

    fun mute() {
        collectFlow(getApp().filterNotNull()) { app ->
            tonConnectRepository.setPushEnabled(app, false)
        }
    }

    fun disconnect() {
        collectFlow(getApp().filterNotNull()) { app ->
            tonConnectRepository.disconnect(app)
        }
    }

    suspend fun restoreConnection(url: String): String {
        val (wallet, _) = get(url)
        val reply = tonConnectRepository.autoConnect(wallet)
        return reply.toJSON().toString()
    }

    private suspend fun get(url: String): Pair<WalletEntity, DAppEntity> {
        val wallet = walletRepository.activeWalletFlow.firstOrNull() ?: throw IllegalStateException("No active wallet")
        val app = tonConnectRepository.getApp(url, wallet) ?: throw IllegalStateException("No app")
        return wallet to app
    }
}