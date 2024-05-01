package com.tonapps.tonkeeper.ui.screen.swap

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.extensions.drawable
import uikit.widget.FrescoView

class Adapter(
    private val onClick: (AssetModel) -> Unit
) : BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return AssetHolder(parent, onClick)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setHasFixedSize(false)
        recyclerView.isNestedScrollingEnabled = true
    }
}

class AssetHolder(
    parent: ViewGroup,
    val onClick: (AssetModel) -> Unit
) : BaseListHolder<AssetModel>(parent, R.layout.view_asset_picker_item) {

    private val icon: FrescoView = findViewById(R.id.asset_icon)
    private val code: TextView = findViewById(R.id.asset_code)
    private val name: TextView = findViewById(R.id.asset_name)
    private val balance: TextView = findViewById(R.id.asset_balance)
    private val fiatBalance: TextView = findViewById(R.id.asset_fiat_balance)

    override fun onBind(item: AssetModel) {
        icon.setImageURI(item.token.imageUri)
        code.text = item.token.symbol
        name.text = item.token.name
        balance.text = CurrencyFormatter.format("", item.balance, item.token.decimals)
        itemView.setOnClickListener { onClick(item) }
        itemView.background = item.position.drawable(context)
    }

}

data class AssetModel(
    val token: TokenEntity,
    val balance: Float,
    val walletAddress: String,
    override val position: ListCell.Position
) : BaseListItem(), ListCell