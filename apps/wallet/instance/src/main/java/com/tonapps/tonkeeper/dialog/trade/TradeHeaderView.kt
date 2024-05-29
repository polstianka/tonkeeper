package com.tonapps.tonkeeper.dialog.trade

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.WindowInsetsCompat
import uikit.R
import uikit.drawable.BarDrawable
import uikit.drawable.HeaderDrawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.setPaddingTop
import uikit.extensions.useAttributes
import uikit.widget.RowLayout
import com.tonapps.tonkeeperx.R as tonR

open class TradeHeaderView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : RowLayout(context, attrs, defStyle), BarDrawable.BarDrawableOwner {
        private companion object {
            private const val ANIMATION_DURATION = 180L
        }

        val closeView: AppCompatImageView
        val actionView: AppCompatImageView
        val tabSwitcherView: TabSwitcherView

        private val barHeight = context.getDimensionPixelSize(R.dimen.barHeight)
        private var ignoreSystemOffset = false
        private var topOffset: Int = 0
            set(value) {
                if (field != value) {
                    field = value
                    setPaddingTop(value)
                    requestLayout()
                }
            }

        private val drawable = HeaderDrawable(context)

        var doOnCloseClick: (() -> Unit)? = null
            set(value) {
                field = value
                closeView.setOnClickListener {
                    if (it.alpha != 0f) {
                        value?.invoke()
                    }
                }
            }

        var doOnActionClick: ((view: View) -> Unit)? = null
            set(value) {
                field = value
                actionView.setOnClickListener {
                    if (it.alpha != 0f) {
                        value?.invoke(it)
                    }
                }
            }

        var doOnTabClick: ((Int) -> Unit)? = null
            set(value) {
                field = value
                tabSwitcherView.setTabClickedListener { tab ->
                    value?.invoke(tab)
                }
            }

        init {
            super.setBackground(drawable)
            setPaddingHorizontal(context.getDimensionPixelSize(R.dimen.offsetMedium))

            inflate(context, tonR.layout.view_trade_header, this)

            closeView = findViewById(tonR.id.header_close)
            actionView = findViewById(tonR.id.header_action)
            tabSwitcherView = findViewById(com.tonapps.tonkeeperx.R.id.tab_switcher)
            tabSwitcherView.setTabs(
                listOf(
                    context.getString(com.tonapps.wallet.localization.R.string.buy),
                    context.getString(com.tonapps.wallet.localization.R.string.sell),
                ),
            )
            context.useAttributes(attrs, R.styleable.HeaderView) {
                ignoreSystemOffset = it.getBoolean(R.styleable.HeaderView_ignoreSystemOffset, false)
                val iconResId = it.getResourceId(R.styleable.HeaderView_android_icon, 0)
                setIcon(iconResId)

                val actionResId = it.getResourceId(R.styleable.HeaderView_android_action, 0)
                setAction(actionResId)
            }
        }

        override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
            if (ignoreSystemOffset) {
                return super.onApplyWindowInsets(insets)
            }
            val compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
            val statusInsets = compatInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            topOffset = statusInsets.top
            return super.onApplyWindowInsets(insets)
        }

        override fun setDivider(value: Boolean) {
            drawable.setDivider(value)
        }

        fun setColor(color: Int) {
            drawable.setColor(color)
        }

        fun setIcon(
            @DrawableRes resId: Int,
        ) {
            setDrawableForView(closeView, resId)
        }

        fun setAction(
            @DrawableRes resId: Int,
        ) {
            setDrawableForView(actionView, resId)
        }

        private fun setDrawableForView(
            view: AppCompatImageView,
            @DrawableRes resId: Int,
        ) {
            if (resId == 0) {
                view.alpha = 0f
            } else {
                view.setImageResource(resId)
                view.alpha = 1f
            }
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(barHeight + topOffset, MeasureSpec.EXACTLY),
            )
        }
    }
