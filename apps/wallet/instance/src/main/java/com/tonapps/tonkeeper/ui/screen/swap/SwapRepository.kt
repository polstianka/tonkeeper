package com.tonapps.tonkeeper.ui.screen.swap

import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.token.TokenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SwapRepository(
    private val walletManager: WalletManager,
    private val tokenRepository: TokenRepository,
) {

    private var _sendToken = MutableStateFlow<AssetModel?>(null)
    val sendToken: StateFlow<AssetModel?> = _sendToken

    private var _receiveToken = MutableStateFlow<AssetModel?>(null)
    val receiveToken: StateFlow<AssetModel?> = _receiveToken

    suspend fun init() {
        walletManager.getWalletInfo()?.let {
            tokenRepository.get(WalletCurrency.TON, it.accountId, it.testnet).firstOrNull()
                ?.let { token ->
                    setSendToken(
                        AssetModel(
                            token = TokenEntity.TON,
                            balance = token.balance.value,
                            walletAddress = it.address,
                            position = ListCell.Position.SINGLE,
                            fiatBalance = 0f
                        )
                    )
                }
        }
    }

    fun setSendToken(model: AssetModel) {
        _sendToken.value = model
    }

    fun setReceiveToken(model: AssetModel) {
        _receiveToken.value = model
    }

    fun swap() {
        val tempReceive = _receiveToken.value
        _receiveToken.value = _sendToken.value
        _sendToken.value = tempReceive
    }

    fun simulateSwap() {

    }

}