package com.tonapps.tonkeeper.ui.screen.swap.assets.list

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.common.util.UriUtil
import com.tonapps.tonkeeper.ui.screen.swap.assets.AssetPickerScreen
import com.tonapps.tonkeeper.ui.screen.swap.main.widget.SwapTokenView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.iconSecondaryColor
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.LinearLayoutManager
import com.tonapps.uikit.list.ListCell
import uikit.extensions.drawable
import uikit.extensions.updateVisibility
import uikit.widget.FrescoView

abstract class ViewHolder<T : Item>(
    screen: AssetPickerScreen,
    parent: ViewGroup,
    val itemType: ListItemType,
    resId: Int
): BaseListHolder<Item>(parent, resId) {
    companion object {
        fun valueOf(screen: AssetPickerScreen, parent: ViewGroup, type: ListItemType) : ViewHolder<out Item> {
            return when (type) {
                ListItemType.HEADER -> TitleViewHolder(screen, parent)
                ListItemType.SUGGESTED_TOKENS_LIST -> SuggestedTokensViewHolder(screen, parent)
                ListItemType.TOKEN -> TokenViewHolder(screen, parent)
                ListItemType.TOKEN_PATCH -> PatchViewHolder(screen, parent)
                ListItemType.EMPTY -> EmptyViewHolder(screen, parent)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    final override fun onBind(item: Item) {
        val typed = item as T
        itemView.tag = item
        onBindTyped(typed)
    }

    abstract fun onBindTyped(item: T)
}

class TitleViewHolder(screen: AssetPickerScreen, parent: ViewGroup) : ViewHolder<HeaderItem>(
    screen,
    parent,
    ListItemType.HEADER,
    R.layout.view_asset_title
) {
    private val titleView = itemView as AppCompatTextView

    override fun onBindTyped(item: HeaderItem) {
        titleView.text = titleView.context.getString(item.titleRes)
    }
}

class SuggestedTokensViewHolder(screen: AssetPickerScreen, parent: ViewGroup) : ViewHolder<SuggestedTokensItem>(
    screen,
    parent,
    ListItemType.SUGGESTED_TOKENS_LIST,
    R.layout.view_asset_suggested_tokens
) {
    private val recyclerView = itemView as RecyclerView
    private val adapter: Adapter = Adapter(screen)

    init {
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(Decoration(context))
    }

    override fun onBindTyped(item: SuggestedTokensItem) {
        adapter.submitList(item.list)
    }
}

class TokenViewHolder(screen: AssetPickerScreen, parent: ViewGroup) : ViewHolder<TokenItem>(
    screen,
    parent,
    ListItemType.TOKEN,
    R.layout.view_cell_jetton
) {
    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val rateView = findViewById<AppCompatTextView>(R.id.rate)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)
    private val kindView = findViewById<AppCompatTextView>(R.id.kind)

    private val textPrimaryColor = context.textPrimaryColor
    private val textSecondaryColor = context.textSecondaryColor

    private val failureIcon: Drawable

    init {
        itemView.setOnClickListener {
            val data = it.tag as TokenItem
            screen.selectToken(data.entity)
        }
        failureIcon = context.drawable(R.drawable.ic_question_mark_16)
        failureIcon.setTint(context.iconSecondaryColor)
    }

    private var position: ListCell.Position? = null
        set(value) {
            if (field == value) return
            field = value
            itemView.background = value?.drawable(context)
        }

    override fun onBindTyped(item: TokenItem) {
        this.position = item.position
        item.entity.let {
            if (UriUtil.isLocalResourceUri(it.token.imageUri)) {
                iconView.setFailureImage(null)
            } else {
                iconView.setFailureImage(failureIcon)
            }
            iconView.setImageURI(it.token.imageUri, this)
            titleView.text = it.token.symbol
            balanceView.text = it.balance?.stringRepresentation ?: "0"
            balanceView.setTextColor(if (it.hasFunds) textPrimaryColor else textSecondaryColor)
            balanceFiatView.text = it.balanceInUserCurrency?.stringRepresentation ?: it.balanceInUsd?.stringRepresentation ?: ""
            rateView.text = it.token.name
            val specialBadge = it.specialBadge
            if (specialBadge.isNullOrEmpty()) {
                kindView.updateVisibility(View.GONE)
            } else {
                kindView.text = specialBadge
                kindView.updateVisibility(View.VISIBLE)
            }
        }
    }
}

class PatchViewHolder(screen: AssetPickerScreen, parent: ViewGroup) : ViewHolder<TokenPatchItem>(
    screen,
    parent,
    ListItemType.TOKEN_PATCH,
    R.layout.view_asset_suggestion
) {
    private val tokenView = itemView as SwapTokenView

    init {
        tokenView.setOnClickListener {
            val data = it.tag as TokenPatchItem
            screen.selectToken(data.entity)
        }
    }

    override fun onBindTyped(item: TokenPatchItem) {
        tokenView.token = item.entity.token
    }
}

class EmptyViewHolder(screen: AssetPickerScreen, parent: ViewGroup) : ViewHolder<EmptyItem>(
    screen,
    parent,
    ListItemType.EMPTY,
    R.layout.view_asset_empty
) {
    private val emptyView = itemView as AppCompatTextView

    override fun onBindTyped(item: EmptyItem) {
        emptyView.text = context.getString(item.emptyRes)
    }
}