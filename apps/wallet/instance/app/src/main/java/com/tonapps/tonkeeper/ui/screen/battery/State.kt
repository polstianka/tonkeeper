package com.tonapps.tonkeeper.ui.screen.battery

import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.ui.screen.battery.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.battery.BatteryMapper
import com.tonapps.wallet.data.battery.entity.BatteryConfigEntity
import com.tonapps.wallet.data.battery.entity.RechargeMethodType
import com.tonapps.wallet.data.settings.BatteryTransaction
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
        val supportedTransactions: Array<BatteryTransaction>,
    ) : State() {

        fun uiItem(): Item.Settings {
            return Item.Settings(supportedTransactions.toList())
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

        fun uiItems(
            hasBalance: Boolean,
            config: ConfigEntity,
            wallet: WalletEntity,
        ): List<Item> {
            val items = mutableListOf<Item>()

            if (hasBalance) {
                items.add(Item.SettingsHeader())
            }

            for ((index, type) in BatteryTransaction.entries.withIndex()) {
                val position = ListCell.getPosition(BatteryTransaction.entries.size, index)
                val meanPrice = getTransactionMeanPrice(config, type)
                val charges = BatteryMapper.calculateChargesAmount(meanPrice, config.batteryMeanFees)
                val enabled = supportedTransactions.contains(type)
                val item = Item.SupportedTransaction(
                    wallet = wallet,
                    position = position,
                    supportedTransaction = type,
                    enabled = enabled,
                    showToggle = hasBalance,
                    changes = charges,
                )

                items.add(item)
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