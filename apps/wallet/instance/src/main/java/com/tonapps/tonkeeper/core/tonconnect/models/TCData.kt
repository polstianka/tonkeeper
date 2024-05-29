package com.tonapps.tonkeeper.core.tonconnect.models

import android.net.Uri
import android.os.Parcelable
import com.tonapps.blockchain.ton.extensions.toUserFriendly
import com.tonapps.tonkeeper.api.shortAddress
import com.tonapps.wallet.data.tonconnect.entities.DAppItemEntity
import com.tonapps.wallet.data.tonconnect.entities.DAppManifestEntity
import kotlinx.parcelize.Parcelize

data class TCData(
    val manifest: DAppManifestEntity,
    val accountId: String,
    val testnet: Boolean,
    val clientId: String,
    val items: List<DAppItemEntity>
) {

    val shortAddress: String
        get() = accountId.toUserFriendly(testnet = testnet).shortAddress

    val url: String
        get() = manifest.url

    val host: String
        get() = Uri.parse(url).host ?: ""
}