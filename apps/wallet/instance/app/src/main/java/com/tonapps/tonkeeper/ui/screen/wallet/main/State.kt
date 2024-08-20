package com.tonapps.tonkeeper.ui.screen.wallet.main

import android.util.Log
import com.tonapps.icu.Coins
import com.tonapps.icu.Coins.Companion.DEFAULT_DECIMALS
import com.tonapps.icu.Coins.Companion.sumOf
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.core.entities.AssetsEntity
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item.BalanceType
import com.tonapps.tonkeeper.view.BatteryView
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.api.entity.NotificationEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.core.isAvailableBiometric
import com.tonapps.wallet.data.push.entities.AppPushEntity
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.tonconnect.entities.DAppManifestEntity
import com.tonapps.wallet.data.tonconnect.entities.DConnectEntity
import com.tonapps.wallet.localization.Localization

sealed class State {

    private enum class SetupType {
        Push,
        Biometry,
        Telegram,
        Backup,
    }

    data class Battery(
        val balance: Coins,
        val beta: Boolean,
        val disabled: Boolean,
        val viewed: Boolean,
    ): State()

    data class Setup(
        val pushEnabled: Boolean,
        val biometryEnabled: Boolean,
        val hasBackup: Boolean,
        val showTelegramChannel: Boolean,
    ): State()

    data class Assets(
        val currency: WalletCurrency,
        val list: List<AssetsEntity>,
        val fromCache: Boolean,
        val rates: RatesEntity,
    ): State() {

        val size: Int
            get() = list.size

        private fun getTotalBalanceFiat(wallet: WalletEntity): Coins {
            return if (wallet.testnet) {
                list.first().fiat
            } else {
                list.map { it.fiat }.sumOf { it }
            }
        }

        fun getBalanceType(wallet: WalletEntity): BalanceType {
            val balanceFiat = getTotalBalanceFiat(wallet)
            val balanceTON = rates.convertFromFiat(TokenEntity.TON.address, balanceFiat)
            return when {
                balanceTON >= Coins.of(20.0, DEFAULT_DECIMALS) -> BalanceType.Huge
                balanceTON >= Coins.of(2.0, DEFAULT_DECIMALS) -> BalanceType.Positive
                else -> BalanceType.Zero
            }
        }

        fun getTotalBalanceFormat(
            wallet: WalletEntity,
        ): CharSequence {
            val total = getTotalBalanceFiat(wallet)
            return CurrencyFormatter.formatFiat(currency.code, total)
        }
    }

    data class Main(
        val wallet: WalletEntity,
        val assets: Assets,
        val hasBackup: Boolean,
        val battery: Battery,
    ): State() {

        private val totalBalanceFormat: CharSequence
            get() = assets.getTotalBalanceFormat(wallet)

        private val balanceType: BalanceType
            get() = assets.getBalanceType(wallet)

        private fun uiItemsTokens(hiddenBalance: Boolean): List<Item> {
            val currencyCode = assets.currency.code
            val uiItems = mutableListOf<Item>()
            uiItems.add(Item.Space(true))

            for ((index, asset) in assets.list.withIndex()) {
                val position = ListCell.getPosition(assets.list.size, index)
                if (asset is AssetsEntity.Staked) {
                    val staked = asset.staked
                    val item = Item.Stake(
                        position = position,
                        poolAddress = staked.pool.address,
                        poolName = staked.pool.name,
                        poolImplementation = staked.pool.implementation,
                        balance = staked.amount,
                        balanceFormat = CurrencyFormatter.format(value = staked.amount),
                        message = null,
                        fiat = staked.fiatBalance,
                        fiatFormat = CurrencyFormatter.formatFiat(currencyCode, staked.fiatBalance),
                    )
                    uiItems.add(item)
                } else if (asset is AssetsEntity.Token) {
                    val item = Item.Token(
                        position = position,
                        token = asset.token,
                        hiddenBalance = hiddenBalance,
                        testnet = wallet.testnet,
                        currencyCode = currencyCode
                    )
                    uiItems.add(item)
                }
            }
            uiItems.add(Item.Space(true))
            uiItems.add(Item.Manage(true))
            return uiItems.toList()
        }

        private fun uiItemBalance(
            hiddenBalance: Boolean,
            status: Item.Status,
            lastUpdatedFormat: String,
        ): Item.Balance {
            return Item.Balance(
                balance = totalBalanceFormat,
                address = wallet.address,
                walletType = wallet.type,
                walletVersion = wallet.version,
                status = status,
                hiddenBalance = hiddenBalance,
                hasBackup = hasBackup,
                balanceType = balanceType,
                lastUpdatedFormat = lastUpdatedFormat,
                batteryBalance = battery.balance,
                showBattery = !battery.disabled && (!battery.beta || !battery.balance.isZero),
                batteryEmptyState = if (battery.viewed) BatteryView.EmptyState.SECONDARY else BatteryView.EmptyState.ACCENT,
            )
        }

        private fun uiItemActions(
            config: ConfigEntity
        ): Item.Actions {
            return Item.Actions(
                address = wallet.address,
                token = TokenEntity.TON,
                walletType = wallet.type,
                swapUri = config.swapUri,
                disableSwap = config.flags.disableSwap
            )
        }

        private fun uiItemsSetup(
            walletId: String,
            config: ConfigEntity,
            setupTypes: List<SetupType>
        ): List<Item> {
            if (1 >= setupTypes.size && !setupTypes.contains(SetupType.Backup)) {
                return emptyList()
            }
            val uiItems = mutableListOf<Item>()
            uiItems.add(Item.SetupTitle(
                walletId = walletId,
                showDone = !setupTypes.contains(SetupType.Backup)
            ))
            for ((index, setupType) in setupTypes.withIndex()) {
                val position = ListCell.getPosition(setupTypes.size, index)
                val item = when (setupType) {
                    SetupType.Backup -> Item.SetupLink(
                        position = position,
                        iconRes = UIKitIcon.ic_key_28,
                        textRes = Localization.setup_finish_backup,
                        link = "tonkeeper://backups",
                        blue = false,
                        walletId = wallet.id,
                        settingsType = Item.SetupLink.TYPE_NONE
                    )
                    SetupType.Telegram -> Item.SetupLink(
                        position = position,
                        iconRes = UIKitIcon.ic_telegram_28,
                        textRes = Localization.setup_finish_telegram,
                        link = config.tonkeeperNewsUrl,
                        blue = true,
                        walletId = wallet.id,
                        settingsType = Item.SetupLink.TYPE_TELEGRAM_CHANNEL
                    )
                    SetupType.Biometry -> Item.SetupSwitch(
                        position = position,
                        iconRes = UIKitIcon.ic_faceid_28,
                        textRes = Localization.setup_finish_biometry,
                        enabled = false,
                        walletId = wallet.id,
                        settingsType = Item.SetupSwitch.TYPE_BIOMETRIC
                    )
                    SetupType.Push -> Item.SetupSwitch(
                        position = ListCell.Position.FIRST,
                        iconRes = UIKitIcon.ic_bell_28,
                        textRes = Localization.setup_finish_push,
                        enabled = false,
                        walletId = wallet.id,
                        settingsType = Item.SetupSwitch.TYPE_PUSH
                    )
                }
                uiItems.add(item)
            }
            return uiItems.toList()
        }

        private fun createSetupTypes(
            setup: Setup,
        ): List<SetupType> {
            val setupTypes = mutableListOf<SetupType>()
            if (!setup.pushEnabled) {
                setupTypes.add(SetupType.Push)
            }
            if (!setup.biometryEnabled && isAvailableBiometric(App.instance)) {
                setupTypes.add(SetupType.Biometry)
            }
            if (setup.showTelegramChannel) {
                setupTypes.add(SetupType.Telegram)
            }
            if (!hasBackup) {
                setupTypes.add(SetupType.Backup)
            }
            return setupTypes.toList()
        }

        fun uiItems(
            wallet: WalletEntity,
            hiddenBalance: Boolean,
            status: Item.Status,
            config: ConfigEntity,
            alerts: List<NotificationEntity>,
            dAppNotifications: DAppNotifications,
            setup: Setup?,
            lastUpdatedFormat: String,
        ): List<Item> {
            val uiItems = mutableListOf<Item>()
            if (alerts.isNotEmpty()) {
                for (alert in alerts) {
                    uiItems.add(Item.Alert(alert))
                    uiItems.add(Item.Space(true))
                }
            }
            uiItems.add(uiItemBalance(hiddenBalance, status, lastUpdatedFormat))
            uiItems.add(uiItemActions(config))
            if (!dAppNotifications.isEmpty) {
                uiItems.add(Item.Push(dAppNotifications.notifications, dAppNotifications.apps, dAppNotifications.manifestEntity))
            }

            setup?.let {
                val setupTypes = createSetupTypes(it)
                if (setupTypes.isNotEmpty()) {
                    uiItems.addAll(uiItemsSetup(wallet.id, config, setupTypes))
                }
            }

            uiItems.addAll(uiItemsTokens(hiddenBalance))
            return uiItems.toList()
        }
    }

    data class DAppNotifications(
        val notifications: List<AppPushEntity> = emptyList(),
        val apps: List<DConnectEntity> = emptyList(),
        val manifestEntity: List<DAppManifestEntity> = emptyList(),
    ): State() {

        val isEmpty: Boolean
            get() = notifications.isEmpty() || apps.isEmpty()
    }

    data class Settings(
        val hiddenBalance: Boolean,
        val config: ConfigEntity,
        val status: Item.Status
    ): State()
}