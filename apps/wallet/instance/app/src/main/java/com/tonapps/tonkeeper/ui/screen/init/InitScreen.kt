package com.tonapps.tonkeeper.ui.screen.init

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.tonapps.ledger.ton.LedgerConnectData
import com.tonapps.tonkeeper.ui.base.BaseWalletScreen
import com.tonapps.tonkeeper.ui.screen.init.list.AccountItem
import com.tonapps.tonkeeper.ui.screen.init.step.LabelScreen
import com.tonapps.tonkeeper.ui.screen.init.step.PasscodeScreen
import com.tonapps.tonkeeper.ui.screen.init.step.PushScreen
import com.tonapps.tonkeeper.ui.screen.init.step.SelectScreen
import com.tonapps.tonkeeper.ui.screen.init.step.WatchScreen
import com.tonapps.tonkeeper.ui.screen.init.step.WordsScreen
import com.tonapps.tonkeeper.ui.screen.notifications.enable.NotificationsEnableScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundPageColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.ton.api.pub.PublicKeyEd25519
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.runAnimation
import uikit.extensions.withAlpha
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class InitScreen: BaseWalletScreen(R.layout.fragment_init), BaseFragment.SwipeBack {

    private val args: InitArgs by lazy {
        InitArgs(requireArguments())
    }

    override val viewModel: InitViewModel by viewModel { parametersOf(args) }

    private lateinit var headerView: HeaderView
    private lateinit var loaderContainerView: View
    private lateinit var loaderIconView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.labelName?.let { viewModel.setLabelName(it) }
        args.accounts?.let { viewModel.setAccounts(it) }
        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            onChildFragment(fragment)
        }
    }

    private fun onChildFragment(fragment: Fragment) {
        if (fragment is PushScreen) {
            headerView.background = null
        } else {
            headerView.setBackgroundResource(uikit.R.drawable.bg_page_gradient)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.setBackgroundResource(uikit.R.drawable.bg_page_gradient)
        headerView.doOnCloseClick = { viewModel.routePopBackStack() }
        headerView.doOnLayout { viewModel.setUiTopOffset(it.measuredHeight) }

        loaderContainerView = view.findViewById(R.id.loader_container)
        loaderContainerView.setOnClickListener { }
        loaderContainerView.setBackgroundColor(requireContext().backgroundPageColor.withAlpha(.64f))

        loaderIconView = view.findViewById(R.id.loader_icon)

        collectFlow(viewModel.eventFlow, ::onEvent)
        collectFlow(viewModel.routeFlow, ::onRoute)
    }

    private fun onEvent(event: InitEvent) {
        when (event) {
            is InitEvent.Back -> popBackStack()
            is InitEvent.Finish -> finish()
            is InitEvent.Loading -> setLoading(event.loading)
        }
    }

    private fun onRoute(route: InitRoute) {
        val fragment = when (route) {
            InitRoute.CreatePasscode -> PasscodeScreen.newInstance(false)
            InitRoute.ReEnterPasscode -> PasscodeScreen.newInstance(true)
            InitRoute.ImportWords -> WordsScreen.newInstance(false)
            InitRoute.WatchAccount -> WatchScreen.newInstance()
            InitRoute.LabelAccount -> LabelScreen.newInstance()
            InitRoute.SelectAccount -> SelectScreen.newInstance()
            InitRoute.Push -> PushScreen.newInstance()
        }

        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(uikit.R.anim.fragment_enter_from_right, uikit.R.anim.fragment_exit_to_left, uikit.R.anim.fragment_enter_from_left, uikit.R.anim.fragment_exit_to_right)
        transaction.replace(R.id.step_container, fragment, fragment.toString())
        transaction.addToBackStack(fragment.toString())
        transaction.commit()
    }

    private fun popBackStack() {
        if (loaderContainerView.visibility == View.VISIBLE) {
            return
        }
        if (1 >= childFragmentManager.backStackEntryCount) {
            finish()
        } else {
            childFragmentManager.popBackStack()
        }
    }

    override fun onBackPressed(): Boolean {
        viewModel.routePopBackStack()
        return false
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            loaderContainerView.visibility = View.VISIBLE
            loaderIconView.runAnimation(R.anim.gear_loading)
        } else {
            loaderContainerView.visibility = View.GONE
            loaderIconView.clearAnimation()
        }
    }

    companion object {

        fun newInstance(
            type: InitArgs.Type,
            publicKeyEd25519: PublicKeyEd25519? = null,
            name: String? = null,
            ledgerConnectData: LedgerConnectData? = null,
            accounts: List<AccountItem>? = null
        ) = newInstance(InitArgs(type, name, publicKeyEd25519, ledgerConnectData, accounts))

        fun newInstance(args: InitArgs): InitScreen {
            val fragment = InitScreen()
            fragment.setArgs(args)
            return fragment
        }
    }
}