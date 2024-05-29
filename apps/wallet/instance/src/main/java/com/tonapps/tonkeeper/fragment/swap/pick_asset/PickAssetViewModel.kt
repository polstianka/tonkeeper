package com.tonapps.tonkeeper.fragment.swap.pick_asset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.pick_asset.rv.TokenListHelper
import com.tonapps.tonkeeper.fragment.swap.pick_asset.rv.TokenListItem
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class PickAssetViewModel(
    private val dexAssetsRepository: DexAssetsRepository,
    private val listHelper: TokenListHelper,
    walletRepository: WalletRepository,
    settings: SettingsRepository
) : ViewModel() {

    private val args = MutableSharedFlow<PickAssetArgs>(replay = 1)
    private val _events = MutableSharedFlow<PickAssetEvent>()

    val events: Flow<PickAssetEvent>
        get() = _events
    val items = listHelper.items
        .flowOn(Dispatchers.Default)
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val domainItems = combine(
        walletRepository.activeWalletFlow,
        settings.currencyFlow,
        args
    ) { wallet, currency, args ->
        Triple(wallet, currency, args.pickedItems)
    }
        .flatMapLatest { (wallet, currency, pickedItems) ->
            dexAssetsRepository.getTotalBalancesFlow(wallet.address, wallet.testnet, currency)
                .map { list -> list.filter { !pickedItems.contains(it.tokenEntity) } }
        }

    init {
        observeFlow(domainItems) { listHelper.submitItems(it) }
    }

    fun provideArgs(args: PickAssetArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, PickAssetEvent.NavigateBack)
    }

    fun onItemClicked(item: TokenListItem) = viewModelScope.launch {
        val args = args.first()
        val event = PickAssetEvent.ReturnResult(item.model, args.type)
        _events.emit(event)
    }

    fun onSearchTextChanged(text: CharSequence?) = viewModelScope.launch {
        text ?: return@launch
        listHelper.setSearchText(text.toString())
    }
}