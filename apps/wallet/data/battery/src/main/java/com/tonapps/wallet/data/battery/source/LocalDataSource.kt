package com.tonapps.wallet.data.battery.source

import android.content.Context
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.wallet.data.battery.entity.BatteryEntity
import com.tonapps.wallet.data.core.BlobDataSource

internal class LocalDataSource(context: Context): BlobDataSource<BatteryEntity>(
    context = context,
    path = "battery",
) {

    override fun onMarshall(data: BatteryEntity) = data.toByteArray()

    override fun onUnmarshall(bytes: ByteArray) = bytes.toParcel<BatteryEntity>()
}

