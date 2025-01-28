package com.tonapps.wallet.data.collectibles.entities

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import com.tonapps.blockchain.ton.extensions.equalsAddress
import com.tonapps.blockchain.ton.extensions.toUserFriendly
import com.tonapps.wallet.api.entity.AccountEntity
import com.tonapps.wallet.data.core.Trust
import io.tonapi.models.NftItem
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class NftEntity(
    val address: String,
    val owner: AccountEntity?,
    val collection: NftCollectionEntity?,
    val metadata: NftMetadataEntity,
    val previews: List<NftPreviewEntity>,
    val testnet: Boolean,
    val verified: Boolean,
    val inSale: Boolean,
    val dns: String?,
    val trust: Trust,
): Parcelable {

    val collectionAddressOrNFTAddress: String
        get() = collection?.address ?: address

    val id: String
        get() = if (testnet) "testnet:$address" else address

    val userFriendlyAddress: String
        get() = address.toUserFriendly(wallet = false, testnet = testnet)

    val name: String
        get() = metadata.name ?: ""

    val description: String
        get() = metadata.description ?: ""

    val collectionName: String
        get() {
            val name = collection?.name
            if (name.isNullOrBlank() && isDomain) {
                return "TON DNS"
            }
            return name ?: ""
        }

    val collectionDescription: String
        get() = collection?.description ?: ""

    val isDomain: Boolean
        get() = dns != null

    val ownerAddress: String
        get() = owner?.address ?: address

    @IgnoredOnParcel
    val thumbUri: Uri by lazy {
        getImageUri(64, 320) ?: previews.first().url.let { Uri.parse(it) }
    }

    @IgnoredOnParcel
    val mediumUri: Uri by lazy {
        getImageUri(256, 512) ?: previews.first().url.let { Uri.parse(it) }
    }

    @IgnoredOnParcel
    val bigUri: Uri by lazy {
        getImageUri(512, 1024) ?: previews.last().url.let { Uri.parse(it) }
    }

    @IgnoredOnParcel
    val lottieUri: Uri? by lazy {
        metadata.lottie?.toUri()
    }

    val isTrusted: Boolean
        get() = trust == Trust.whitelist

    val suspicious: Boolean
        get() =  trust == Trust.none || trust == Trust.blacklist

    val isTelegramUsername: Boolean
        get() {
            val address = collection?.address ?: return false
            return address.equalsAddress("0:80d78a35f955a14b679faa887ff4cd5bfc0f43b4a4eea2a7e6927f3701b273c2")
        }

    private fun getImage(minSize: Int, maxSize: Int): NftPreviewEntity? {
        return previews.find {
            if (minSize > it.width || minSize > it.height) {
                return@find false
            }
            if (maxSize < it.width || maxSize < it.height) {
                return@find false
            }
            true
        }
    }

    private fun getImageUri(minSize: Int, maxSize: Int): Uri? {
        return getImage(minSize, maxSize)?.url?.let { Uri.parse(it) }
    }

    constructor(item: NftItem, testnet: Boolean) : this(
        address = item.address,
        owner = item.owner?.let { AccountEntity(it, testnet) },
        collection = item.collection?.let { NftCollectionEntity(it) },
        metadata = NftMetadataEntity(item.metadata),
        previews = item.previews?.map { NftPreviewEntity(it) } ?: emptyList(),
        testnet = testnet,
        verified = item.approvedBy.isNotEmpty(),
        inSale = item.sale != null,
        dns = item.dns,
        trust = Trust(item.trust),
    )
}