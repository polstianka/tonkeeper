package com.tonapps.tonkeeper.ui.screen.init

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import com.tonapps.tonkeeper.ui.screen.init.step.LabelScreen
import com.tonapps.tonkeeper.ui.screen.init.step.PasscodeScreen
import com.tonapps.tonkeeper.ui.screen.init.step.PushScreen
import com.tonapps.tonkeeper.ui.screen.init.step.SelectScreen
import com.tonapps.tonkeeper.ui.screen.init.step.WatchScreen
import com.tonapps.tonkeeper.ui.screen.init.step.WordsScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundPageColor
import com.tonapps.wallet.data.account.WalletSource
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.ton.api.pub.PublicKeyEd25519
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.runAnimation
import uikit.extensions.withAlpha
import uikit.widget.HeaderView

class InitScreen: BaseFragment(R.layout.fragment_init), BaseFragment.SwipeBack {

    private val args: InitArgs by lazy {
        InitArgs(requireArguments())
    }

    private val initViewModel: InitViewModel by viewModel { parametersOf(args.type) }

    private lateinit var headerView: HeaderView
    private lateinit var loaderContainerView: View
    private lateinit var loaderIconView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.name?.let {
            initViewModel.setLabelName(it)
        }
        args.publicKeyEd25519?.let {
            initViewModel.setPublicKey(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.setBackgroundResource(uikit.R.drawable.bg_page_gradient)
        headerView.doOnCloseClick = { initViewModel.routePopBackStack() }
        headerView.doOnLayout { initViewModel.setUiTopOffset(it.measuredHeight) }

        loaderContainerView = view.findViewById(R.id.loader_container)
        loaderContainerView.setOnClickListener { }
        loaderContainerView.setBackgroundColor(requireContext().backgroundPageColor.withAlpha(.64f))

        loaderIconView = view.findViewById(R.id.loader_icon)

        collectFlow(initViewModel.eventFlow, ::onEvent)
    }

    private fun onEvent(event: InitEvent) {
        when (event) {
            is InitEvent.Back -> popBackStack()
            is InitEvent.Finish -> finish()
            is InitEvent.Loading -> setLoading(event.loading)
            is InitEvent.Step -> setStep(event)
        }
    }

    private fun setStep(step: InitEvent.Step) {
        val fragment = when (step) {
            InitEvent.Step.CreatePasscode -> PasscodeScreen.newInstance(false)
            InitEvent.Step.ReEnterPasscode -> PasscodeScreen.newInstance(true)
            InitEvent.Step.ImportWords -> WordsScreen.newInstance(false)
            InitEvent.Step.WatchAccount -> WatchScreen.newInstance()
            InitEvent.Step.LabelAccount -> LabelScreen.newInstance()
            InitEvent.Step.SelectAccount -> SelectScreen.newInstance()
            InitEvent.Step.Push -> PushScreen.newInstance()
            else -> throw IllegalArgumentException("Unknown step: $step")
        }

        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(uikit.R.anim.fragment_enter_from_right, uikit.R.anim.fragment_exit_to_left, uikit.R.anim.fragment_enter_from_left, uikit.R.anim.fragment_exit_to_right)
        transaction.replace(R.id.step_container, fragment, step.toString())
        transaction.addToBackStack(step.toString())
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
        initViewModel.routePopBackStack()
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
            walletSource: WalletSource? = null
        ) = newInstance(InitArgs(type, name, publicKeyEd25519, walletSource))

        fun newInstance(args: InitArgs): InitScreen {
            val fragment = InitScreen()
            fragment.arguments = args.toBundle()
            return fragment
        }
    }
}