package com.tonapps.wallet.data.token.source

import android.content.Context
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toListParcel
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.data.core.BlobDataSource
import com.tonapps.wallet.data.token.entities.AssetEntity

@Deprecated("")
internal class SwapLocalDataSource(context: Context): BlobDataSource<List<AssetEntity>>(
    context = context,
    path = "swap",
    lruInitialCapacity = 12 // todo change if necessary
) {
    override fun onMarshall(data: List<AssetEntity>) = data.toByteArray()

    override fun onUnmarshall(bytes: ByteArray) = bytes.toListParcel<AssetEntity>()
}