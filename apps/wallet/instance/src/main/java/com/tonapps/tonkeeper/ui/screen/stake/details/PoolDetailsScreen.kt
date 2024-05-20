package com.tonapps.tonkeeper.ui.screen.stake.details

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.helper.NumberFormatter
import com.tonapps.tonkeeper.ui.screen.stake.model.DetailsArgs
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsMainViewModel
import com.tonapps.tonkeeper.ui.screen.stake.view.PoolDetailView
import com.tonapps.tonkeeper.ui.screen.stake.view.SocialLinkView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.collectFlow

class PoolDetailsScreen : Fragment(R.layout.fragment_pool_details) {

    private val detailsViewModel: PoolDetailsViewModel by viewModel()
    private val optionsMainViewModel: StakeOptionsMainViewModel by activityViewModel()

    private lateinit var topDetails: ViewGroup
    private lateinit var socialLinks: ViewGroup
    private lateinit var socialLinksTitle: AppCompatTextView
    private lateinit var chooseButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        topDetails = view.findViewById(R.id.top_details)
        socialLinks = view.findViewById(R.id.social_links)
        socialLinksTitle = view.findViewById(R.id.social_links_title)
        chooseButton = view.findViewById(R.id.choose_button)

        collectFlow(optionsMainViewModel.detailsArgs) { args ->
            if (args != null) {
                chooseButton.setOnClickListener {
                    detailsViewModel.choose(args.address)
                    optionsMainViewModel.finish()
                }
                addTopDetails(args)
                addLinks(args)
            }
        }
    }

    private fun addLinks(args: DetailsArgs) {
        socialLinksTitle.isVisible = args.links.isNotEmpty()
        socialLinks.removeAllViews()
        args.links.forEach {
            socialLinks.addView(SocialLinkView(requireContext()).apply {
                setLink(it)
            })
        }
    }

    private fun addTopDetails(args: DetailsArgs) {
        topDetails.removeAllViews()
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

        fun newInstance() = PoolDetailsScreen()
    }
}