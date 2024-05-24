package com.tonapps.tonkeeper.ui.screen.wallet.list.holder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toRectF
import com.tonapps.tonkeeper.api.shortAddress
import com.tonapps.tonkeeper.extensions.copyWithToast
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.UIKitColor
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.accentPurpleColor
import com.tonapps.uikit.color.iconSecondaryColor
import com.tonapps.uikit.color.resolveColor
import com.tonapps.uikit.color.stateList
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import com.tonapps.wallet.data.settings.SettingsRepository
import uikit.HapticHelper
import uikit.base.BaseDrawable
import uikit.extensions.dp
import uikit.extensions.withAlpha
import uikit.widget.LoaderView

class BalanceHolder(
    parent: ViewGroup,
    private val settingsRepository: SettingsRepository
): Holder<Item.Balance>(parent, R.layout.view_wallet_data) {

    private val balanceView = itemView.findViewById<AppCompatTextView>(R.id.wallet_balance)
    private val walletAddressView = itemView.findViewById<AppCompatTextView>(R.id.wallet_address)
    private val walletLoaderView = itemView.findViewById<LoaderView>(R.id.wallet_loader)
    private val walletTypeView = itemView.findViewById<AppCompatTextView>(R.id.wallet_type)

    init {
        balanceView.setOnClickListener {
            settingsRepository.hiddenBalances = !settingsRepository.hiddenBalances
            HapticHelper.impactLight(context)
        }
        walletLoaderView.setColor(context.iconSecondaryColor)
        walletLoaderView.setTrackColor(context.iconSecondaryColor.withAlpha(.32f))
        walletTypeView.backgroundTintList = context.accentOrangeColor.withAlpha(.16f).stateList
    }

    override fun onBind(item: Item.Balance) {
        if (item.hiddenBalance) {
            balanceView.text = HIDDEN_BALANCE
            balanceView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            balanceView.background = HiddenBalanceDrawable(context)
        } else {
            balanceView.text = item.balance
            balanceView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f)
            balanceView.background = null
        }

        setWalletType(item.walletType)
        setWalletState(item.status, item.address, item.walletType)
    }

    private fun setWalletState(state: Item.Status, address: String, walletType: WalletType) {
        if (state == Item.Status.Updating) {
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
                if (walletType == WalletType.Default) {
                    context.copyWithToast(address)
                } else {
                    context.copyWithToast(address, getTypeColor(walletType))
                }
            }
        }
    }

    private fun setWalletType(type: WalletType) {
        val resId = when (type) {
            WalletType.Watch -> Localization.watch_only
            WalletType.Testnet -> Localization.testnet
            WalletType.Signer -> Localization.signer
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

    private fun getTypeColor(type: WalletType): Int {
        return when (type) {
            WalletType.Signer -> context.accentPurpleColor
            else -> context.accentOrangeColor
        }
    }

    private class HiddenBalanceDrawable(context: Context): BaseDrawable() {

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