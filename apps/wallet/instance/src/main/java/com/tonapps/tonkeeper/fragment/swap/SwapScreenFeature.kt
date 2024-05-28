package com.tonapps.tonkeeper.fragment.swap

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.fragment.swap.currency.CurrencyScreenState
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo
import com.tonapps.tonkeeper.fragment.swap.pager.SwapScreenAdapter
import com.tonapps.wallet.data.jetton.JettonRepository
import com.tonapps.wallet.data.token.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ton.TonAddress

class SwapScreenFeature(
    private val tokenRepository: TokenRepository,
    private val jettonRepository: JettonRepository,
) : ViewModel() {

    private val _uiEffect = MutableSharedFlow<SwapScreenEffect>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEffect: SharedFlow<SwapScreenEffect> = _uiEffect.asSharedFlow()

    private val _uiState = MutableStateFlow<SwapScreenState>(SwapScreenState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _sendToken = MutableStateFlow<TokenInfo?>(null)
    val sendToken = _sendToken.asStateFlow()

    private val _receiveToken = MutableStateFlow<TokenInfo?>(null)
    val receiveToken = _receiveToken.asStateFlow()

    private val _resultState = MutableStateFlow<CurrencyScreenState?>(null)
    val resultState = _resultState.asStateFlow()

    var tokens: List<TokenInfo> = emptyList()
        private set

    fun load() = viewModelScope.launch(Dispatchers.IO) {
        val wallet = App.walletManager.getWalletInfo()!!
        val accountId = wallet.accountId
        val currency = App.settings.currency
        val accountTokens = tokenRepository.get(currency, accountId, wallet.testnet)
        val allTokens = jettonRepository.get(testnet = wallet.testnet)

        val resultTokens = allTokens.map { token ->
            val accountToken = if (token.isTon) {
                accountTokens.find { it.isTon }
            } else {
                accountTokens.find {
                    if (it.isTon) {
                        false
                    } else {
                        TonAddress(it.address) == TonAddress(token.contractAddress)
                    }
                }
            }

            val balanceTon = accountToken?.let { at ->
                CurrencyFormatter.format(currency = at.symbol, value = at.balance.value)
            } ?: CurrencyFormatter.format(value = 0F)

            val balanceFiat = accountToken?.let { at ->
                CurrencyFormatter.formatFiat(at.rate?.currency?.code.orEmpty(), at.fiat)
            } ?: CurrencyFormatter.format(value = 0F)

            TokenInfo(
                name = token.displayName,
                symbol = token.symbol,
                balance = balanceTon.toString(),
                decimals = token.decimals,
                balanceFiat = balanceFiat.toString(),
                iconUri = token.imageUrl.orEmpty().toUri(),
                contractAddress = token.contractAddress,
                priority = token.priority,
                tonTag = token.symbol == "USD₮"
            )
        }.sortedWith { first, second ->
            val firstBalance = first.balanceFiat.replace("[^\\d.-]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val secondBalance = second.balanceFiat.replace("[^\\d.-]".toRegex(), "").toDoubleOrNull() ?: 0.0

            when {
                first.isTon && !second.isTon -> -1
                !first.isTon && second.isTon -> 1
                first.isTon && second.isTon -> first.symbol.compareTo(second.symbol, true)
                firstBalance != secondBalance -> secondBalance.compareTo(firstBalance)
                else -> first.symbol.compareTo(second.symbol, true)
            }
        }

        tokens = resultTokens
        _uiState.emit(SwapScreenState.Content())
        _sendToken.emit(resultTokens.firstOrNull())
    }

    fun nextPage() {
        updateContentState { it.copy(currentPage = it.currentPage + 1) }
        closeMessage()
    }

    fun prevPage() {
        updateContentState { it.copy(currentPage = it.currentPage - 1) }
        closeMessage()
    }

    fun setCurrentPage(index: Int) {
        updateContentState { it.copy(currentPage = index) }
        closeMessage()
    }

    fun onActionClick() {
        when (val currentState = _uiState.value) {
            is SwapScreenState.Content -> {
                if (currentState.currentPage == SwapScreenAdapter.POSITION_CURRENCY) {
                    _uiEffect.tryEmit(SwapScreenEffect.OpenSettings)
                } else {
                    _uiEffect.tryEmit(SwapScreenEffect.Back)
                }
            }

            else -> Unit
        }
    }

    fun updateResultState(state: CurrencyScreenState) {
        _resultState.update { state }
    }

    fun showMessage(text: String) {
        _uiEffect.tryEmit(SwapScreenEffect.ShowMessage(text))
    }

    fun finish() {
        _uiEffect.tryEmit(SwapScreenEffect.Finish)
    }

    private fun closeMessage() {
        _uiEffect.tryEmit(SwapScreenEffect.CloseMessage)
    }

    private fun updateContentState(action: (SwapScreenState.Content) -> SwapScreenState) {
        _uiState.update { oldState ->
            when (oldState) {
                is SwapScreenState.Content -> {
                    action.invoke(oldState)
                }

                else -> oldState
            }
        }
    }
}