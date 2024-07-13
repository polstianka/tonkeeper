package com.tonapps.tonkeeper.ui.screen.swap.choose

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import com.tonapps.tonkeeper.ui.screen.swap.ListBackgroundDecoration
import com.tonapps.tonkeeper.ui.screen.swap.SwapChooseView
import com.tonapps.tonkeeper.ui.screen.swap.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.swap.pager.SwapScreenAdapter
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.dp
import uikit.widget.SimpleRecyclerView

class SwapChooseScreen :
    PagerScreen<SwapChooseScreenState, SwapChooseScreenEffect, SwapChooseScreenFeature>(R.layout.fragment_swap_choose),
    BaseFragment.CustomBackground {

    companion object {
        fun newInstance() = SwapChooseScreen()
    }

    override val feature: SwapChooseScreenFeature by viewModel()


    private lateinit var recyclerView: SimpleRecyclerView
    private lateinit var empty: View
    private lateinit var button: FrameLayout
    private lateinit var search: AppCompatEditText
    private lateinit var suggestedContainer: View
    private lateinit var close: View
    private lateinit var suggested: LinearLayoutCompat
    private var canAnimateKeyboard = false
    private var skipTextChanged = false
    private val handler = Handler()

    private val adapter: SwapAssetAdapter by lazy {
        SwapAssetAdapter {
            feature.selectAsset(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suggested = view.findViewById(R.id.suggested)
        empty = view.findViewById(R.id.empty)
        close = view.findViewById(R.id.close)
        close.setOnClickListener {
            swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_AMOUNT)
        }

        suggestedContainer = view.findViewById(R.id.suggested_container)
        button = view.findViewById(R.id.button)
        button.setOnClickListener {
            swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_AMOUNT)
        }

        search = view.findViewById(R.id.search_edit)
        search.doAfterTextChanged {
            if (!skipTextChanged) {
                feature.setSearchQuery(it.toString())
            }
        }

        recyclerView = view.findViewById(R.id.list)
        recyclerView.adapter = adapter
    }

    override fun newUiState(state: SwapChooseScreenState) {
        android.util.Log.d("wwttff", "new ui state")
        adapter.submitList(state.filteredAssets)
        empty.isVisible = state.filteredAssets.isEmpty()

        suggestedContainer.isVisible = state.suggestedAssets.isNotEmpty()
        if (state.suggestedAssets.isNotEmpty() && suggested.childCount == 0) {
            state.suggestedAssets.forEachIndexed { index, asset ->
                val view = SwapChooseView(
                    requireContext(),
                    suggest = true
                ).apply { setData(SwapChooseView.SwapItem.Asset(asset.symbol, asset.imageURL)) }
                view.setOnClickListener {
                    feature.selectAsset(asset)
                }
                view.layoutParams = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = if (index == 0) 16.dp else 8.dp
                }
                suggested.addView(view)
            }
        }

        if (state.chosen != null) {
            canAnimateKeyboard = false
            if (swapFeature.swap.value?.currentFrom == true) {
                swapFeature.setFrom(state.chosen)
                if (state.chosen.symbol == swapFeature.swap.value?.to?.symbol) {
                    swapFeature.clearTo()
                }
            } else {
                swapFeature.setTo(state.chosen)
                if (state.chosen.symbol == swapFeature.swap.value?.from?.symbol) {
                    swapFeature.clearFrom()
                }
            }
            swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_AMOUNT)
        }
    }

    private var goingUp = true
    private val kbdHelpRunnable = Runnable { goingUp = true }
    fun onKeyboardAppear(progress: Float, isShowing: Boolean) {
        if (!canAnimateKeyboard) return
        // disappear all but recycler animated when search mode activated
        if (progress == 1f && isShowing) goingUp = false
        if (progress == 0f && isShowing) {
            handler.removeCallbacks(kbdHelpRunnable)
            handler.postDelayed(kbdHelpRunnable, 150)
        }
        if (suggestedContainer.isVisible) {
            suggestedContainer?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height = (134.dp * (1f - progress)).toInt().coerceAtLeast(1) // TODO fix magic
            }
            suggestedContainer.alpha = 1f - progress
        }

        // bottom button needs to disappear immediately, but isShowing not working well (at progress = 0.0f isShowing always true)
        // so here is goingUp tricky workaround..
        if (goingUp && isShowing) {
            button?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height = 1
            }
            button.alpha = 0f
        } else {
            button?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height = (56.dp * (1f - 2f * progress)).toInt().coerceAtLeast(1)
            }
            button.alpha = (1f - 2f * progress).coerceAtLeast(0f)
        }
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            handler.postDelayed({
                canAnimateKeyboard = true
            }, 500)
            skipTextChanged = true
            search.setText("")
            goingUp = true
            recyclerView.scrollToPosition(0)
            if (suggestedContainer.isVisible) {
                suggestedContainer?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    height = 134.dp
                }
                suggestedContainer.alpha = 1f
            }
            button?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height = 56.dp
            }
            button.alpha = 1f
            skipTextChanged = false
            swapFeature.swap.value?.let {
                feature.update(it)
            }
        } else {
            canAnimateKeyboard = false
        }
    }
}