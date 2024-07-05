package com.tonapps.tonkeeper.ui.screen.notifications

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.ui.screen.notifications.list.Adapter
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.widget.HeaderView
import uikit.widget.SimpleRecyclerView

class NotificationsScreen: BaseFragment(R.layout.fragment_notifications), BaseFragment.SwipeBack {

    private val notificationsViewModel: NotificationsViewModel by viewModel()
    private val adapter = Adapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectFlow(notificationsViewModel.uiItemsFlow, adapter::submitList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val headerView = view.findViewById<HeaderView>(R.id.header)
        headerView.doOnCloseClick = { finish() }

        val listView = view.findViewById<SimpleRecyclerView>(R.id.list)
        listView.adapter = adapter
    }

    companion object {
        fun newInstance() = NotificationsScreen()
    }
}