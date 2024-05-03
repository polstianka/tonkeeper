package com.tonapps.tonkeeper.ui.screen.swap

import android.content.Context
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toListParcel
import com.tonapps.wallet.api.entity.AssetEntity
import com.tonapps.wallet.data.core.BlobDataSource

internal class WalletAssetsLocalDataSource(context: Context) : BlobDataSource<List<AssetEntity>>(
    context = context,
    path = "wallet_assets",
    lruInitialCapacity = 100
) {
    override fun onMarshall(data: List<AssetEntity>) = data.toByteArray()

    override fun onUnmarshall(bytes: ByteArray) = bytes.toListParcel<AssetEntity>()
}