package com.tonapps.wallet.data.battery.source

import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.withRetry
import com.tonapps.wallet.data.battery.entity.BatteryConfigEntity
import com.tonapps.wallet.data.battery.entity.RechargeMethodEntity
import com.tonapps.wallet.data.battery.entity.RechargeMethodType
import io.batteryapi.models.RechargeMethodsMethodsInner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RemoteConfigDataSource(
    private val api: API
) {
    suspend fun load(testnet: Boolean): BatteryConfigEntity? = withContext(Dispatchers.IO) {
        val config = withRetry { api.battery(testnet).getConfig() }
            ?: return@withContext null
        val rechargeMethods = withRetry { api.battery(testnet).getRechargeMethods(false) }
            ?: return@withContext null

        BatteryConfigEntity(
            excessesAccount = config.excessAccount,
            fundReceiver = config.fundReceiver,
            rechargeMethods = rechargeMethods.methods.map(::mapToRechargeMethodEntity)
        )
    }

    private fun mapToRechargeMethodEntity(method: RechargeMethodsMethodsInner): RechargeMethodEntity {
        return RechargeMethodEntity(
            type = method.type.toRechargeMethodType(),
            rate = method.rate,
            symbol = method.symbol,
            decimals = method.decimals,
            supportGasless = method.supportGasless,
            supportRecharge = method.supportRecharge,
            image = method.image,
            jettonMaster = method.jettonMaster,
            minBootstrapValue = method.minBootstrapValue
        )
    }

    private fun RechargeMethodsMethodsInner.Type.toRechargeMethodType(): RechargeMethodType {
        return when (this) {
            RechargeMethodsMethodsInner.Type.jetton -> RechargeMethodType.JETTON
            RechargeMethodsMethodsInner.Type.ton -> RechargeMethodType.TON
        }
    }
}