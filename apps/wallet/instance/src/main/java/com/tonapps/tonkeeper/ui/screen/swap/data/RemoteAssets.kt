package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import com.tonapps.wallet.api.entity.TokenEntity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemoteAssets(
    val list: List<AssetEntity> = emptyList()
): Parcelable {
    fun isEmpty(): Boolean = list.isEmpty()

    @IgnoredOnParcel
    val displayList: List<AssetEntity> by lazy {
        list.filter {
            !it.hidden
        }
    }

    @IgnoredOnParcel
    val suggestedList: List<AssetEntity> by lazy {
        list.filter {
            it.isSuggested
        }
    }

    @IgnoredOnParcel
    val map: Map<String, AssetEntity> by lazy {
        val map = mutableMapOf<String, AssetEntity>()
        for (asset in list) {
            map[asset.token.address] = asset
        }
        map
    }

    fun findAsset(token: TokenEntity): AssetEntity? {
        return map[token.address]
    }
}