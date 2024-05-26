package com.tonapps.tonkeeper.dialog.trade

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.tonapps.tonkeeperx.R
import uikit.extensions.dp
import uikit.extensions.inflate
import uikit.extensions.invisible
import uikit.extensions.visible

class TabSwitcherView : LinearLayout {
    private lateinit var tabContainer: LinearLayout
    private var lastSetIndicator: View? = null
    private val tabs: MutableList<TextView> = ArrayList()
    private var textPrimaryColor = 0
    private var textSecondaryColor = 0
    private var indicatorColor = 0
    private var selectedTabIndex = 0
    private var tabClickListener: ((Int) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    ) {
        init(context, attrs)
    }

    private fun init(
        context: Context,
        attrs: AttributeSet?,
    ) {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_tab_switcher, this, true)
        tabContainer = findViewById(R.id.tab_container)
        // Set initial colors (default values, can be modified via setter methods)
        textPrimaryColor = -0x1 // White
        textSecondaryColor = -0x777778 // Gray
        indicatorColor = -0xbd5a0b // Blue

        // Load custom attributes
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.TabSwitcherView)
            val tabLabels = a.getString(R.styleable.TabSwitcherView_tabLabels)
            val tabs = tabLabels?.split(",")
            if (tabLabels.isNullOrEmpty().not()) {
                setTabs(tabs!!)
            }
            textPrimaryColor =
                a.getColor(R.styleable.TabSwitcherView_customTextPrimaryColor, textPrimaryColor)
            setTextPrimaryColor(textPrimaryColor)
            textSecondaryColor =
                a.getColor(R.styleable.TabSwitcherView_customTextSecondaryColor, textSecondaryColor)
            setTextSecondaryColor(textSecondaryColor)
            indicatorColor =
                a.getColor(R.styleable.TabSwitcherView_customIndicatorColor, indicatorColor)
            a.recycle()
        }
    }

    fun setTabs(labels: List<String>) {
        tabContainer.removeAllViews()
        tabs.clear()

        for (index in labels.indices) {
            val tab = context.inflate(R.layout.view_tab, this, false)
            val tabTextView = tab.findViewById<TextView>(R.id.tab_text)
            tabTextView.text = labels[index].trim { it <= ' ' }
            tab.setOnClickListener {
                selectTab(index)
                tabClickListener?.invoke(index)
            }
            if (index > 0) {
                (tab.layoutParams as MarginLayoutParams).marginStart = 8.dp
            }
            tabs.add(tabTextView)
            tabContainer.addView(tab)
        }

        // Set initial state
        selectTab(0)
    }

    private fun selectTab(index: Int) {
        if (index >= 0 && index < tabs.size) {
            tabs[selectedTabIndex].setTextColor(textSecondaryColor)
            tabs[index].setTextColor(textPrimaryColor)
            selectedTabIndex = index
            val tabIndicator = tabContainer.getChildAt(index).findViewById<View>(R.id.indicator)
            tabIndicator.visible(animate = true)
            lastSetIndicator?.invisible(animate = true)
            /*tabIndicator.animate().alpha(1f).setDuration(800).start()
            lastSetIndicator?.animate()?.alpha(0f)?.setDuration(800)?.start()*/
            lastSetIndicator = tabIndicator
        }
    }

    fun setTabClickedListener(onClick: (Int) -> Unit) {
        tabClickListener = onClick
    }

    fun setTextPrimaryColor(color: Int) {
        textPrimaryColor = color
        // Update the currently selected tab color
        if (tabs.size > 0) {
            tabs[selectedTabIndex].setTextColor(textPrimaryColor)
        }
    }

    fun setTextSecondaryColor(color: Int) {
        textSecondaryColor = color
        // Update all non-selected tab colors
        for (i in tabs.indices) {
            if (i != selectedTabIndex) {
                tabs[i].setTextColor(textSecondaryColor)
            }
        }
    }
}
