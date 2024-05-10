package com.tonapps.wallet.data.stake

import android.content.Context
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.wallet.api.entity.StakePoolsEntity
import com.tonapps.wallet.data.core.BlobDataSource

internal class StakeLocalDataSource(context: Context) : BlobDataSource<StakePoolsEntity>(
    context = context,
    path = "staking_pools",
    lruInitialCapacity = 100
) {
    override fun onMarshall(data: StakePoolsEntity) = data.toByteArray()

    override fun onUnmarshall(bytes: ByteArray) = bytes.toParcel<StakePoolsEntity>()
}