package com.tonapps.tonkeeper.ui.screen.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.swap.AssetModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WalletAssetsPickerViewModel(
    private val assetsRepository: com.tonapps.wallet.data.swap.WalletAssetsRepository,
    private val swapRepository: com.tonapps.wallet.data.swap.SwapRepository
) : ViewModel() {

    private val localAssets = mutableListOf<AssetModel>()

    private val _assets = MutableStateFlow(emptyList<AssetModel>())
    val assets: StateFlow<List<AssetModel>> = _assets

    private val _other = MutableStateFlow(emptyList<AssetModel>())
    val other: StateFlow<List<AssetModel>> = _other

    private var isSend = true

    fun init(isSend: Boolean, oppSymbol: String) {
        this.isSend = isSend
        viewModelScope.launch(Dispatchers.IO) {
            val allTokens = assetsRepository.get().filter { it.token.symbol != oppSymbol }
            _assets.value = allTokens.mapIndexed { index, asset ->
                AssetModel(
                    token = asset.token,
                    balance = asset.value,
                    walletAddress = asset.walletAddress,
                    position = ListCell.getPosition(allTokens.size, index),
                    fiatBalance = asset.value * asset.usdPrice,
                    isTon = asset.kind == "TON"
                )
            }
            localAssets.clear()
            localAssets.addAll(_assets.value)
            _other.value = localAssets.take(2)
        }
    }

    fun search(s: String) {
        viewModelScope.launch {
            val assetModels = localAssets.filter {
                it.token.name.contains(s, true) || it.token.symbol.contains(s, true)
            }
            _assets.value = assetModels.mapIndexed { index, it ->
                it.copy(position = ListCell.getPosition(assetModels.size, index))
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