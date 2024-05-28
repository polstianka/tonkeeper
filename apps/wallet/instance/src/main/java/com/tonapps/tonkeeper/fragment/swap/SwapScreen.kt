package com.tonapps.tonkeeper.fragment.swap

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.tonapps.tonkeeper.fragment.send.pager.PagerScreen
import com.tonapps.tonkeeper.fragment.swap.pager.SwapScreenAdapter
import com.tonapps.tonkeeper.fragment.swap.settings.SettingsScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.hideKeyboard
import uikit.extensions.setPaddingHorizontal
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView


class SwapScreen : BaseFragment(R.layout.fragment_swap_new), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(): SwapScreen {
            return SwapScreen()
        }
    }

    private val feature: SwapScreenFeature by viewModel()

    private lateinit var pageAdapter: SwapScreenAdapter

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            feature.setCurrentPage(position)
            for (i in 0 until pageAdapter.itemCount) {
                val fragment = pageAdapter.findFragmentByPosition(i) as? PagerScreen<*, *, *>
                fragment?.visible = i == position
            }
        }
    }

    private lateinit var headerView: HeaderView
    private lateinit var pagerView: ViewPager2
    private lateinit var shimmerView: View
    private lateinit var messageView: View
    private lateinit var messageTitleView: AppCompatTextView
    private lateinit var messageCloseView: AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageAdapter = SwapScreenAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feature.uiState.collect { state -> newUiState(state) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feature.uiEffect.collect { state -> newUiEffect(state) }
            }
        }

        headerView = view.findViewById(R.id.title)
        headerView.contentMatchParent()
        headerView.doOnCloseClick = {
            feature.onActionClick()
        }
        headerView.doOnActionClick = { finish() }

        shimmerView = view.findViewById(R.id.shimmer)

        pagerView = view.findViewById(R.id.pager)
        pagerView.offscreenPageLimit = 3
        pagerView.isUserInputEnabled = false
        pagerView.adapter = pageAdapter
        pagerView.registerOnPageChangeCallback(pageChangeCallback)

        messageView = view.findViewById(R.id.message)
        messageTitleView = view.findViewById(R.id.message_content)
        messageCloseView = view.findViewById(R.id.message_close)
        messageCloseView.setOnClickListener {
            closeMessage()
        }

        feature.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pagerView.unregisterOnPageChangeCallback(pageChangeCallback)
    }

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    private fun newUiState(state: SwapScreenState) {
        when (state) {
            is SwapScreenState.Loading -> {
                shimmerView.visibility = View.VISIBLE
                pagerView.visibility = View.GONE
            }

            is SwapScreenState.Content -> {
                shimmerView.visibility = View.GONE
                pagerView.visibility = View.VISIBLE
                pagerView.currentItem = state.currentPage

                if (state.currentPage > 0) {
                    headerView.title = getString(Localization.confirm_swap)
                    headerView.closeView.visibility = View.GONE
                    val params = LinearLayoutCompat.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.START
                    }
                    headerView.titleView.layoutParams = params
                    headerView.findViewById<View>(uikit.R.id.header_text).setPaddingHorizontal(0)
                }
            }

            is SwapScreenState.Error -> {
                // TODO find error ui / figma?
            }
        }
    }

    private fun newUiEffect(effect: SwapScreenEffect) {
        when (effect) {
            is SwapScreenEffect.ShowMessage -> showMessage(effect.text)
            is SwapScreenEffect.CloseMessage -> closeMessage()
            is SwapScreenEffect.Finish -> finish()
            is SwapScreenEffect.Back -> feature.prevPage()
            is SwapScreenEffect.OpenSettings -> navigation?.add(SettingsScreen.newInstance())
        }
    }

    private fun showMessage(message: String) {
        messageTitleView.text = message
        messageView.visibility = View.VISIBLE
    }

    private fun closeMessage() {
        messageView.visibility = View.GONE
    }
}