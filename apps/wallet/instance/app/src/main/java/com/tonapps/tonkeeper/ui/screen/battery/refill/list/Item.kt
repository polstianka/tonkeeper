package com.tonapps.tonkeeper.ui.screen.battery.refill.list

import android.net.Uri
import com.android.billingclient.api.ProductDetails
import com.tonapps.tonkeeper.billing.ProductEntity
import com.tonapps.wallet.api.entity.IAPPackageId
import com.tonapps.tonkeeper.ui.screen.battery.refill.entity.PromoState
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.token.entities.AccountTokenEntity

sealed class Item(type: Int) : BaseListItem(type) {

    companion object {
        const val TYPE_BATTERY = 0
        const val TYPE_SPACE = 1
        const val TYPE_RECHARGE_METHOD = 2
        const val TYPE_GIFT = 3
        const val TYPE_SETTINGS = 4
        const val TYPE_REFUND = 5
        const val TYPE_PROMO = 6
        const val TYPE_IAP = 7
        const val TYPE_RESTORE_IAP = 8
    }

    data class Battery(
        val balance: Float,
        val beta: Boolean,
        val changes: Int,
        val formattedChanges: CharSequence,
    ) : Item(TYPE_BATTERY)

    data class RechargeMethod(
        val position: ListCell.Position,
        val token: AccountTokenEntity,
        val wallet: WalletEntity,
    ) : Item(TYPE_RECHARGE_METHOD) {

        val symbol: String
            get() = token.symbol

        val imageUri: Uri
            get() = token.imageUri
    }

    data class Gift(
        val wallet: WalletEntity,
        val position: ListCell.Position,
    ) : Item(TYPE_GIFT)

    data class Refund(
        val refundUrl: String,
        val wallet: WalletEntity,
    ) : Item(TYPE_REFUND)

    data class Promo(
        val promoState: PromoState,
    ) : Item(TYPE_PROMO) {

        val appliedPromo: String
            get() = (promoState as? PromoState.Applied)?.appliedPromo ?: ""

        val isLoading: Boolean
            get() = promoState is PromoState.Loading

        val isError: Boolean
            get() = promoState is PromoState.Error

        val initialPromo: String?
            get() = (promoState as? PromoState.Loading)?.initialPromo
    }

    data class Settings(
        val supportedTransactions: Array<BatteryTransaction>,
    ) : Item(TYPE_SETTINGS)

    data class IAPPack(
        val position: ListCell.Position,
        val product: ProductEntity,
        // val isEnabled: Boolean,
        val charges: Int,
        val transactions: Map<BatteryTransaction, Int>
    ) : Item(TYPE_IAP) {

        val formattedPrice: CharSequence
            get() = product.priceFormat

        val packType: IAPPackageId
            get() = product.packType
    }

    data object RestoreIAP : Item(TYPE_RESTORE_IAP)

    data object Space : Item(TYPE_SPACE)
}