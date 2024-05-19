package com.tonapps.tonkeeper.ui.screen.collectibles.list.holder

import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.drawee.generic.RoundingParams
import com.facebook.imagepipeline.postprocessors.BlurPostProcessor
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.tonapps.tonkeeper.ui.screen.collectibles.list.Item
import com.tonapps.tonkeeper.ui.screen.nft.NftScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundHighlightedColor
import com.tonapps.uikit.color.stateList
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import com.tonapps.wallet.localization.Localization
import uikit.extensions.getDimension
import uikit.extensions.round
import uikit.navigation.Navigation
import uikit.widget.FrescoView

class NftHolder(parent: ViewGroup): Holder<Item.Nft>(parent, R.layout.view_collectibles) {

    private val radius = context.getDimension(uikit.R.dimen.cornerMedium)

    private val imageView = findViewById<FrescoView>(R.id.image)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val collectionView = findViewById<AppCompatTextView>(R.id.collection)
    private val saleBadgeView = findViewById<AppCompatImageView>(R.id.sale_badge)

    init {
        itemView.foreground = RippleDrawable(context.backgroundHighlightedColor.stateList, null, null)

        itemView.round(radius.toInt())
        imageView.hierarchy.roundingParams = RoundingParams.fromCornersRadii(radius, radius, 0f, 0f)
    }

    override fun onBind(item: Item.Nft) {
        itemView.setOnClickListener {
            Navigation.from(context)?.add(NftScreen.newInstance(item.entity))
        }
        loadImage(item.imageURI, item.hiddenBalance)
        if (item.hiddenBalance) {
            titleView.text = HIDDEN_BALANCE
            collectionView.text = HIDDEN_BALANCE
        } else {
            titleView.text = item.title
            collectionView.text = item.collectionName.ifEmpty {
                getString(Localization.unnamed_collection)
            }
        }
        saleBadgeView.visibility = if (item.sale) View.VISIBLE else View.GONE
    }

    private fun loadImage(uri: Uri, blur: Boolean) {
        if (blur) {
            val request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setPostprocessor(BlurPostProcessor(25, context, 3))
                .build()
            imageView.setImageRequest(request)
        } else {
            imageView.setImageURI(uri, null)
        }

    }
}