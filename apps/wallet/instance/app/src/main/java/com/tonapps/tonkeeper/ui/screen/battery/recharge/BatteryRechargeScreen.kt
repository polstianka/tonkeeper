package com.tonapps.tonkeeper.ui.screen.battery.recharge

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.inflate
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.lifecycleScope
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.extensions.showToast
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.ui.base.BaseListWalletScreen
import com.tonapps.tonkeeper.ui.screen.battery.recharge.entity.BatteryRechargeEvent
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.Adapter
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.send.contacts.SendContactsScreen
import com.tonapps.tonkeeper.ui.screen.send.main.SendContact
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.hideKeyboard
import uikit.widget.FrescoView
import uikit.widget.InputView
import java.util.UUID

class BatteryRechargeScreen : BaseListWalletScreen(), BaseFragment.BottomSheet {

    private val args: RechargeArgs by lazy { RechargeArgs(requireArguments()) }
    private val contractsRequestKey: String by lazy { "contacts_${UUID.randomUUID()}" }

    private val rootViewModel: RootViewModel by activityViewModel()

    override val viewModel: BatteryRechargeViewModel by viewModel { parametersOf(args) }

    private val adapter = Adapter(
        onAddressChange = { viewModel.updateAddress(it) },
        openAddressBook = ::openAddressBook,
        onAmountChange = { viewModel.updateAmount(it) },
        onPackSelect = { viewModel.setSelectedPack(it) },
        onCustomAmountSelect = { viewModel.onCustomAmountSelect() },
        onContinue = ::onContinue,
        onSubmitPromo = { viewModel.applyPromo(it) }
    )

    private lateinit var tokenIconView: FrescoView
    private lateinit var tokenTitleView: AppCompatTextView

    private val addressInput: InputView?
        get() = findListItemView(0)?.findViewById<InputView>(R.id.address)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectFlow(viewModel.uiItemsFlow, adapter::submitList)

        navigation?.setFragmentResultListener(contractsRequestKey) { bundle ->
            val contact = bundle.getParcelableCompat<SendContact>("contact")
                ?: return@setFragmentResultListener
            addressInput?.text = contact.address
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter(adapter)

        val rightContentView = inflate(context, R.layout.view_battery_recharge_token, null)
        tokenIconView = rightContentView.findViewById(R.id.token_icon)
        tokenTitleView = rightContentView.findViewById(R.id.token_title)
        rightContentView.findViewById<LinearLayoutCompat>(R.id.token)
            .setOnClickListener { openTokenSelector() }

        headerView.setRightContent(rightContentView)
        headerView.hideIcon()
        headerView.setTitleGravity(Gravity.START)
        headerView.title = when (args.isGift) {
            true -> getString(Localization.battery_gift_title)
            false -> getString(Localization.battery_recharge_title)
        }
        collectFlow(viewModel.tokenFlow) { token ->
            tokenTitleView.text = token.symbol
            tokenIconView.setImageURI(token.imageUri, this)
        }
        collectFlow(viewModel.eventFlow, ::onEvent)
    }

    private fun onContinue() {
        requireContext().hideKeyboard()
        viewModel.onContinue()
    }

    private fun openAddressBook() {
        navigation?.add(SendContactsScreen.newInstance(contractsRequestKey))
        getCurrentFocus()?.hideKeyboard()
    }

    private fun openTokenSelector() {
        Log.d("BatteryRechargeScreen", "openTokenSelector")
    }

    private fun showError(message: String? = null) {
        navigation?.toast(message ?: getString(Localization.sending_error))
    }

    private fun onSuccess() {
        requireContext().showToast(Localization.battery_please_wait)
        navigation?.openURL("tonkeeper://activity")
        finish()
    }

    private fun sing(
        request: SignRequestEntity
    ) {
        lifecycleScope.launch {
            try {
                rootViewModel.requestSign(requireContext(), request, forceRelayer = true)
                onSuccess()
            } catch (_: Exception) {}
        }
    }

    private fun onEvent(event: BatteryRechargeEvent) {
        when (event) {
            is BatteryRechargeEvent.Sign -> sing(event.request)
            is BatteryRechargeEvent.Error -> showError()
            is BatteryRechargeEvent.MaxAmountError -> {
                val message = requireContext().getString(
                    Localization.battery_max_input_amount,
                    CurrencyFormatter.format(currency = event.currency, value = event.maxAmount)
                )
                showError(message)
            }
        }
    }

    companion object {

        fun newInstance(
            token: AccountTokenEntity? = null, isGift: Boolean = false
        ): BatteryRechargeScreen {
            val args = RechargeArgs(token, isGift)
            val fragment = BatteryRechargeScreen()
            fragment.setArgs(args)
            return fragment
        }
    }
}