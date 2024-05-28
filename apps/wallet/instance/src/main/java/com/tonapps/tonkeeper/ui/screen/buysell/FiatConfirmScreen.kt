package com.tonapps.tonkeeper.ui.screen.buysell

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.tonkeeper.fragment.fiat.web.FiatWebFragment
import com.tonapps.tonkeeperx.R
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.InputView

class FiatConfirmScreen : BaseFragment(R.layout.fragment_fiat_confirmation),
    BaseFragment.BottomSheet {

    private val confirmViewModel: FiatConfirmViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var continueButton: Button
    private lateinit var youPayInput: InputView
    private lateinit var youGetInput: InputView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = arguments?.getParcelable<FiatConfirmArgs>(ARGS_KEY) ?: error("Provide args")
        headerView = view.findViewById(R.id.header)
        headerView.setDefault()
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = { finish() }

        view.findViewById<SimpleDraweeView>(R.id.method_image).apply {
            setImageURI(args.iconUrl)
        }
        view.findViewById<AppCompatTextView>(R.id.method_name).apply {
            text = args.name
        }
        view.findViewById<AppCompatTextView>(R.id.method_subtitle).apply {
            text = args.subtitle
        }

        view.findViewById<View>(R.id.root)
            .applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))

        youPayInput = view.findViewById(R.id.you_pay_input)
        youPayInput.editText.addTextChangedListener(
            PostfixTextWatcher(
                youPayInput.editText,
                " ${confirmViewModel.getCurrencyCode()}"
            ) {
                confirmViewModel.onYouPayChanged(it)
            })
        youPayInput.text = "0"
        youGetInput = view.findViewById(R.id.you_get_input)
        youGetInput.editText.addTextChangedListener(
            PostfixTextWatcher(
                youGetInput.editText,
                " TON",
                {

                })
        )

        continueButton = view.findViewById(R.id.continue_action)
        continueButton.setOnClickListener {
            openUrl(args.url, args.pattern)
        }

        collectFlow(confirmViewModel.uiState) { state ->
            youPayInput.text = state.youPayInput
            youGetInput.text = state.youGetInput
            continueButton.isEnabled = state.canContinue
        }

        confirmViewModel.setAmount(args.amount)
    }

    private fun openUrl(
        url: String,
        pattern: String?
    ) {
        navigation?.finishAll()
        navigation?.add(FiatWebFragment.newInstance(url, pattern))
    }

    companion object {
        private const val ARGS_KEY = "args"
        fun newInstance(args: FiatConfirmArgs) = FiatConfirmScreen().apply {
            arguments = bundleOf(ARGS_KEY to args)
        }
    }
}

@Parcelize
data class FiatConfirmArgs(
    val id: String,
    val name: String,
    val subtitle: String,
    val iconUrl: String,
    val amount: Float,
    val url: String,
    val pattern: String?
) : Parcelable
