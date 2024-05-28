package com.tonapps.tonkeeper.ui.screen.buysell

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout
import uikit.R
import uikit.drawable.BarDrawable
import uikit.drawable.HeaderDrawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.setPaddingTop
import uikit.widget.RowLayout

class FiatHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RowLayout(context, attrs, defStyle), BarDrawable.BarDrawableOwner {

    private companion object {
        private const val ANIMATION_DURATION = 180L
    }

    private val countryTextView: AppCompatTextView
    private val closeView: AppCompatImageView
    // private val buyView: TabItem
    //private val sellView: TabItem

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

    var doOnCountryClick: (() -> Unit)? = null
        set(value) {
            field = value
            countryTextView.setOnClickListener {
                value?.invoke()
            }
        }

    var doOnCloseClick: (() -> Unit)? = null
        set(value) {
            field = value
            closeView.setOnClickListener {
                value?.invoke()
            }
        }

    var doOnTypeChange: (FiatOperation) -> Unit = {}

    var countryCode: CharSequence
        get() = countryTextView.text
        set(value) {
            countryTextView.text = value
        }

    init {
        super.setBackground(drawable)
        setPaddingHorizontal(context.getDimensionPixelSize(R.dimen.offsetMedium))

        inflate(context, R.layout.view_fiat_header, this)

        countryTextView = findViewById(R.id.fiat_country)
        closeView = findViewById(R.id.header_close)

        findViewById<TabLayout>(R.id.tab_layout).addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab) {
                when (p0.position) {
                    0 -> doOnTypeChange(FiatOperation.Buy)
                    1 -> doOnTypeChange(FiatOperation.Sell)
                }
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) = Unit

            override fun onTabReselected(p0: TabLayout.Tab?) = Unit

        })
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(barHeight + topOffset, MeasureSpec.EXACTLY)
        )
    }
}