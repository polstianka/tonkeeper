package com.tonapps.tonkeeper.ui.screen.battery

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import com.tonapps.tonkeeper.ui.base.BaseWalletScreen
import com.tonapps.tonkeeper.ui.screen.battery.refill.BatteryRefillScreen
import com.tonapps.tonkeeper.ui.screen.battery.settings.BatterySettingsScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import kotlinx.coroutines.flow.map
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.commitChildAsSlide
import uikit.widget.HeaderView

class BatteryScreen : BaseWalletScreen(R.layout.fragment_battery), BaseFragment.BottomSheet {

    private val initialPromo: String? by lazy { requireArguments().getString(ARG_PROMO) }

    override val viewModel: BatteryViewModel by viewModel()

    private val backStackListener = FragmentManager.OnBackStackChangedListener {
        updateBackVisibility()
    }

    private lateinit var headerView: HeaderView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = { childFragmentManager.popBackStack() }
        headerView.setBackgroundResource(uikit.R.drawable.bg_page_gradient)

        childFragmentManager.addOnBackStackChangedListener(backStackListener)
        collectFlow(viewModel.routeFlow.map { route ->
            when(route) {
                BatteryRoute.Refill -> BatteryRefillScreen.newInstance(initialPromo)
                BatteryRoute.Settings -> BatterySettingsScreen.newInstance()
            }
        }, ::setChildFragment)

        collectFlow(viewModel.titleFlow) {
            headerView.title = it
        }
    }

    private fun setChildFragment(fragment: BaseFragment) {
        childFragmentManager.commitChildAsSlide {
            replace(R.id.fragment_battery_container, fragment, fragment.toString())
            addToBackStack(fragment.toString())
        }
    }

    private fun updateBackVisibility() {
        val hasBackStack = childFragmentManager.backStackEntryCount > 1
        headerView.setIcon(if (hasBackStack) UIKitIcon.ic_chevron_left_16 else 0)
    }

    override fun onBackPressed(): Boolean {
        if (childFragmentManager.backStackEntryCount > 1) {
            childFragmentManager.popBackStack()
            return false
        }
        return super.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        childFragmentManager.removeOnBackStackChangedListener(backStackListener)
    }

    companion object {
        private const val ARG_PROMO = "promo"

        fun newInstance(promo: String? = null): BatteryScreen {
            val fragment = BatteryScreen()
            fragment.arguments = Bundle().apply {
                putString(ARG_PROMO, promo)
            }

            return fragment
        }
    }
}