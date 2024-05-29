package com.tonapps.tonkeeper.ui.screen.swap.assets

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tonapps.tonkeeper.core.measuredHeightWithVerticalMargins
import com.tonapps.tonkeeper.core.updateInsetPaddingBottom
import com.tonapps.tonkeeper.ui.screen.swap.assets.list.Adapter
import com.tonapps.tonkeeper.ui.screen.swap.assets.list.Decoration
import com.tonapps.tonkeeper.ui.screen.swap.data.AssetEntity
import com.tonapps.tonkeeper.ui.screen.swap.data.RemoteAssets
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapTarget
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.LinearLayoutManager
import com.tonapps.wallet.api.entity.TokenEntity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import uikit.base.BaseFragment
import uikit.extensions.doKeyboardAnimation
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.hideKeyboard
import uikit.widget.BottomSheetLayout
import uikit.widget.HeaderView
import uikit.widget.SimpleRecyclerView

class AssetPickerScreen : BaseFragment(R.layout.fragment_swap_assets), BaseFragment.BottomSheet {
    companion object {
        const val RESULT_KEY = "picked_asset"

        fun newInstance(requestKey: String,
                        remoteAssets: RemoteAssets,
                        target: SwapTarget,
                        limitToMarkets: Set<String>? = null,
                        selectedToken: TokenEntity? = null): AssetPickerScreen {
            val screen = AssetPickerScreen()
            screen.setArgs(AssetPickerArgs(
                requestKey, remoteAssets, target,
                selectedToken, limitToMarkets
            ), ignoreErrors = true)
            return screen
        }
    }

    private val args: AssetPickerArgs by lazy {
        lazyArgs as? AssetPickerArgs ?: AssetPickerArgs(requireArguments())
    }

    private lateinit var headerView: HeaderView
    private lateinit var labelAction: ViewGroup
    private lateinit var bottomOverlay: View
    private lateinit var contentView: SimpleRecyclerView
    private lateinit var adapter: Adapter
    private lateinit var searchInput: AppCompatEditText

    private val assetPickerViewModel: AssetPickerViewModel by viewModel { parametersOf(args)  }
    private var keyboardVisible: Boolean = false

    private var behavior: BottomSheetBehavior<FrameLayout>? = null
    override fun onPrepareToShow(parent: BottomSheetLayout, behavior: BottomSheetBehavior<FrameLayout>) {
        this.behavior = behavior
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.contentMatchParent()
        headerView.doOnActionClick = {
            hideKeyboardAndRun(::finish)
        }
        headerView.alignTitleToStart()

        contentView = view.findViewById(R.id.content)
        labelAction = view.findViewById(R.id.label_action)
        bottomOverlay = view.findViewById(R.id.bottom_overlay)
        view.doKeyboardAnimation { offset, progress, isShowing, navigationBarSize ->
            labelAction.translationY = -navigationBarSize.toFloat()
            keyboardVisible = isShowing || progress == 1.0f
            contentView.updateInsetPaddingBottom(offset, progress, isShowing, navigationBarSize, labelAction.measuredHeightWithVerticalMargins)
            if (bottomOverlay.layoutParams.height != navigationBarSize) {
                bottomOverlay.updateLayoutParams<ViewGroup.LayoutParams> {
                    height = navigationBarSize
                }
            }
        }
        val labelButton: Button = labelAction.findViewById(R.id.label_button)
        labelButton.setOnClickListener {
            hideKeyboardAndRun(::finish)
        }

        searchInput = view.findViewById(R.id.search_input)
        searchInput.doAfterTextChanged { assetPickerViewModel.query(it.toString()) }

        val context = requireContext()
        adapter = Adapter(this)
        contentView.adapter = adapter
        contentView.addItemDecoration(Decoration(context))
        val searchHeight = context.getDimensionPixelSize(uikit.R.dimen.searchHeight)
        val offsetMedium = context.getDimensionPixelSize(uikit.R.dimen.offsetMedium)
        val offsetLarge = context.getDimensionPixelSize(uikit.R.dimen.offsetLarge)
        val paddingTop = searchHeight + offsetMedium * 2
        val paddingBottom = searchHeight + offsetMedium + offsetLarge
        contentView.setPadding(0, paddingTop, 0, paddingBottom)
        contentView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                val isScrolled = firstPosition > 0 || (layoutManager.findViewByPosition(0)?.let {
                    layoutManager.getDecoratedTop(it) < 0
                } ?: false)
                behavior?.isHideable = !isScrolled
            }
        })
        contentView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0 && keyboardVisible) {
                    getCurrentFocus()?.hideKeyboard()
                }
            }
        })

        assetPickerViewModel.viewModelScope.launch {
            assetPickerViewModel.uiItemsFlow.collect {
                adapter.submitList(it) {
                    if (first) {
                        first = false
                    } else {
                        (contentView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
                    }
                }
            }
        }
    }

    private var first: Boolean = true

    fun selectToken(entity: AssetEntity) {
        val bundle = Bundle().apply {
            putParcelable(RESULT_KEY, entity)
        }
        setFragmentResult(args.requestKey, bundle)
        hideKeyboardAndRun(::finish)
    }

    override fun onDragging() {
        getCurrentFocus()?.hideKeyboard()
    }
}