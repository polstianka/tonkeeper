package com.tonapps.tonkeeper.ui.screen.init.step

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.blockchain.ton.extensions.toRawAddress
import com.tonapps.tonkeeper.ui.screen.init.InitRoute
import com.tonapps.tonkeeper.ui.screen.init.InitViewModel
import com.tonapps.tonkeeper.ui.screen.init.list.AccountItem
import com.tonapps.tonkeeper.ui.screen.init.list.Adapter
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize

class SelectScreen : BaseFragment(R.layout.fragment_init_select) {

    private val initViewModel: InitViewModel by viewModel(ownerProducer = { requireParentFragment() })

    private val adapter = Adapter { account, checked ->
        initViewModel.toggleAccountSelection(account.address.toRawAddress(), checked)
    }

    private lateinit var scrollView: NestedScrollView
    private lateinit var containerView: View
    private lateinit var listView: RecyclerView
    private lateinit var button: Button
    private lateinit var buttonContainerView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scrollView = view.findViewById(R.id.scroll)
        containerView = view.findViewById(R.id.container)

        listView = view.findViewById(R.id.accounts)
        listView.adapter = adapter
        listView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetLarge))

        button = view.findViewById(R.id.button)
        button.setOnClickListener {
            initViewModel.nextStep(requireContext(), InitRoute.SelectAccount)
        }

        buttonContainerView = view.findViewById(R.id.button_container)
        buttonContainerView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetLarge))

        collectFlow(initViewModel.uiTopOffset) {
            containerView.updatePadding(top = it)
        }

        collectFlow(initViewModel.accountsFlow, ::setItems)
    }

    private fun setItems(items: List<AccountItem>) {
        adapter.submitList(items)
        button.isEnabled = items.count { it.selected } > 0
    }

    companion object {
        fun newInstance() = SelectScreen()
    }
}