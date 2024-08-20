package com.tonapps.tonkeeper.ui.screen.battery

import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.ui.screen.battery.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.battery.BatteryRepository
import com.tonapps.wallet.data.battery.entity.BatteryConfigEntity
import com.tonapps.wallet.data.battery.entity.BatterySupportedTransaction
import com.tonapps.wallet.data.battery.entity.RechargeMethodType
import com.tonapps.wallet.data.token.entities.AccountTokenEntity

sealed class State {

    data class Config(
        val wallet: WalletEntity,
        val batteryConfig: BatteryConfigEntity,
    ) : State()

    data class Battery(
        val balance: Float,
        val charges: Int,
        val isBeta: Boolean
    ) : State() {
        fun uiItem(): Item.Battery {
            val formattedChanges = CurrencyFormatter.format(value = charges.toBigDecimal())
            return Item.Battery(balance, isBeta, charges, formattedChanges)
        }
    }

    data class Settings(
        val supportedTransactions: Map<BatterySupportedTransaction, Boolean>,
    ) : State() {
        fun uiItem(): Item.Settings {
            val list = supportedTransactions.filter { it.value }.keys.toList()
            return Item.Settings(list)
        }

        private fun getTransactionMeanPrice(
            config: ConfigEntity,
            transaction: BatterySupportedTransaction
        ): String {
            return when (transaction) {
                BatterySupportedTransaction.NFT -> config.batteryMeanPriceNft
                BatterySupportedTransaction.SWAP -> config.batteryMeanPriceSwap
                BatterySupportedTransaction.JETTON -> config.batteryMeanPriceJetton
                else -> "0"
            }
        }

        fun uiItems(hasBalance: Boolean, config: ConfigEntity): List<Item> {
            val items = mutableListOf<Item>()

            if (hasBalance) {
                items.add(Item.SettingsHeader())
            }

            supportedTransactions.onEachIndexed { index, entry ->
                val meanPrice = getTransactionMeanPrice(config, entry.key)
                val charges =
                    BatteryRepository.calculateChargesAmount(meanPrice, config.batteryMeanFees)

                items.add(
                    Item.SupportedTransaction(
                        position = ListCell.getPosition(supportedTransactions.keys.size, index),
                        supportedTransaction = entry.key,
                        enabled = entry.value,
                        showToggle = hasBalance,
                        changes = charges,
                    )
                )
            }

            return items.toList()
        }
    }

    data class RechargeMethods(
        val batteryConfig: BatteryConfigEntity,
        val tokens: List<AccountTokenEntity>,
        val disabled: Boolean,
    ) : State() {
        fun uiItems(): List<Item> {
            val items = mutableListOf<Item>()

            if (disabled) {
                return items.toList()
            }

            val filteredTokens = tokens.filter { token ->
                batteryConfig.rechargeMethods.any { method ->
                    val hasMethod =
                        if (method.type == RechargeMethodType.TON) token.isTon else token.address == method.jettonMaster
                    hasMethod && method.supportRecharge && token.balance.value.isPositive
                }
            }.sortedBy { it.isTon }

            if (filteredTokens.isNotEmpty()) {
                items.addAll(filteredTokens.mapIndexed { index, item ->
                    Item.RechargeMethod(
                        position = ListCell.getPosition(filteredTokens.size + 1, index),
                        currency = item.symbol,
                        image = item.imageUri,
                    )
                })
                items.add(Item.Gift(position = ListCell.Position.LAST))
            }

            return items.toList()
        }
    }

    data class Main(
        val battery: Battery,
        val settings: Settings,
        val rechargeMethods: RechargeMethods,
    ) : State() {
        fun uiItems(): List<Item> {
            val items = mutableListOf<Item>()
            items.add(battery.uiItem())
            items.add(settings.uiItem())
            items.add(Item.Space())
            items.addAll(rechargeMethods.uiItems())
            items.add(Item.Space())
            return items.toList()
        }
    }
}