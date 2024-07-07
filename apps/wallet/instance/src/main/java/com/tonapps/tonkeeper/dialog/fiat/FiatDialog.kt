package com.tonapps.tonkeeper.dialog.fiat

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern
import com.tonapps.tonkeeper.dialog.fiat.list.MethodAdapter
import com.tonapps.tonkeeper.fragment.country.CountryScreen
import com.tonapps.tonkeeper.fragment.fiat.web.FiatWebFragment
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class FiatDialog : BaseFragment(R.layout.dialog_fiat), BaseFragment.BottomSheet {

    private val confirmationDialog: ConfirmationDialog by lazy {
        ConfirmationDialog(requireContext())
    }

    private val adapter = MethodAdapter {
        fiatViewModel.openItem(it.body)
    }

    private val fiatViewModel: FiatViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var listView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerView = view.findViewById(R.id.header)
        headerView.setBackgroundColor(Color.TRANSPARENT)
        headerView.doOnCloseClick = { pickCountry() }
        headerView.doOnActionClick = { finish() }

        listView = view.findViewById(R.id.list)
        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(requireContext())

        collectFlow(fiatViewModel.items) {
            showWithData(it.methods)
        }

        collectFlow(fiatViewModel.events) {
            when (it) {
                is Action.ConfirmationDialog -> showConfirmDialog(it.item)
                is Action.OpenUrl -> openUrl(it.url, it.pattern)
            }
        }
    }

    private fun showWithData(items: List<FiatItem>) {
        adapter.submitList(MethodAdapter.buildMethodItems(items)) {
            //fixPeekHeight()
        }
    }

    private fun showConfirmDialog(item: FiatItem) {
        confirmationDialog.show(item) { disableConfirm ->
            openUrl(item.actionButton.url, item.successUrlPattern)
            if (disableConfirm) {
                fiatViewModel.disableShowConfirmation(item)
            }
        }
    }

    private fun openUrl(
        url: String,
        pattern: FiatSuccessUrlPattern?
    ) {
        finish()
        navigation?.add(FiatWebFragment.newInstance(url, pattern))
    }

    private fun pickCountry() {
        finish()
        navigation?.add(CountryScreen.newInstance(FIAT_DIALOG_REQUEST))
    }


    companion object {
        const val FIAT_DIALOG_REQUEST = "fiat_dialog_request"

        fun newInstance(): FiatDialog = FiatDialog()
    }
}