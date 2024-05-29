package com.tonapps.tonkeeper.fragment.staking.deposit.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.tonapps.tonkeeper.api.getName
import com.tonapps.tonkeeper.api.icon
import com.tonapps.tonkeeperx.R

class LinksView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {
    private var constraintLayout: ConstraintLayout
    private var flow: Flow

    private var ids = mutableListOf<Int>()

    init {
        inflate(context, R.layout.view_links, this)

        constraintLayout = findViewById(R.id.links_wrapper)
        flow = findViewById(R.id.flow)
    }

    fun setLinks(links: List<Uri>) {
        clearOldViews()

        for (link in links) {
            addLink(link)
        }

        flow.referencedIds = ids.toIntArray()
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.applyTo(constraintLayout)
    }

    private fun addLink(uri: Uri) {
        val linkView = LinkView(context).apply {
            id = View.generateViewId()
        }
        linkView.titleView.text = uri.getName(context)
        linkView.iconView.setImageResource(uri.icon)
        linkView.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)
        }
        ids.add(linkView.id)
        constraintLayout.addView(linkView)
    }

    private fun clearOldViews() {
        constraintLayout.removeAllViews()
        ids.clear()
        if (constraintLayout.findViewById<Flow>(R.id.flow) == null) {
            constraintLayout.addView(flow)
        }
    }
}