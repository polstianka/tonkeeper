package com.tonapps.tonkeeper.fragment.stake.pool_details

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tonapps.tonkeeper.core.toString
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeper.fragment.stake.pool_details.presentation.LinksChipModel
import com.tonapps.tonkeeper.fragment.stake.root.StakeFragment
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.buttonSecondaryBackgroundColor
import com.tonapps.uikit.color.iconPrimaryColor
import com.tonapps.uikit.color.stateList
import com.tonapps.uikit.color.textPrimaryColor
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class PoolDetailsFragment : BaseFragment(R.layout.fragment_pool_details), BaseFragment.BottomSheet {
    companion object {
        const val REQUEST_KEY_PICK_POOL = "REQUEST_KEY_PICK_POOL "
        fun newInstance(
            service: StakingService,
            pool: StakingPool
        ) = PoolDetailsFragment().apply {
            setArgs(
                PoolDetailsFragmentArgs(service, pool)
            )
        }
    }

    private val viewModel: PoolDetailsViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_pool_details_header)
    private val apyTextView: TextView?
        get() = view?.findViewById(R.id.fragment_pool_details_apy)
    private val chip: View?
        get() = view?.findViewById(R.id.fragment_pool_details_chip)
    private val minimalDepositTextView: TextView?
        get() = view?.findViewById(R.id.fragment_pool_details_minimal_deposit)
    private val chipGroup: ChipGroup?
        get() = view?.findViewById(R.id.fragment_pool_details_chip_group)
    private val button: Button?
        get() = view?.findViewById(R.id.fragment_pool_details_button)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_pool_details_footer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(PoolDetailsFragmentArgs(requireArguments()))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnActionClick = { viewModel.onCloseClicked() }
        header?.doOnCloseClick = { viewModel.onChevronClicked() }

        button?.setThrottleClickListener { viewModel.onButtonClicked() }

        footer?.applyNavBottomPadding(32f.dp)

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.title) { header?.title = it }
        observeFlow(viewModel.apy) { apyTextView?.text = it }
        observeFlow(viewModel.isMaxApyVisible) { chip?.isVisible = it }
        observeFlow(viewModel.minimalDeposit) { minimalDepositTextView?.text = it }
        observeFlow(viewModel.chips) { applyChips(it) }
    }

    private fun applyChips(chips: List<LinksChipModel>) {
        chipGroup?.removeAllViews()
        chips.forEach { model ->
            val chip = createChip(model)
            chipGroup?.addView(chip)
        }
    }

    private fun createChip(model: LinksChipModel): Chip {
        val context = requireContext()
        val chip = Chip(context)
        val horizontalPadding = 12f.dp

        chip.chipIcon = ContextCompat.getDrawable(context, model.iconResId)
        chip.chipIconTint = context.iconPrimaryColor.stateList
        chip.chipIconSize = 16f.dp
        chip.iconStartPadding = horizontalPadding

        chip.text = toString(model.text)
        chip.setTextColor(context.textPrimaryColor)
        chip.setTextAppearance(context, uikit.R.style.TextAppearance_Label2)
        chip.textEndPadding = horizontalPadding

        chip.setThrottleClickListener { viewModel.onChipClicked(model) }
        chip.chipBackgroundColor = context.buttonSecondaryBackgroundColor.stateList
        chip.chipMinHeight = 36f.dp
        return chip
    }

    private fun handleEvent(event: PoolDetailsEvent) {
        when (event) {
            PoolDetailsEvent.FinishFlow -> Log.wtf("###", "finishFlow")
            PoolDetailsEvent.NavigateBack -> finish()
            is PoolDetailsEvent.NavigateToLink -> navigation?.openURL(event.url, external = true)
            is PoolDetailsEvent.PickPool -> event.handle()
        }
    }

    private fun PoolDetailsEvent.PickPool.handle() {
        val result = PoolDetailsFragmentResult(pool)
        navigation?.setFragmentResult(REQUEST_KEY_PICK_POOL, result.toBundle())
        popBackToStakingFragment()
    }

    private fun popBackToStakingFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.commit {
            val fragments = fragmentManager.fragments
            val iterator = fragments.iterator()
            var visitedRoot = false
            val toRemove = mutableListOf<Fragment>()
            while (iterator.hasNext()) {
                val current = iterator.next()
                if (visitedRoot) {
                    if (iterator.hasNext()) {
                        toRemove.add(current)
                    }
                } else {
                    if (current is StakeFragment) {
                        visitedRoot = true
                    }
                }
            }
            toRemove.forEach { remove(it) }
        }
        finish()
    }
}