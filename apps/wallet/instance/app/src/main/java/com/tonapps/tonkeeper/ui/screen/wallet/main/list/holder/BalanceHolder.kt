package com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.icu.CurrencyFormatter.withCustomSymbol
import com.tonapps.tonkeeper.api.shortAddress
import com.tonapps.tonkeeper.extensions.copyWithToast
import com.tonapps.tonkeeper.ui.screen.backup.main.BackupScreen
import com.tonapps.tonkeeper.ui.screen.battery.BatteryScreen
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item
import com.tonapps.tonkeeper.view.BatteryView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.UIKitColor
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.accentPurpleColor
import com.tonapps.uikit.color.accentRedColor
import com.tonapps.uikit.color.iconSecondaryColor
import com.tonapps.uikit.color.resolveColor
import com.tonapps.uikit.color.stateList
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.data.account.Wallet
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Localization
import uikit.HapticHelper
import uikit.base.BaseDrawable
import uikit.extensions.dp
import uikit.extensions.expandTouchArea
import uikit.extensions.withAlpha
import uikit.navigation.Navigation
import uikit.widget.LoaderView

class BalanceHolder(
    parent: ViewGroup,
    private val settingsRepository: SettingsRepository
) : Holder<Item.Balance>(parent, R.layout.view_wallet_data) {

    private val balanceView = itemView.findViewById<AppCompatTextView>(R.id.wallet_balance)
    private val batteryView = itemView.findViewById<BatteryView>(R.id.wallet_battery)
    private val walletAddressView = itemView.findViewById<AppCompatTextView>(R.id.wallet_address)
    private val walletLoaderView = itemView.findViewById<LoaderView>(R.id.wallet_loader)
    private val walletTypeView = itemView.findViewById<AppCompatTextView>(R.id.wallet_type)
    private val backupIconView = itemView.findViewById<AppCompatImageView>(R.id.backup_icon)

    init {
        balanceView.setOnClickListener {
            settingsRepository.hiddenBalances = !settingsRepository.hiddenBalances
            HapticHelper.impactLight(context)
        }
        walletLoaderView.setColor(context.iconSecondaryColor)
        walletLoaderView.setTrackColor(context.iconSecondaryColor.withAlpha(.32f))
        walletTypeView.backgroundTintList = context.accentOrangeColor.withAlpha(.16f).stateList
        backupIconView.setOnClickListener {
            Navigation.from(context)?.add(BackupScreen.newInstance())
        }
        batteryView.expandTouchArea(left = 0, top = 10.dp, right = 24.dp, bottom = 10.dp)
        batteryView.setOnClickListener {
            Navigation.from(context)?.add(BatteryScreen.newInstance())
        }
    }

    override fun onBind(item: Item.Balance) {
        if (item.hiddenBalance) {
            balanceView.text = HIDDEN_BALANCE
            balanceView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            balanceView.background = HiddenBalanceDrawable(context)
        } else {
            balanceView.text = item.balance.withCustomSymbol(context)
            balanceView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f)
            balanceView.background = null
        }

        val requestBackup =
            (item.walletType == Wallet.Type.Default || item.walletType == Wallet.Type.Lockup) && !item.hasBackup

        if (requestBackup && item.balanceType == Item.BalanceType.Huge) {
            balanceView.setTextColor(context.accentRedColor)
            backupIconView.imageTintList = context.accentRedColor.stateList
            backupIconView.visibility = View.VISIBLE
        } else if (requestBackup && item.balanceType == Item.BalanceType.Positive) {
            balanceView.setTextColor(context.accentOrangeColor)
            backupIconView.imageTintList = context.accentOrangeColor.stateList
            backupIconView.visibility = View.VISIBLE
        } else {
            balanceView.setTextColor(context.textPrimaryColor)
            backupIconView.visibility = View.GONE
        }


        if (item.showBattery) {
            batteryView.visibility = View.VISIBLE
            batteryView.setBatteryLevel(item.batteryBalance.value.toFloat())
            batteryView.setEmptyState(item.batteryEmptyState)
        } else {
            batteryView.visibility = View.GONE
        }

        setWalletType(item.walletType, item.walletVersion)
        setWalletState(item.status, item.address, item.walletType, item.lastUpdatedFormat)
    }

    private fun setWalletState(
        state: Item.Status,
        address: String,
        walletType: Wallet.Type,
        lastUpdatedFormat: String,
    ) {
        if (state == Item.Status.LastUpdated) {
            walletLoaderView.visibility = View.GONE
            walletAddressView.text = context.getString(Localization.last_updated, lastUpdatedFormat)
            walletAddressView.setTextColor(context.textSecondaryColor)
        } else if (state == Item.Status.Updating) {
            walletLoaderView.visibility = View.VISIBLE
            walletAddressView.setText(Localization.updating)
            walletAddressView.setTextColor(context.textSecondaryColor)
        } else if (state == Item.Status.SendingTransaction) {
            walletLoaderView.visibility = View.VISIBLE
            walletAddressView.setText(Localization.sending_transaction)
            walletAddressView.setTextColor(context.textSecondaryColor)
        } else if (state == Item.Status.TransactionConfirmed) {
            walletLoaderView.visibility = View.GONE
            walletAddressView.setText(Localization.transaction_confirmed)
            walletAddressView.setTextColor(context.accentGreenColor)
        } else if (state == Item.Status.NoInternet) {
            walletLoaderView.visibility = View.GONE
            walletAddressView.setText(Localization.no_internet_connection)
            walletAddressView.setTextColor(context.accentOrangeColor)
        } else {
            walletLoaderView.visibility = View.GONE
            walletAddressView.text = address.shortAddress
            walletAddressView.setTextColor(context.textSecondaryColor)
            walletAddressView.setOnClickListener {
                if (walletType == Wallet.Type.Testnet || walletType == Wallet.Type.Watch) {
                    context.copyWithToast(address, getTypeColor(walletType))
                } else {
                    context.copyWithToast(address)
                }
            }
        }
    }

    private fun setWalletType(type: Wallet.Type, version: WalletVersion) {
        if (version == WalletVersion.V5R1 || version == WalletVersion.V5BETA) {
            val color = context.accentGreenColor
            walletTypeView.visibility = View.VISIBLE
            walletTypeView.setTextColor(color)
            walletTypeView.backgroundTintList = color.withAlpha(.16f).stateList
            walletTypeView.setText(if (version == WalletVersion.V5BETA) Localization.w5beta else Localization.w5)
            return
        }

        val resId = when (type) {
            Wallet.Type.Watch -> Localization.watch_only
            Wallet.Type.Testnet -> Localization.testnet
            Wallet.Type.Signer, Wallet.Type.SignerQR -> Localization.signer
            Wallet.Type.Ledger -> Localization.ledger
            else -> {
                walletTypeView.visibility = View.GONE
                return
            }
        }

        val color = getTypeColor(type)
        walletTypeView.visibility = View.VISIBLE
        walletTypeView.setTextColor(color)
        walletTypeView.backgroundTintList = color.withAlpha(.16f).stateList
        walletTypeView.setText(resId)
    }

    private fun getTypeColor(type: Wallet.Type): Int {
        return when (type) {
            Wallet.Type.Ledger -> context.accentGreenColor
            Wallet.Type.Signer, Wallet.Type.SignerQR -> context.accentPurpleColor
            else -> context.accentOrangeColor
        }
    }

    private class HiddenBalanceDrawable(context: Context) : BaseDrawable() {

        private val radius = 20f.dp
        private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.resolveColor(UIKitColor.buttonSecondaryBackgroundColor)
        }
        private val rect = RectF()

        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(rect, radius, radius, backgroundPaint)
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            rect.left = bounds.left.toFloat()
            rect.top = bounds.top.toFloat()
            rect.right = bounds.right.toFloat()
            rect.bottom = bounds.bottom.toFloat() - 14f.dp
        }

    }
}