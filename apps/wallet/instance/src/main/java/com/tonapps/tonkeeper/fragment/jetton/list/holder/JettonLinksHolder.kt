package com.tonapps.tonkeeper.fragment.jetton.list.holder

import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayout
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItem
import com.tonapps.tonkeeper.ui.screen.stake.view.SocialLinkView
import com.tonapps.tonkeeperx.R
import uikit.drawable.SpaceDrawable
import uikit.extensions.dp

class JettonLinksHolder(
    parent: ViewGroup
) : JettonHolder<JettonItem.Links>(parent, R.layout.view_jetton_links) {

    private val linksView = findViewById<FlexboxLayout>(R.id.social_links)

    override fun onBind(item: JettonItem.Links) {
        linksView.removeAllViews()
        linksView.setDividerDrawable(SpaceDrawable(8.dp))
        item.links.forEach {
            linksView.addView(SocialLinkView(context).apply {
                setLink(it)
            })
        }
    }
}