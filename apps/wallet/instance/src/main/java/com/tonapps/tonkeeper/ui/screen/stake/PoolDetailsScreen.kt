package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.tonapps.tonkeeperx.R
import kotlinx.parcelize.Parcelize
import uikit.base.BaseFragment
import uikit.widget.HeaderView

class PoolDetailsScreen : BaseFragment(R.layout.fragment_pool_details), BaseFragment.BottomSheet {

    private lateinit var headerView: HeaderView
    private lateinit var topDetails: ViewGroup
    private lateinit var socialLinks: ViewGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        topDetails = view.findViewById(R.id.top_details)
        socialLinks = view.findViewById(R.id.social_links)

        val args = arguments?.getParcelable<DetailsArgs>(ARGS_KEY) ?: error("Provide args")
        headerView.title = args.name

        topDetails.addView(PoolTopDetail(requireContext()).apply {
            titleTextView.text = "APY"
            maxView.isVisible = args.isApyMax
            valueTextView.text = args.value
        })
        topDetails.addView(PoolTopDetail(requireContext()).apply {
            titleTextView.text = "Minimal deposit"
            maxView.isVisible = false
            valueTextView.text = args.minDeposit.toString() + args.currency
        })

        args.links.forEach {
            socialLinks.addView(SocialLinkView(requireContext()).apply {
                setLink(it)
            })
        }
    }

    companion object {
        private const val ARGS_KEY = "args"
        fun newInstance(
            args: DetailsArgs
        ): PoolDetailsScreen {
            return PoolDetailsScreen().apply {
                arguments = bundleOf(ARGS_KEY to args)
            }
        }
    }
}

@Parcelize
data class DetailsArgs(
    val name: String,
    val isApyMax: Boolean,
    val value: String,
    val minDeposit: Float,
    val currency: String,
    val links: List<String>
) : Parcelable