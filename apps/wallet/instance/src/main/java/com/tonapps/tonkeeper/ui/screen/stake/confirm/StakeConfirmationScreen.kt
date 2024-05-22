package com.tonapps.tonkeeper.ui.screen.stake.confirm

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.ui.component.keyvalue.KeyValueModel
import com.tonapps.tonkeeper.ui.component.keyvalue.KeyValueRowAdapter
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.stake.StakeMainViewModel
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.ton.contract.wallet.WalletTransfer
import uikit.extensions.circle
import uikit.extensions.collectFlow
import uikit.widget.SimpleRecyclerView
import uikit.widget.SlideActionView

class StakeConfirmationScreen : Fragment(R.layout.fragment_stake_confirmation) {

    private val stakeMainViewModel: StakeMainViewModel by activityViewModel()
    private val rootViewModel: RootViewModel by activityViewModel()
    private val confirmationViewModel: StakeConfirmationViewModel by viewModel()

    private lateinit var amountTon: AppCompatTextView
    private lateinit var amountCurrency: AppCompatTextView
    private lateinit var detailsRecycler: SimpleRecyclerView
    private lateinit var slideActionView: SlideActionView
    private lateinit var poolImage: AppCompatImageView
    private lateinit var title: AppCompatTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        amountTon = view.findViewById(R.id.amount_ton)
        amountCurrency = view.findViewById(R.id.amount_currency)
        slideActionView = view.findViewById(R.id.confirm_button)
        detailsRecycler = view.findViewById(R.id.details_recycler_view)
        poolImage = view.findViewById(R.id.pool_image)
        title = view.findViewById(R.id.confirmation_title)

        collectFlow(stakeMainViewModel.confirmationArgs) { args ->
            if (args != null) {
                amountTon.text = args.amount
                amountCurrency.text = args.amountInCurrency
                poolImage.setImageResource(args.imageRes)
                poolImage.circle()

                title.text = if (args.unstake) getString(Localization.unstake)
                else getString(Localization.stake)

                slideActionView.text = getString(Localization.slide_to_confirm)

                val adapter = KeyValueRowAdapter()
                detailsRecycler.adapter = adapter

                adapter.submitList(args.details)

                slideActionView.doOnDone = {
                    lifecycleScope.launch {
                        val sign = confirmationViewModel.getSignRequestEntity(args.walletTransfer)
                        rootViewModel.requestSign(requireContext(), sign)
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance() = StakeConfirmationScreen()
    }
}

data class ConfirmationArgs(
    val amount: String,
    val amountInCurrency: String,
    @DrawableRes val imageRes: Int,
    val details: List<KeyValueModel>,
    val walletTransfer: WalletTransfer,
    val unstake: Boolean
)