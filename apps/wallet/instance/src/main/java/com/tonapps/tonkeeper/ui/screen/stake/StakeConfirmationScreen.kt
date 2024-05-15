package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import com.tonapps.tonkeeper.ui.component.keyvalue.KeyValueModel
import com.tonapps.tonkeeper.ui.component.keyvalue.KeyValueRowAdapter
import com.tonapps.tonkeeperx.R
import kotlinx.parcelize.Parcelize
import uikit.base.BaseFragment
import uikit.widget.HeaderView
import uikit.widget.SimpleRecyclerView
import uikit.widget.SlideActionView

class StakeConfirmationScreen :
    BaseFragment(R.layout.fragment_stake_confirmation),
    BaseFragment.BottomSheet {

    private lateinit var headerView: HeaderView
    private lateinit var amountTon: AppCompatTextView
    private lateinit var amountCurrency: AppCompatTextView
    private lateinit var detailsRecycler: SimpleRecyclerView
    private lateinit var slideActionView: SlideActionView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = { finish() }

        amountTon = view.findViewById(R.id.amount_ton)
        amountCurrency = view.findViewById(R.id.amount_currency)

        amountTon.text = "1,000.01 TON"
        amountCurrency.text = "\$ 6,010.01"

        slideActionView = view.findViewById(R.id.confirm_button)
        slideActionView.text = "Slide to confirm"

        detailsRecycler = view.findViewById(R.id.details_recycler_view)

        val adapter = KeyValueRowAdapter()
        detailsRecycler.adapter = adapter

        val args = arguments?.getParcelable<ConfirmationArgs>(ARGS_KEY) ?: error("Provide args")

        adapter.submitList(args.details)
    }

    companion object {
        private const val ARGS_KEY = "args"
        fun newInstance(args: ConfirmationArgs): StakeConfirmationScreen {
            return StakeConfirmationScreen().apply {
                arguments = bundleOf(ARGS_KEY to args)
            }
        }
    }
}

@Parcelize
data class ConfirmationArgs(
    val details: List<KeyValueModel>
) : Parcelable