package com.tonapps.tonkeeper.ui.component.wallet

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.WindowInsetsCompat
import com.tonapps.emoji.ui.EmojiView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import com.tonapps.wallet.data.account.entities.WalletLabel
import uikit.drawable.BarDrawable
import uikit.drawable.HeaderDrawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.setPaddingTop
import uikit.extensions.statusBarHeight
import uikit.widget.RowLayout
import kotlin.math.abs

class WalletHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle),
    BarDrawable.BarDrawableOwner {

    private val barHeight = context.getDimensionPixelSize(uikit.R.dimen.barHeight)
    private var topOffset: Int = statusBarHeight
        set(value) {
            if (field != value) {
                field = value
                setPaddingTop(value)
                requestLayout()
            }
        }


    var doWalletSwipe: ((right: Boolean) -> Unit)? = null

    private val swipeGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            ev1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val threshold = 100
            val velocityThreshold = 100
            val e1 = ev1 ?: return false
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > threshold && abs(velocityX) > velocityThreshold) {
                    doWalletSwipe?.invoke(diffX > 0)
                    return true
                }
            }

            return false
        }
    }

    private val swipeDetector = GestureDetector(context, swipeGestureListener, handler)
    private val settingsView: View
    private val walletView: View
    private val emojiView: EmojiView
    private val nameView: AppCompatTextView
    private val arrowView: AppCompatImageView
    private val drawable = HeaderDrawable(context).apply {
        setColor(context.backgroundTransparentColor)
    }

    var onSettingsClick: (() -> Unit)? = null
        set(value) {
            field = value
            settingsView.setOnClickListener { value?.invoke() }
        }

    var onWalletClick: (() -> Unit)? = null
        set(value) {
            field = value
            walletView.setOnClickListener { value?.invoke() }
        }

    init {
        setPaddingHorizontal(context.getDimensionPixelSize(uikit.R.dimen.offsetMedium))
        setPaddingTop(topOffset)
        super.setBackground(drawable)
        inflate(context, R.layout.view_wallet_header, this)
        settingsView = findViewById(R.id.settings)
        walletView = findViewById(R.id.wallet)
        walletView.setOnTouchListener { v, event -> swipeDetector.onTouchEvent(event) }
        emojiView = findViewById(R.id.wallet_emoji)
        nameView = findViewById(R.id.wallet_name)
        arrowView = findViewById(R.id.wallet_arrow)
    }

    override fun setDivider(value: Boolean) {
        drawable.setDivider(value)
    }

    fun setWallet(walletLabel: WalletLabel) {
        if (walletLabel.isEmpty) {
            walletView.visibility = View.GONE
            return
        }

        walletView.visibility = View.VISIBLE
        nameView.text = walletLabel.name
        emojiView.setEmoji(walletLabel.emoji)
        walletView.backgroundTintList = ColorStateList.valueOf(walletLabel.color)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
        val statusInsets = compatInsets.getInsets(WindowInsetsCompat.Type.statusBars())
        topOffset = statusInsets.top
        return super.onApplyWindowInsets(insets)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(barHeight + topOffset, MeasureSpec.EXACTLY))
    }

}