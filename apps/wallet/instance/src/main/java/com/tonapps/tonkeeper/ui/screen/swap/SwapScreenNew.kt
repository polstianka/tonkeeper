package com.tonapps.tonkeeper.ui.screen.swap

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.swap.amount.SwapAmountScreen
import com.tonapps.tonkeeper.ui.screen.swap.choose.SwapChooseScreen
import com.tonapps.tonkeeper.ui.screen.swap.confirm.SwapConfirmScreen
import com.tonapps.tonkeeper.ui.screen.swap.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.swap.pager.SwapScreenAdapter
import com.tonapps.tonkeeper.ui.screen.swap.settings.SwapSettingsScreen
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseArgs
import uikit.base.BaseFragment
import uikit.extensions.doKeyboardAnimation
import uikit.extensions.hideKeyboard
import uikit.mvi.UiScreen
import uikit.navigation.Navigation.Companion.navigation


class SwapScreenNew : UiScreen<SwapScreenState, SwapScreenEffect, SwapScreenFeature>(R.layout.fragment_swap_new), BaseFragment.BottomSheet {

    companion object {

        fun newInstance(toToken: String? = null): SwapScreenNew {
            val fragment = SwapScreenNew()
            fragment.arguments = Args(toToken).toBundle()
            return fragment
        }
    }

    private val args: Args by lazy { Args(requireArguments()) }
    override val feature: SwapScreenFeature by viewModels()
    private val rootViewModel: RootViewModel by activityViewModel()
    private lateinit var pageAdapter: SwapScreenAdapter
    private var fromPage = 0

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            feature.setCurrentPage(position)
            for (i in 0 until pageAdapter.itemCount) {
                val fragment = pageAdapter.findFragmentByPosition(i) as? PagerScreen<*, *, *>
                fragment?.visible = i == position
                fragment?.view?.alpha = if (i == position || i == fromPage) 1f else 0f
            }
        }
    }

    private lateinit var pagerView: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageAdapter = SwapScreenAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pagerView = view.findViewById(R.id.pager)
        pagerView.offscreenPageLimit = 4
        pagerView.orientation = ViewPager2.ORIENTATION_VERTICAL
        pagerView.isUserInputEnabled = false
        pagerView.setPageTransformer { page, position ->
            val realContentView = ((page as ViewGroup).getChildAt(0))
            val realPos = when (realContentView.tag) {
                is SwapAmountScreen -> SwapScreenAdapter.POSITION_AMOUNT
                is SwapChooseScreen -> SwapScreenAdapter.POSITION_CHOOSE
                is SwapSettingsScreen -> SwapScreenAdapter.POSITION_SETTINGS
                is SwapConfirmScreen -> SwapScreenAdapter.POSITION_CONFIRM
                else -> 0
            }
            val factor = position - realPos
            if (realPos == SwapScreenAdapter.POSITION_AMOUNT) {
                page.translationY = -page.height * factor
                page.alpha = (factor + 1).coerceIn(0.5f, 1f)
            }
        }
        pagerView.adapter = pageAdapter
        pagerView.registerOnPageChangeCallback(pageChangeCallback)
        view.doKeyboardAnimation { offset, progress, isShowing ->
            if (pageAdapter.swapChooseScreen?.isVisibleForUser() == true)
                pageAdapter.swapChooseScreen?.onKeyboardAppear(progress, isShowing)
        }
        feature.setInitialTo(args.toToken)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pagerView.unregisterOnPageChangeCallback(pageChangeCallback)
    }

    override fun onEndShowingAnimation() {
        super.onEndShowingAnimation()
        feature.readyView()
    }

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    override fun newUiState(state: SwapScreenState) {
        fromPage = pagerView.currentItem
        pagerView.currentItem = state.currentPage
    }

    override fun newUiEffect(effect: SwapScreenEffect) {
        super.newUiEffect(effect)

        if (effect is SwapScreenEffect.Finish) {
            finish()
        } else if (effect is SwapScreenEffect.FinishAndGoHistory) {
            rootViewModel.goHistory()
            this.finish()
        }
    }
}

data class Args(
    val toToken: String?
): BaseArgs() {

    private companion object {
        private const val TO_TOKEN = "to"
    }

    constructor(bundle: Bundle) : this(
        toToken = bundle.getString(TO_TOKEN)
    )

    override fun toBundle(): Bundle = Bundle().apply {
        toToken?.let {
            putString(TO_TOKEN, it)
        }
    }
}