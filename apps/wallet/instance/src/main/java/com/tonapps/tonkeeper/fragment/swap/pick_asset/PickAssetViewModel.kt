package com.tonapps.tonkeeper.fragment.swap.pick_asset

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.pick_asset.rv.TokenListItem
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class PickAssetViewModel(
    private val dexAssetsRepository: DexAssetsRepository
) : ViewModel() {

    private val args = MutableSharedFlow<PickAssetArgs>(replay = 1)
    private val _events = MutableSharedFlow<PickAssetEvent>()

    val events: Flow<PickAssetEvent>
        get() = _events
    val items = dexAssetsRepository.nonCommunityItems
        .map { list ->
            list.mapIndexed { index, item ->
                TokenListItem(
                    model = item,
                    iconUrl = item.imageUrl,
                    symbol = item.symbol,
                    amountCrypto = "",
                    name = item.displayName,
                    amountFiat = "",
                    amountCryptoColor = com.tonapps.uikit.color.R.attr.textPrimaryColor,
                    position = ListCell.getPosition(list.size, index)
                )
            }
        }
    fun provideArgs(args: PickAssetArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, PickAssetEvent.NavigateBack)
    }

    fun onItemClicked(it: TokenListItem) {
        Log.wtf("###", "onItemClicked: $it")
    }
}