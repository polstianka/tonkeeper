package com.tonapps.tonkeeper.fragment.swap.pick_asset

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.core.observeFlow
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.pick_asset.rv.TokenListHelper
import com.tonapps.tonkeeper.fragment.swap.pick_asset.rv.TokenListItem
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PickAssetViewModel(
    private val dexAssetsRepository: DexAssetsRepository,
    private val listHelper: TokenListHelper
) : ViewModel() {

    private val args = MutableSharedFlow<PickAssetArgs>(replay = 1)
    private val _events = MutableSharedFlow<PickAssetEvent>()

    val events: Flow<PickAssetEvent>
        get() = _events
    val items = listHelper.items

    init {
        observeFlow(dexAssetsRepository.nonCommunityItems) { listHelper.submitItems(it) }
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

    fun onSearchTextChanged(text: CharSequence?) {
        text ?: return
        listHelper.setSearchText(text.toString())
    }
}