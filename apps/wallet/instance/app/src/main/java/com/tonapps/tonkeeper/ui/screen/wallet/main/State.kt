package com.tonapps.tonkeeper.ui.screen.wallet.main

import com.tonapps.icu.Coins
import com.tonapps.icu.Coins.Companion.sumOf
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.core.BalanceType
import com.tonapps.tonkeeper.core.entities.AssetsEntity
import com.tonapps.tonkeeper.manager.apk.APKManager
import com.tonapps.tonkeeper.ui.screen.card.entity.CardScreenPath
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item
import com.tonapps.tonkeeper.view.BatteryView
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.api.entity.NotificationEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.core.isAvailableBiometric
import com.tonapps.wallet.data.dapps.entities.AppPushEntity
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.localization.Localization

sealed class State {

    private enum class SetupType {
        Push,
        Biometry,
        Telegram,
        Backup,
        SafeMode,
    }

    data class Battery(
        val balance: Coins,
        val beta: Boolean,
        val disabled: Boolean,
        val viewed: Boolean,
    ) : State()

    data class Cards(
        val list: List<CardListItem>,
        val totalFiat: Coins,
        val dismissed: Boolean,
    ) : State()

    data class CardListItem(
        val lastFourDigits: String,
        val title: CharSequence,
        val subtitle: CharSequence,
        val path: CardScreenPath,
        val kindResId: Int,
    ) : State()

    data class Setup(
        val pushEnabled: Boolean,
        val biometryEnabled: Boolean,
        val hasBackup: Boolean,
        val showTelegramChannel: Boolean,
        val safeModeBlock: Boolean
    ) : State()

    data class Assets(
        val currency: WalletCurrency,
        val list: List<AssetsEntity>,
        val fromCache: Boolean,
        val rates: RatesEntity,
    ) : State() {

        val size: Int
            get() = list.size

        fun getTotalBalanceFiat(wallet: WalletEntity): Coins {
            return if (wallet.testnet) {
                list.first().fiat
            } else {
                list.map { it.fiat }.sumOf { it }
            }
        }

        fun getBalanceType(wallet: WalletEntity): Int {
            val balanceFiat = getTotalBalanceFiat(wallet)
            val balanceTON = rates.convertFromFiat(TokenEntity.TON.address, balanceFiat)
            return BalanceType.getBalanceType(balanceTON)
        }
    }

    data class Main(
        val wallet: WalletEntity,
        val assets: Assets,
        val hasBackup: Boolean,
        val battery: Battery,
        val cards: Cards?,
        val lt: Long?,
        val isOnline: Boolean,
        val apkStatus: APKManager.Status,
    ) : State() {

        val totalBalanceFiat: Coins
            get() = assets.getTotalBalanceFiat(wallet) + (cards?.totalFiat ?: Coins.ZERO)

        private val totalBalanceFormat: CharSequence
            get() = CurrencyFormatter.formatFiat(assets.currency.code, totalBalanceFiat)

        private val balanceType: Int
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
                        balance = staked.balance,
                        balanceFormat = CurrencyFormatter.format(value = staked.balance),
                        message = null,
                        fiat = staked.fiatBalance,
                        fiatFormat = CurrencyFormatter.formatFiat(currencyCode, staked.fiatBalance),
                        hiddenBalance = hiddenBalance,
                        wallet = wallet,
                        readyWithdraw = staked.readyWithdraw,
                        readyWithdrawFormat = CurrencyFormatter.formatFiat(
                            "TON",
                            staked.readyWithdraw
                        ),
                        pendingDeposit = staked.pendingDeposit,
                        pendingDepositFormat = CurrencyFormatter.formatFiat(
                            "TON",
                            staked.pendingDeposit
                        ),
                        pendingWithdraw = staked.pendingWithdraw,
                        pendingWithdrawFormat = CurrencyFormatter.formatFiat(
                            "TON",
                            staked.pendingWithdraw
                        ),
                        cycleEnd = staked.cycleEnd,
                    )
                    uiItems.add(item)
                } else if (asset is AssetsEntity.Token) {
                    val item = Item.Token(
                        position = position,
                        token = asset.token,
                        hiddenBalance = hiddenBalance,
                        testnet = wallet.testnet,
                        currencyCode = currencyCode,
                        wallet = wallet,
                    )
                    uiItems.add(item)
                }
            }
            uiItems.add(Item.Space(true))
            uiItems.add(Item.Manage(wallet))
            return uiItems.toList()
        }

        private fun uiItemBalance(
            hiddenBalance: Boolean,
            status: Item.Status,
            lastUpdatedFormat: String,
            prefixYourAddress: Boolean,
        ): Item.Balance {
            return Item.Balance(
                balance = totalBalanceFormat,
                wallet = wallet,
                status = status,
                hiddenBalance = hiddenBalance,
                hasBackup = hasBackup,
                balanceType = balanceType,
                lastUpdatedFormat = lastUpdatedFormat,
                batteryBalance = battery.balance,
                showBattery = !battery.disabled && (!battery.beta || !battery.balance.isZero),
                batteryEmptyState = if (battery.viewed) BatteryView.EmptyState.SECONDARY else BatteryView.EmptyState.ACCENT,
                prefixYourAddress = prefixYourAddress
            )
        }

        private fun uiItemActions(
            config: ConfigEntity
        ): Item.Actions {
            return Item.Actions(
                wallet = wallet,
                token = TokenEntity.TON,
                swapUri = config.swapUri,
                disableSwap = config.flags.disableSwap
            )
        }

        private fun uiItemsSetup(
            walletId: String,
            config: ConfigEntity,
            setupTypes: List<SetupType>
        ): List<Item> {
            val uiItems = mutableListOf<Item>()
            uiItems.add(
                Item.SetupTitle(
                    walletId = walletId,
                    showDone = !setupTypes.contains(SetupType.Backup)
                )
            )
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
                        wallet = wallet,
                        settingsType = Item.SetupSwitch.TYPE_BIOMETRIC
                    )

                    SetupType.Push -> Item.SetupSwitch(
                        position = ListCell.Position.FIRST,
                        iconRes = UIKitIcon.ic_bell_28,
                        textRes = Localization.setup_finish_push,
                        enabled = false,
                        wallet = wallet,
                        settingsType = Item.SetupSwitch.TYPE_PUSH
                    )

                    SetupType.SafeMode -> Item.SetupLink(
                        position = position,
                        iconRes = UIKitIcon.ic_control_28,
                        textRes = Localization.setup_safe_mode,
                        link = "tonkeeper://security",
                        blue = true,
                        walletId = wallet.id,
                        settingsType = Item.SetupLink.TYPE_SAFE_MODE
                    )
                }
                uiItems.add(item)
            }
            return uiItems.toList()
        }

        private fun uiItemsCards(hiddenBalance: Boolean): List<Item> {
            if (cards == null) {
                return emptyList()
            }

            val uiItems = mutableListOf<Item>()
            uiItems.addAll(cards.list.map { item ->
                Item.Card(
                    lastFourDigits = item.lastFourDigits,
                    title = item.title,
                    subtitle = item.subtitle,
                    path = item.path,
                    hiddenBalance = hiddenBalance,
                    testnet = wallet.testnet,
                    isSingle = cards.list.size == 1,
                    kindResId = item.kindResId
                )
            })
            uiItems.add(Item.AddCard())
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
            if (setup.safeModeBlock) {
                setupTypes.add(SetupType.SafeMode)
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
            prefixYourAddress: Boolean,
        ): List<Item> {
            val uiItems = mutableListOf<Item>()
            if (apkStatus != APKManager.Status.Default) {
                uiItems.add(Item.ApkStatus(apkStatus))
            }
            if (alerts.isNotEmpty()) {
                for (alert in alerts) {
                    uiItems.add(Item.Alert(alert))
                    uiItems.add(Item.Space(true))
                }
            }
            uiItems.add(uiItemBalance(hiddenBalance, status, lastUpdatedFormat, prefixYourAddress))
            uiItems.add(uiItemActions(config))
            if (!dAppNotifications.isEmpty) {
                uiItems.add(Item.Push(dAppNotifications.pushes))
            }

            setup?.let {
                val setupTypes = createSetupTypes(it)
                if (setupTypes.isNotEmpty()) {
                    uiItems.addAll(uiItemsSetup(wallet.id, config, setupTypes))
                }
            }

            if (cards !== null) {
                if (cards.list.isNotEmpty()) {
                    uiItems.add(Item.Space(large = setup == null && dAppNotifications.isEmpty))
                    uiItems.add(Item.Cards(uiItemsCards(hiddenBalance)))
                } else if (!cards.dismissed) {
                    uiItems.add(Item.Space(large = setup == null && dAppNotifications.isEmpty))
                    uiItems.add(Item.CardsBanner(setup != null))
                }
            }

            uiItems.addAll(uiItemsTokens(hiddenBalance))
            return uiItems.toList()
        }
    }

    data class DAppNotifications(
        val pushes: List<AppPushEntity> = emptyList(),
    ) : State() {

        val isEmpty: Boolean
            get() = pushes.isEmpty()
    }

    data class Settings(
        val hiddenBalance: Boolean,
        val config: ConfigEntity,
        val status: Item.Status
    ) : State()
}