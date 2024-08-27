package com.tonapps.wallet.data.battery.source

import com.tonapps.icu.Coins
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.battery.entity.BatteryBalanceEntity
import com.tonapps.wallet.data.battery.entity.BatteryConfigEntity
import com.tonapps.wallet.data.battery.entity.RechargeMethodEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

internal class RemoteDataSource(
    private val api: API
) {

    suspend fun fetchBalance(
        tonProofToken: String,
        testnet: Boolean
    ): BatteryBalanceEntity? = withContext(Dispatchers.IO) {
        val response = api.getBatteryBalance(tonProofToken, testnet) ?: return@withContext null

        BatteryBalanceEntity(
            balance = Coins.of(response.balance.toBigDecimal(), 20),
            reservedBalance = Coins.of(response.reserved.toBigDecimal(), 20)
        )
    }

    suspend fun fetchConfig(
        testnet: Boolean
    ): BatteryConfigEntity? = withContext(Dispatchers.IO) {
        val configDeferred = async { api.getBatteryConfig(testnet) }
        val rechargeMethodsDeferred = async { api.getBatteryRechargeMethods(testnet) }

        val config = configDeferred.await() ?: return@withContext null
        val rechargeMethods = rechargeMethodsDeferred.await() ?: return@withContext null

        BatteryConfigEntity(
            excessesAccount = config.excessAccount,
            fundReceiver = config.fundReceiver,
            rechargeMethods = rechargeMethods.methods.map(::RechargeMethodEntity)
        )
    }

}