package com.tonapps.tonkeeper.ui.screen.swap

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.HapticHelper
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.topScrolled
import uikit.widget.ModalHeader
import uikit.widget.RowLayout
import uikit.widget.SearchInput
import uikit.widget.SimpleRecyclerView

class AllAssetsPickerScreen : BaseFragment(R.layout.fragment_all_assets_picker),
    BaseFragment.Modal {

    private val pickerViewModel: AllAssetsPickerViewModel by viewModel()
    private val adapter = Adapter { item ->
        pickerViewModel.setAsset(item)
        finish()
    }

    private lateinit var listView: RecyclerView
    private lateinit var skeletonView: View
    private lateinit var searchInput: SearchInput
    private lateinit var suggestedRow: RowLayout
    private lateinit var suggestedTextView: TextView
    private lateinit var otherTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickerViewModel.init(arguments?.getBoolean("IS_SEND_KEY") ?: true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headerView = view.findViewById<ModalHeader>(R.id.header)
        headerView.onCloseClick = ::finish

        skeletonView = view.findViewById(R.id.skeleton)
        searchInput = view.findViewById(R.id.search)

        suggestedTextView = view.findViewById(R.id.suggested)
        suggestedRow = view.findViewById(R.id.suggested_layout)
        otherTextView = view.findViewById(R.id.other)

        searchInput.doOnTextChanged = {
            pickerViewModel.search((it ?: "").toString())
            listView.smoothScrollToPosition(0)
        }

        listView = view.findViewById<SimpleRecyclerView>(R.id.list)
        listView.adapter = adapter
        collectFlow(listView.topScrolled, headerView::setDivider)

        collectFlow(pickerViewModel.assets, ::setItems)
        collectFlow(pickerViewModel.other) {
            suggestedRow.isVisible = it.isNotEmpty()
            suggestedTextView.isVisible = it.isNotEmpty()
            otherTextView.isVisible = it.isNotEmpty()
            it.forEach { model ->
                suggestedRow.addView(SmallTokenView(requireContext()).apply {
                    setAsset(model)
                    setOnAssetClickListener {
                        pickerViewModel.setAsset(it)
                        finish()
                    }
                })
                suggestedRow.addView(Space(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(4.dp, 4.dp)
                })
            }
        }
    }

    private fun setItems(items: List<AssetModel>) {
        adapter.submitList(items) {
            skeletonView.visibility = View.GONE
            listView.visibility = View.VISIBLE
            HapticHelper.selection(requireContext())
        }
    }

    companion object {
        fun newInstance(isSend: Boolean) = AllAssetsPickerScreen().apply {
            arguments = bundleOf("IS_SEND_KEY" to isSend)
        }
    }
}