package com.tonapps.wallet.data.battery.source

import com.tonapps.wallet.api.withRetry
import com.tonapps.icu.Coins
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.battery.entity.BatteryEntity
import io.batteryapi.apis.BatteryApi.UnitsGetBalance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RemoteDataSource(
    private val api: API
) {
    suspend fun load(
        tonProofToken: String,
        testnet: Boolean
    ): BatteryEntity? = withContext(Dispatchers.IO) {
        val response = withRetry {
            api.battery(testnet).getBalance(tonProofToken, UnitsGetBalance.ton)
        } ?: return@withContext null

        BatteryEntity(
            balance = Coins.of(response.balance.toBigDecimal(), 20),
            reservedBalance = Coins.of(response.reserved.toBigDecimal(), 20)
        )
    }

}