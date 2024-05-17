package com.tonapps.tonkeeper.fragment.swap.pick_asset

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.fragment.swap.pick_asset.rv.TokenAdapter
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import uikit.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.applyNavBottomPadding
import uikit.widget.ModalHeader

class PickAssetFragment : BaseFragment(R.layout.fragment_pick_asset), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(type: PickAssetType) = PickAssetFragment().apply {
            setArgs(
                PickAssetArgs(type)
            )
        }
    }

    private val viewModel: PickAssetViewModel by viewModel()
    private val header: ModalHeader?
        get() = view?.findViewById(R.id.fragment_pick_asset_header)
    private val recyclerView: RecyclerView?
        get() = view?.findViewById(R.id.fragment_pick_asset_rv)
    private val adapter = TokenAdapter { viewModel.onItemClicked(it) }
    private val recyclerViewContainer: View?
        get() = view?.findViewById(R.id.fragment_pick_asset_rv_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                PickAssetArgs(requireArguments())
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.onCloseClick = { viewModel.onCloseClicked() }

        recyclerView?.adapter = adapter

        recyclerViewContainer?.applyNavBottomPadding()

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.items) { adapter.submitList(it) }
    }

    private fun handleEvent(event: PickAssetEvent) {
        when (event) {
            PickAssetEvent.NavigateBack -> finish()
        }
    }
}