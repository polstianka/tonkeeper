package com.tonapps.tonkeeper.ui.screen.battery.settings

import android.app.Application
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.tonkeeper.ui.screen.battery.settings.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.battery.BatteryMapper
import com.tonapps.wallet.data.battery.BatteryRepository
import com.tonapps.wallet.data.battery.entity.BatteryBalanceEntity
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class BatterySettingsViewModel(
    app: Application,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val batteryRepository: BatteryRepository,
    private val api: API,
): BaseWalletVM(app) {

    val uiItemsFlow = combine(
        accountRepository.selectedWalletFlow,
        api.configFlow,
    ) { wallet, config ->
        val batteryBalance = getBatteryBalance(wallet)
        val hasBalance = batteryBalance.balance.isPositive

        val uiItems = mutableListOf<Item>()
        if (hasBalance) {
            uiItems.add(Item.SettingsHeader)
        }

        val types = BatteryTransaction.entries
        for ((index, type) in types.withIndex()) {
            val position = ListCell.getPosition(types.size, index)
            val meanPrice = getTransactionMeanPrice(config, type)
            val charges = BatteryMapper.calculateChargesAmount(meanPrice, config.batteryMeanFees)
            val enabled = settingsRepository.batteryIsEnabledTx(wallet.accountId, type)
            val item = Item.SupportedTransaction(
                wallet = wallet,
                position = position,
                supportedTransaction = type,
                enabled = enabled,
                showToggle = hasBalance,
                changes = charges,
            )
            uiItems.add(item)
        }

        uiItems.toList()
    }.flowOn(Dispatchers.IO)

    private suspend fun getBatteryBalance(
        wallet: WalletEntity
    ): BatteryBalanceEntity {
        val tonProofToken = accountRepository.requestTonProofToken(wallet) ?: return BatteryBalanceEntity.Empty
        return batteryRepository.getBalance(
            tonProofToken = tonProofToken,
            publicKey = wallet.publicKey,
            testnet = wallet.testnet
        )
    }

    private fun getTransactionMeanPrice(
        config: ConfigEntity,
        transaction: BatteryTransaction
    ): String {
        return when (transaction) {
            BatteryTransaction.NFT -> config.batteryMeanPriceNft
            BatteryTransaction.SWAP -> config.batteryMeanPriceSwap
            BatteryTransaction.JETTON -> config.batteryMeanPriceJetton
            else -> "0"
        }
    }
}