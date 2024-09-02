package com.tonapps.tonkeeper.ui.screen.battery.recharge.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.battery.recharge.entity.RechargePackType
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.holder.AddressHolder
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.holder.AmountHolder
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.holder.ButtonHolder
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.holder.CustomAmountHolder
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.holder.PromoHolder
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.holder.RechargePackHolder
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.holder.SpaceHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class Adapter(
    private val onAddressChange: (String) -> Unit,
    private val openAddressBook: () -> Unit,
    private val onAmountChange: (Double) -> Unit,
    private val onPackSelect: (RechargePackType) -> Unit,
    private val onCustomAmountSelect: () -> Unit,
    private val onContinue: () -> Unit,
    private val onSubmitPromo: (String) -> Unit,
): BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when(viewType) {
            Item.TYPE_RECHARGE_PACK -> RechargePackHolder(parent, onPackSelect)
            Item.TYPE_CUSTOM_AMOUNT -> CustomAmountHolder(parent, onCustomAmountSelect)
            Item.TYPE_SPACE -> SpaceHolder(parent)
            Item.TYPE_AMOUNT -> AmountHolder(parent, onAmountChange)
            Item.TYPE_ADDRESS -> AddressHolder(parent, onAddressChange, openAddressBook)
            Item.TYPE_BUTTON -> ButtonHolder(parent, onContinue)
            Item.TYPE_PROMO -> PromoHolder(parent, onSubmitPromo)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.isNestedScrollingEnabled = true
    }
}