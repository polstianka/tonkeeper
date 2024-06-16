package com.tonapps.tonkeeper.ui.screen.main

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.extensions.removeAllFragments
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.ui.screen.browser.main.BrowserMainScreen
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.collectibles.CollectiblesScreen
import com.tonapps.tonkeeper.ui.screen.events.EventsScreen
import com.tonapps.tonkeeper.ui.screen.wallet.picker.PickerScreen
import com.tonapps.tonkeeper.ui.screen.root.RootEvent
import com.tonapps.tonkeeper.ui.screen.swap.SwapScreen
import com.tonapps.tonkeeper.ui.screen.wallet.main.WalletScreen
import com.tonapps.uikit.color.constantBlackColor
import com.tonapps.uikit.color.drawable
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.koin.androidx.viewmodel.ext.android.getViewModel
import uikit.base.BaseFragment
import uikit.drawable.BarDrawable
import uikit.extensions.collectFlow
import uikit.extensions.isMaxScrollReached
import uikit.navigation.Navigation.Companion.navigation
import uikit.utils.RecyclerVerticalScrollListener
import uikit.widget.BottomTabsView

class MainScreen: BaseFragment(R.layout.fragment_main) {

    abstract class Child(@LayoutRes layoutId: Int): BaseFragment(layoutId) {

        val mainViewModel: MainViewModel by lazy {
            requireParentFragment().getViewModel()
        }

        private val scrollListener = object : RecyclerVerticalScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, verticalScrollOffset: Int) {
                getHeaderDividerOwner()?.setDivider(verticalScrollOffset > 0)
                mainViewModel.setBottomScrolled(!recyclerView.isMaxScrollReached)
            }
        }

        abstract fun getRecyclerView(): RecyclerView?

        abstract fun getHeaderDividerOwner(): BarDrawable.BarDrawableOwner?

        open fun scrollUp() {
            getRecyclerView()?.scrollToPosition(0)
        }

        override fun onResume() {
            super.onResume()
            attachScrollHandler()
        }

        override fun onPause() {
            super.onPause()
            detachScrollHandler()
        }

        override fun onHiddenChanged(hidden: Boolean) {
            super.onHiddenChanged(hidden)
            if (hidden) {
                detachScrollHandler()
            } else {
                attachScrollHandler()
            }
        }

        private fun attachScrollHandler() {
            getRecyclerView()?.let {
                scrollListener.attach(it)
            }
        }

        private fun detachScrollHandler() {
            scrollListener.detach()
        }
    }

    private val mainViewModel: MainViewModel by viewModel()
    private val rootViewModel: RootViewModel by activityViewModel()

    private var currentFragment: BaseFragment? = null

    private lateinit var fragments: Map<Int, BaseFragment>
    private lateinit var bottomTabsView: BottomTabsView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.removeAllFragments()
        currentFragment = null
        fragments = createMapFragments()

        bottomTabsView = view.findViewById(R.id.bottom_tabs)
        bottomTabsView.doOnClick = { itemId ->
            setFragment(itemId, false)
        }
        bottomTabsView.doOnLongClick = { itemId ->
            if (itemId == R.id.wallet) {
                navigation?.add(PickerScreen.newInstance())
            }
        }
        collectFlow(mainViewModel.childBottomScrolled, bottomTabsView::setDivider)
        collectFlow(rootViewModel.eventFlow.filterIsInstance<RootEvent.OpenTab>().map { mainDeepLinks[it.link] }.filterNotNull(), this::forceSelectTab)
        collectFlow(rootViewModel.eventFlow.filterIsInstance<RootEvent.Swap>()) {
            navigation?.add(SwapScreen.newInstance(it.uri, it.address, it.from, it.to))
        }
        collectFlow(mainViewModel.browserTabEnabled) { enabled ->
            if (enabled) {
                bottomTabsView.showItem(R.id.browser)
            } else {
                bottomTabsView.hideItem(R.id.browser)
                if (currentFragment is BrowserMainScreen) {
                    forceSelectTab(R.id.wallet)
                }
            }
        }

        setFragment(R.id.wallet, false)
    }

    private fun fragmentByItemId(itemId: Int): BaseFragment {
        return fragments[itemId] ?: throw IllegalArgumentException("Unknown itemId: $itemId")
    }

    fun forceSelectTab(itemId: Int) {
        bottomTabsView.setItemChecked(itemId)
        setFragment(itemId, true)
    }

    private fun setFragment(itemId: Int, force: Boolean) {
        val tag = itemId.toString()
        val isAlreadyFragment = childFragmentManager.findFragmentByTag(tag) != null

        val newFragment = fragmentByItemId(itemId)
        if (newFragment == currentFragment) {
            (newFragment as? Child)?.scrollUp()
            return
        }

        val transaction = childFragmentManager.beginTransaction()
        currentFragment?.let { transaction.hide(it) }
        if (isAlreadyFragment) {
            transaction.show(newFragment)
        } else {
            transaction.add(R.id.child_fragment, newFragment, tag)
        }
        transaction.runOnCommit {
            if (force && newFragment is Child) {
                newFragment.scrollUp()
            }
        }
        transaction.commitAllowingStateLoss()
        bottomTabsView.setDivider(false)

        currentFragment = newFragment
    }

    override fun onResume() {
        super.onResume()
        window?.setBackgroundDrawable(requireContext().constantBlackColor.drawable)
    }

    companion object {

        private val mainDeepLinks = mapOf(
            "tonkeeper://wallet" to R.id.wallet,
            "tonkeeper://activity" to R.id.activity,
            "tonkeeper://browser" to R.id.browser,
            "tonkeeper://collectibles" to R.id.collectibles
        )

        private fun createMapFragments(): Map<Int, BaseFragment> {
            return mapOf(
                R.id.wallet to WalletScreen.newInstance(),
                R.id.activity to EventsScreen.newInstance(),
                R.id.collectibles to CollectiblesScreen.newInstance(),
                R.id.browser to BrowserMainScreen.newInstance()
            )
        }

        fun isSupportedDeepLink(uri: String): Boolean {
            return mainDeepLinks.containsKey(uri)
        }

        fun newInstance() = MainScreen()
    }

}