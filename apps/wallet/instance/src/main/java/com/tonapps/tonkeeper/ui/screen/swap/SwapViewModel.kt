package com.tonapps.tonkeeper.ui.screen.swap

import android.util.Log
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.tokenModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SwapViewModel(state: SwapViewState, tokenRepository: TokenRepository?, lifecycleScope: CoroutineScope) {
    private var _stateFlow = MutableStateFlow(state)
    private var accountId: Long? = null
    val stateFlow: StateFlow<SwapViewState> = _stateFlow.asStateFlow()

    init{
        lifecycleScope.launch {
            val wallet = App.walletManager.getWalletInfo()!!
            val accountId = wallet.accountId
            val tokens = tokenRepository!!.get(WalletCurrency.TON, wallet.address, wallet.testnet)
            val token = tokens.firstOrNull { it.isTon }
            val balance = CurrencyFormatter.format("TON", token!!.balance.value).toString()
            val temp = _stateFlow.value
            _stateFlow.value = SwapViewState(
                temp.fromTokenTitle, temp.toTokenTitle,
                temp.fromTokenIcon, temp.toTokenIcon,
                temp.toAmount, temp.fromAmount,
                temp.swapButton, balance
            )
        }
    }

    fun swapTokens(){
        val temp = _stateFlow.value
        _stateFlow.value = SwapViewState(
            temp.toTokenTitle, temp.fromTokenTitle,
            temp.toTokenIcon, temp.fromTokenIcon,
            temp.toAmount, temp.fromAmount,
            temp.swapButton, temp.balance)
    }
}