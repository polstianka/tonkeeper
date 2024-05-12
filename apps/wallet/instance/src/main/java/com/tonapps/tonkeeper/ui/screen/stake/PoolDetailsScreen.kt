package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.helper.NumberFormatter
import com.tonapps.tonkeeper.ui.screen.stake.model.DetailsArgs
import com.tonapps.tonkeeper.ui.screen.stake.view.PoolDetailView
import com.tonapps.tonkeeper.ui.screen.stake.view.SocialLinkView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.widget.HeaderView

class PoolDetailsScreen : BaseFragment(R.layout.fragment_pool_details), BaseFragment.BottomSheet {

    private val detailsViewModel: PoolDetailsViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var topDetails: ViewGroup
    private lateinit var socialLinks: ViewGroup
    private lateinit var socialLinksTitle: AppCompatTextView
    private lateinit var chooseButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = arguments?.getParcelable<DetailsArgs>(ARGS_KEY) ?: error("Provide args")

        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        topDetails = view.findViewById(R.id.top_details)
        socialLinks = view.findViewById(R.id.social_links)
        socialLinksTitle = view.findViewById(R.id.social_links_title)
        chooseButton = view.findViewById(R.id.choose_button)
        chooseButton.setOnClickListener {
            detailsViewModel.choose(args.address)
            finish()
        }

        headerView.title = args.name

        addTopDetails(args)
        addLinks(args)
    }

    private fun addLinks(args: DetailsArgs) {
        socialLinksTitle.isVisible = args.links.isNotEmpty()
        args.links.forEach {
            socialLinks.addView(SocialLinkView(requireContext()).apply {
                setLink(it)
            })
        }
    }

    private fun addTopDetails(args: DetailsArgs) {
        topDetails.addView(PoolDetailView(requireContext()).apply {
            titleTextView.text = context.getString(Localization.apy)
            maxView.isVisible = args.isApyMax
            valueTextView.text =
                context.getString(Localization.apy_short_percent_placeholder, args.value)
        })
        topDetails.addView(PoolDetailView(requireContext()).apply {
            titleTextView.text = context.getString(Localization.min_deposit)
            maxView.isVisible = false
            valueTextView.text = "${NumberFormatter.format(Coin.toCoins(args.minDeposit))} TON"
        })
    }

    companion object {
        private const val ARGS_KEY = "args"
        fun newInstance(args: DetailsArgs): PoolDetailsScreen {
            return PoolDetailsScreen().apply {
                arguments = bundleOf(ARGS_KEY to args)
            }
        }
    }
}