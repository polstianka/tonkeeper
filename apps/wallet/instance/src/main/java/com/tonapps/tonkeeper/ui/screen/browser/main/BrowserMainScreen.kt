package com.tonapps.tonkeeper.ui.screen.browser.main

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.tonapps.tonkeeper.extensions.flagEmoji
import com.tonapps.tonkeeper.fragment.country.CountryScreen
import com.tonapps.tonkeeper.ui.screen.browser.connected.BrowserConnectedScreen
import com.tonapps.tonkeeper.ui.screen.browser.explore.BrowserExploreScreen
import com.tonapps.tonkeeper.ui.screen.browser.search.BrowserSearchScreen
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.FooterDrawable
import uikit.drawable.HeaderDrawable
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.navigation.Navigation.Companion.navigation

class BrowserMainScreen : BaseFragment(R.layout.fragment_browser_main) {

    private val mainViewModel: BrowserMainViewModel by viewModel()

    private val exploreScreen = BrowserExploreScreen.newInstance()
    private val connectedScreen = BrowserConnectedScreen.newInstance()
    private var currentScreen: BaseFragment? = null

    private lateinit var headerDrawable: HeaderDrawable
    private lateinit var headerView: View
    private lateinit var exploreView: View
    private lateinit var connectedView: View
    private lateinit var countryView: AppCompatTextView

    private lateinit var footerDrawable: FooterDrawable
    private lateinit var containerView: View
    private lateinit var searchContainerView: View
    private lateinit var searchView: View

    private val tabs: List<Tab> by lazy {
        listOf(
            Tab(exploreScreen, exploreView),
            Tab(connectedScreen, connectedView)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation?.setFragmentResultListener(COUNTRY_REQUEST_KEY) { bundle ->

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerDrawable = HeaderDrawable(requireContext())
        headerView = view.findViewById(R.id.header)
        headerView.background = headerDrawable

        exploreView = view.findViewById(R.id.explore)
        exploreView.setOnClickListener { setTab(tabs.first()) }

        connectedView = view.findViewById(R.id.connected)
        connectedView.setOnClickListener { setTab(tabs.last()) }

        countryView = view.findViewById(R.id.country)
        countryView.setOnClickListener {
            navigation?.add(CountryScreen.newInstance(COUNTRY_REQUEST_KEY))
        }

        footerDrawable = FooterDrawable(requireContext())

        containerView = view.findViewById(CONTAINER_ID)

        searchContainerView = view.findViewById(R.id.search_container)
        searchContainerView.background = footerDrawable

        searchView = view.findViewById(R.id.search)
        searchView.setOnClickListener {
            navigation?.add(BrowserSearchScreen.newInstance())
        }

        ViewCompat.setOnApplyWindowInsetsListener(view, ::onApplyWindowInsets)
        setTab(tabs.first())

        collectFlow(mainViewModel.childTopScrolled, headerDrawable::setDivider)
        collectFlow(mainViewModel.childBottomScrolled, footerDrawable::setDivider)
        collectFlow(mainViewModel.countryFlow) { country ->
            countryView.text = country.flagEmoji
        }
    }

    private fun onApplyWindowInsets(view: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = statusInsets.top
            bottomMargin = navInsets.bottom + view.context.getDimensionPixelSize(uikit.R.dimen.barHeight)
        }
        return insets
    }

    private fun setTab(tab: Tab) {
        val isAlreadyFragment = childFragmentManager.findFragmentByTag(tab.name) != null

        val transaction = childFragmentManager.beginTransaction()
        currentScreen?.let { transaction.hide(it) }
        if (isAlreadyFragment) {
            transaction.show(tab.screen)
        } else {
            transaction.add(CONTAINER_ID, tab.screen, tab.name)
        }
        transaction.commitNowAllowingStateLoss()

        for (t in tabs) {
            if (t == tab) {
                t.view.setBackgroundResource(uikit.R.drawable.bg_button_secondary)
            } else {
                t.view.background = null
            }
        }

        currentScreen = tab.screen
    }

    private data class Tab(
        val screen: BaseFragment,
        val view: View
    ) {

        val name: String
            get() = screen::class.java.simpleName
    }

    companion object {

        const val COUNTRY_REQUEST_KEY = "country_request"

        private val CONTAINER_ID = R.id.browser_fragment_container

        fun newInstance() = BrowserMainScreen()
    }

}