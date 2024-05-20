package com.tonapps.tonkeeper.fragment.jetton.list.holder

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.fragment.jetton.list.JettonItem
import com.tonapps.tonkeeper.ui.screen.stake.view.PoolDetailView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization

class JettonDetailsHolder(
    parent: ViewGroup
) : JettonHolder<JettonItem.Details>(parent, R.layout.view_jetton_details) {

    private val firstLine = findViewById<PoolDetailView>(R.id.first_line)
    private val secondLine = findViewById<PoolDetailView>(R.id.second_line)

    override fun onBind(item: JettonItem.Details) {
        with(firstLine) {
            maxView.isVisible = item.isApyMax
            titleTextView.text = context.getString(Localization.apy)
            valueTextView.text = item.apy
        }

        with(secondLine) {
            maxView.isVisible = false
            titleTextView.text = context.getString(Localization.min_deposit)
            valueTextView.text = item.minDeposit
        }
    }
}