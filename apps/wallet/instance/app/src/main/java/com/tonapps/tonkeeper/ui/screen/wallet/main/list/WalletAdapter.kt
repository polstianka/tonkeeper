package com.tonapps.tonkeeper.ui.screen.wallet.main.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.card.entity.CardScreenPath
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.ActionsHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.AlertHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.ApkHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.BalanceHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.CardBannerHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.CardsHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.ManageHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.PushHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.SetupLinkHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.SetupSwitchHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.SetupTitleHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.SkeletonHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.SpaceHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.StakedHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.TitleHolder
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.holder.TokenHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class WalletAdapter(private val openCards: (path: CardScreenPath?) -> Unit): BaseListAdapter() {

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseListHolder<out BaseListItem> {
        return when(viewType) {
            Item.TYPE_BALANCE -> BalanceHolder(parent)
            Item.TYPE_ACTIONS -> ActionsHolder(parent)
            Item.TYPE_TOKEN -> TokenHolder(parent)
            Item.TYPE_SPACE -> SpaceHolder(parent)
            Item.TYPE_SKELETON -> SkeletonHolder(parent)
            Item.TYPE_PUSH -> PushHolder(parent)
            Item.TYPE_TITLE -> TitleHolder(parent)
            Item.TYPE_MANAGE -> ManageHolder(parent)
            Item.TYPE_ALERT -> AlertHolder(parent)
            Item.TYPE_SETUP_TITLE -> SetupTitleHolder(parent)
            Item.TYPE_SETUP_SWITCH -> SetupSwitchHolder(parent)
            Item.TYPE_SETUP_LINK -> SetupLinkHolder(parent)
            Item.TYPE_STAKED -> StakedHolder(parent)
            Item.TYPE_APK_STATUS -> ApkHolder(parent)
            Item.TYPE_CARDS_BANNER -> CardBannerHolder(parent, openCards)
            Item.TYPE_CARDS -> CardsHolder(parent, openCards)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }



    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.isNestedScrollingEnabled = true
    }

}