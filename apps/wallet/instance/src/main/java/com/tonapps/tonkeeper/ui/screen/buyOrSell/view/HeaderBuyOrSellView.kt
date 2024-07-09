package com.tonapps.tonkeeper.ui.screen.buyOrSell.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tonapps.tonkeeperx.R

class HeaderBuyOrSellView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private val tabLayout: TabLayout
    private val header_close: AppCompatImageView
    private val btnSelectLanguage: MaterialCardView
    private val txSelectedCurrency: TextView

    var doOnCloseClick: (() -> Unit)? = null
        set(value) {
            field = value
            header_close.setOnClickListener {
                if (it.alpha != 0f) {
                    value?.invoke()
                }
            }
        }


    var doOnSelectLanguageClick: (() -> Unit)? = null
        set(value) {
            field = value
            btnSelectLanguage.setOnClickListener {
                value?.invoke()
            }
        }


    fun setSelectedTypeCurrency(newText: String) {
        txSelectedCurrency.text = newText
    }


    fun setupWithViewPager(viewPager: ViewPager2) {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Buy"
                1 -> tab.text = "Sell"
            }
        }.attach()
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_header_buy_or_sell_view, this, true)

        tabLayout = findViewById(R.id.tabLayout)
        header_close = findViewById(R.id.header_close)
        btnSelectLanguage = findViewById(R.id.btnSelectLanguage)
        txSelectedCurrency = findViewById(R.id.txSelectedCurrency)
    }

}