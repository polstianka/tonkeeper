package com.tonapps.tonkeeper.fragment.swap.assets

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.tonkeeper.fragment.swap.SwapViewModel
import com.tonapps.tonkeeper.fragment.swap.model.SwapTarget
import com.tonapps.tonkeeper.ui.component.swap.SearchInputView
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.setBottomInset
import uikit.widget.HeaderView

class AssetsScreen: BaseFragment(R.layout.fragment_assets), BaseFragment.BottomSheet {

    private val swapViewMode: SwapViewModel by activityViewModel()

    private val headerView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById<HeaderView>(R.id.header)
    }

    private val searchView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById<SearchInputView>(R.id.search_input)
    }

    private val target by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getParcelableCompat<SwapTarget>(KEY_TARGET)!!
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView.doOnActionClick = { finish() }

        val listView = view.findViewById<RecyclerView>(R.id.listView)

        val adapter = AssetsListAdapter(
            onClickListener = { symbol: String ->
                swapViewMode.onAssetSearch("")
                swapViewMode.onSelectAsset(target, symbol)
                finish()
            }
        )
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(view.context)
        listView.adapter = adapter

        searchView.doOnTextChanged = {
            swapViewMode.onAssetSearch(it.toString())
        }

        val cancelBtn = view.findViewById<View>(R.id.cancel)
        cancelBtn.setOnClickListener {
            finish()
        }

        requireView().findViewById<ViewGroup>(R.id.container).setBottomInset()

        listView.isNestedScrollingEnabled = false

        collectFlow(swapViewMode.getItemsFlow(target)) {
            adapter.submitList(it)
        }

    }

    fun clear() {
        swapViewMode.onAssetSearch("")
        searchView.cancel()
    }

    override fun finish() {
        clear()
        super.finish()
    }

    override fun isDraggable(): Boolean {
        return false
    }

    companion object {
        private const val KEY_TARGET = "key_target"
        fun newInstance(target: SwapTarget): AssetsScreen {
            return AssetsScreen().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_TARGET, target)
                }
            }
        }
    }
}