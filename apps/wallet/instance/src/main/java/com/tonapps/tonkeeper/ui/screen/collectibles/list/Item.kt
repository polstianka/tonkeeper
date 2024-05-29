package com.tonapps.tonkeeper.ui.screen.collectibles.list

import android.net.Uri
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.wallet.data.collectibles.entities.NftEntity

sealed class Item(type: Int): BaseListItem(type) {

    companion object {
        const val TYPE_NFT = 0
        const val TYPE_SKELETON = 1
    }

    data class Nft(
        val entity: NftEntity,
        val hiddenBalance: Boolean
    ): Item(TYPE_NFT) {

        val nftAddress: String
            get() = entity.address

        val imageURI: Uri
            get() = if (hiddenBalance) entity.thumbUri else entity.mediumUri

        val title: String
            get() = entity.name

        val collectionName: String
            get() = entity.collectionName

        val testnet: Boolean
            get() = entity.testnet

        val verifier: Boolean
            get() = entity.verified

        val sale: Boolean
            get() = entity.inSale

    }

    data class Skeleton(val value: Boolean = true): Item(TYPE_SKELETON)

}