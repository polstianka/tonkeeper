package com.tonapps.tonkeeper.ui.screen.buysell

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Button
import androidx.core.os.bundleOf
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyScreen
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyViewModel
import com.tonapps.tonkeeperx.R
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.ActionCellSimpleView
import uikit.widget.HeaderView
import uikit.widget.SimpleRecyclerView

class FiatOperatorScreen : BaseFragment(R.layout.fragment_fiat_operator), BaseFragment.BottomSheet {

    private val operatorViewModel: FiatOperatorViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var methodList: SimpleRecyclerView
    private lateinit var currencyView: ActionCellSimpleView
    private lateinit var continueButton: Button

    private val adapter = MethodAdapter {
        operatorViewModel.selectMethod(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = arguments?.getParcelable<FiatOperatorArgs>(ARGS_KEY) ?: error("provide args")
        operatorViewModel.init(args.operationType)

        headerView = view.findViewById(R.id.header)
        headerView.setDefault()
        headerView.setSubtitle(args.name)
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = { finish() }

        view.findViewById<View>(R.id.root)
            .applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))

        currencyView = view.findViewById(R.id.selected_fiat_currency)
        currencyView.actionTint = com.tonapps.uikit.color.R.attr.iconTertiaryColor
        currencyView.setOnClickListener {
            navigation?.setFragmentResultListener(CurrencyScreen.CURRENCY_DIALOG_REQUEST) {
                operatorViewModel.reloadMethods()
            }
            navigation?.add(CurrencyScreen.newInstance(CurrencyScreen.CURRENCY_DIALOG_REQUEST))
        }

        methodList = view.findViewById(R.id.methods_list)
        methodList.adapter = adapter

        continueButton = view.findViewById(R.id.continue_action)
        continueButton.setOnClickListener {
            val selected = operatorViewModel.getSelected()
            navigation?.add(
                FiatConfirmScreen.newInstance(
                    FiatConfirmArgs(
                        id = selected.id,
                        name = selected.name,
                        subtitle = selected.subtitle,
                        iconUrl = selected.iconUrl,
                        amount = args.amount,
                        url = selected.url,
                        pattern = selected.pattern
                    )
                )
            )
        }

        collectFlow(operatorViewModel.uiState) {
            currencyView.title = it.currency
            currencyView.subtitle =
                getString(CurrencyViewModel.getNameResIdForCurrency(it.currency))
            adapter.submitList(it.methods)
        }
    }

    companion object {
        private const val ARGS_KEY = "args"
        fun newInstance(args: FiatOperatorArgs) = FiatOperatorScreen().apply {
            arguments = bundleOf(ARGS_KEY to args)
        }
    }
}

@Parcelize
data class FiatOperatorArgs(
    val type: Int,
    val name: String,
    val operationType: FiatOperation,
    val amount: Float,
) : Parcelable