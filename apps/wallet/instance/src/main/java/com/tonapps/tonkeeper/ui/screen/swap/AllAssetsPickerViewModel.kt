package com.tonapps.tonkeeper.ui.screen.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AllAssetsPickerViewModel(
    private val walletRepository: WalletRepository,
    private val api: API,
    private val swapRepository: SwapRepository
) : ViewModel() {

    private val localAssets = mutableListOf<AssetModel>()

    private val _assets = MutableStateFlow(emptyList<AssetModel>())
    val assets: StateFlow<List<AssetModel>> = _assets

    private val _other = MutableStateFlow(emptyList<AssetModel>())
    val other: StateFlow<List<AssetModel>> = _other

    private var isSend = true

    fun init(isSend: Boolean) {
        this.isSend = isSend
        viewModelScope.launch(Dispatchers.IO) {
            walletRepository.activeWalletFlow.collect {
                val allTokens = api.getAllTokens(it.address, it.testnet)
                _assets.value = allTokens.mapIndexed { index, asset ->
                    AssetModel(
                        token = asset.token,
                        balance = asset.value,
                        walletAddress = asset.walletAddress,
                        position = ListCell.getPosition(allTokens.size, index)
                    )
                }
                localAssets.clear()
                localAssets.addAll(_assets.value)
                _other.value = localAssets.take(2)
            }
        }
    }

    fun search(s: String) {
        viewModelScope.launch {
            _assets.value = localAssets.filter {
                it.token.name.contains(s, true) || it.token.symbol.contains(s, true)
            }
        }
    }

    fun setAsset(model: AssetModel) {
        if (isSend) {
            swapRepository.setSendToken(model)
        } else {
            swapRepository.setReceiveToken(model)
        }
    }
}