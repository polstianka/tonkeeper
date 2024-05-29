package com.tonapps.tonkeeper.core.history.list.holder

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.postprocessors.BlurPostProcessor
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.core.history.ActionType
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.core.history.iconRes
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.tonkeeper.core.history.nameRes
import com.tonapps.tonkeeper.dialog.TransactionDialog
import com.tonapps.tonkeeper.ui.screen.dialog.encrypted.EncryptedCommentScreen
import com.tonapps.tonkeeper.ui.screen.nft.NftScreen
import com.tonapps.uikit.color.UIKitColor
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.iconSecondaryColor
import com.tonapps.uikit.color.stateList
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textTertiaryColor
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import com.tonapps.wallet.localization.Localization
import uikit.extensions.clearDrawables
import uikit.extensions.drawable
import uikit.extensions.setLeftDrawable
import uikit.navigation.Navigation
import uikit.widget.FrescoView
import uikit.widget.LoaderView

class HistoryActionHolder(
    parent: ViewGroup,
    private val disableOpenAction: Boolean
): HistoryHolder<HistoryItem.Event>(parent, R.layout.view_history_action) {

    private val amountColorReceived = context.accentGreenColor
    private val amountColorDefault = context.textPrimaryColor
    private val amountColorTertiary = context.textTertiaryColor

    private val loaderView = findViewById<LoaderView>(R.id.loader)
    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = findViewById<AppCompatTextView>(R.id.subtitle)
    private val commentView = findViewById<AppCompatTextView>(R.id.comment)
    private val amountView = findViewById<AppCompatTextView>(R.id.amount)
    private val amount2View = findViewById<AppCompatTextView>(R.id.amount2)
    private val dateView = findViewById<AppCompatTextView>(R.id.date)
    private val warningView = findViewById<AppCompatTextView>(R.id.warning)

    private val nftView = findViewById<View>(R.id.nft)
    private val nftIconView = findViewById<FrescoView>(R.id.nft_icon)
    private val nftNameView = findViewById<AppCompatTextView>(R.id.nft_name)
    private val nftCollectionView = findViewById<AppCompatTextView>(R.id.nft_collection)
    private val lockDrawable: Drawable by lazy {
        val drawable = context.drawable(UIKitIcon.ic_lock_16)
        drawable.setTint(context.accentGreenColor)
        drawable
    }

    override fun onBind(item: HistoryItem.Event) {
        commentView.clearDrawables()

        if (!disableOpenAction) {
            itemView.setOnClickListener { TransactionDialog.open(context, item) }
        }

        itemView.background = item.position.drawable(context)
        titleView.setText(item.action.nameRes)
        subtitleView.text = item.subtitle
        dateView.text = item.date

        if (item.failed) {
            iconView.setImageResource(UIKitIcon.ic_exclamationmark_circle_28)
            iconView.imageTintList = context.iconSecondaryColor.stateList
            warningView.visibility = View.VISIBLE
        } else if (item.iconURL.isNullOrEmpty()) {
            iconView.setImageResource(item.action.iconRes)
            iconView.imageTintList = context.iconSecondaryColor.stateList
            warningView.visibility = View.GONE
        } else {
            loadIcon(Uri.parse(item.iconURL))
            warningView.visibility = View.GONE
        }

        bindPending(item.pending)
        bindComment(item.comment)
        bindEncryptedComment(item.cipherText, item.address?:"")
        bindNft(item)
        bindAmount(item)
    }

    private fun decryptComment(cipherText: String, senderAddress: String) {
        Navigation.from(context)?.add(EncryptedCommentScreen.newInstance(cipherText, senderAddress))
    }

    private fun loadIcon(uri: Uri) {
        iconView.imageTintList = null
        iconView.setImageURI(uri, this)
    }

    private fun bindPending(pending: Boolean) {
        loaderView.visibility = if (pending) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun bindAmount(item: HistoryItem.Event) {
        if (item.action == ActionType.WithdrawStakeRequest) {
            amountView.setTextColor(amountColorTertiary)
        } else {
            amountView.setTextColor(getAmountColor(item.value))
        }
        if (item.hiddenBalance) {
            amountView.text = HIDDEN_BALANCE
        } else {
            amountView.text = item.value
        }


        if (item.value2.isEmpty()) {
            amount2View.visibility = View.GONE
        } else {
            amount2View.visibility = View.VISIBLE
            if (item.hiddenBalance) {
                amount2View.text = HIDDEN_BALANCE
            } else {
                amount2View.text = item.value2
            }
        }
    }

    private fun bindComment(comment: String?) {
        if (comment.isNullOrBlank()) {
            commentView.visibility = View.GONE
            return
        }

        commentView.visibility = View.VISIBLE
        commentView.text = comment
        commentView.setOnClickListener(null)
    }

    private fun bindEncryptedComment(cipherText: String?, senderAddress: String) {
        if (cipherText.isNullOrBlank()) {
            commentView.visibility = View.GONE
            return
        }

        commentView.visibility = View.VISIBLE
        commentView.text = context.getString(Localization.encrypted_comment)
        commentView.setLeftDrawable(lockDrawable)
        commentView.setOnClickListener { decryptComment(cipherText, senderAddress) }
    }

    private fun bindNft(item: HistoryItem.Event) {
        if (!item.hasNft) {
            nftView.visibility = View.GONE
            return
        }

        val nft = item.nft!!
        nftView.visibility = View.VISIBLE
        nftView.setOnClickListener {
            Navigation.from(context)?.add(NftScreen.newInstance(nft))
        }
        loadNftImage(nft.mediumUri, item.hiddenBalance)
        if (item.hiddenBalance) {
            nftNameView.text = HIDDEN_BALANCE
            nftCollectionView.text = HIDDEN_BALANCE
        } else {
            nftNameView.text = nft.name
            nftCollectionView.text = nft.collectionName.ifEmpty {
                getString(Localization.unnamed_collection)
            }
        }
    }

    private fun loadNftImage(uri: Uri, blur: Boolean) {
        if (blur) {
            val request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setPostprocessor(BlurPostProcessor(25, context, 3))
                .build()
            nftIconView.setImageRequest(request)
        } else {
            nftIconView.setImageURI(uri, null)
        }
    }

    @ColorInt
    private fun getAmountColor(amount: CharSequence): Int {
        if (amount == HistoryHelper.MINUS_SYMBOL) {
            return amountColorTertiary
        }
        return if (amount.startsWith(HistoryHelper.PLUS_SYMBOL)) {
            amountColorReceived
        } else {
            amountColorDefault
        }
    }
}