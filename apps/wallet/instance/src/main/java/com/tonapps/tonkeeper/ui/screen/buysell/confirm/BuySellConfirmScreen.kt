package com.tonapps.tonkeeper.ui.screen.buysell.confirm

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.ui.screen.buysell.BuySellScreenEffect
import com.tonapps.tonkeeper.ui.screen.buysell.pager.PagerScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.dp
import uikit.extensions.hideKeyboard
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import uikit.widget.InputView
import uikit.widget.LoaderView
import uikit.widget.webview.WebViewFixed

class BuySellConfirmScreen : PagerScreen<BuySellConfirmScreenState, BuySellConfirmScreenEffect, BuySellConfirmScreenFeature>(R.layout.fragment_buysell_confirm) {

    companion object {
        fun newInstance() = BuySellConfirmScreen()
    }

    override val feature: BuySellConfirmScreenFeature by viewModel()

    private lateinit var continueButton: Button
    private lateinit var loaderView: LoaderView
    private lateinit var webView: WebViewFixed
    private lateinit var webViewContainer: View
    private lateinit var icon: FrescoView
    private lateinit var title: AppCompatTextView
    private lateinit var subtitle: AppCompatTextView
    private lateinit var mainContainer: View
    private lateinit var rate: AppCompatTextView
    private lateinit var custom: View
    private lateinit var customEdit: AppCompatEditText
    private lateinit var percent: AppCompatTextView
    private lateinit var custom2: View
    private lateinit var customEdit2: AppCompatEditText
    private lateinit var percent2: AppCompatTextView
    private lateinit var privacy: AppCompatTextView
    private lateinit var provider: AppCompatTextView
    private var loadedUrl = false
    private var skipTextChangeListener = false
    private var canAnimateKeyboard = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loaderView = view.findViewById(R.id.loader)
        webViewContainer = view.findViewById(R.id.web_view_container)
        mainContainer = view.findViewById(R.id.main_container)
        webView = view.findViewById(R.id.web)
        icon = view.findViewById(R.id.icon)
        title = view.findViewById(R.id.title)
        subtitle = view.findViewById(R.id.subtitle)
        rate = view.findViewById(R.id.rate)
        privacy = view.findViewById(R.id.privacy)
        provider = view.findViewById(R.id.provided)

        webView.webChromeClient = object : android.webkit.WebChromeClient() {

        }
        webView.webViewClient = object : android.webkit.WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.visibility = View.VISIBLE
                loaderView.visibility = View.GONE
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url?.matches(".*#endsession".toRegex()) == true) {
                    buySellFeature.sendEffect(BuySellScreenEffect.Finish)
                    return
                }
                val successUrlPattern = feature.uiState.value.selectedOperator?.successUrlPattern?.pattern ?: return
                val regexp = Regex(successUrlPattern, RegexOption.IGNORE_CASE)

                regexp.find(url ?: "")?.groupValues ?: return

                navigation?.toast(Localization.done)
                buySellFeature.sendEffect(BuySellScreenEffect.Finish)
            }
        }

        percent = view.findViewById(R.id.percent)
        custom = view.findViewById(R.id.custom)
        customEdit = view.findViewById(R.id.custom_edit)

        percent2 = view.findViewById(R.id.percent2)
        custom2 = view.findViewById(R.id.custom2)
        customEdit2 = view.findViewById(R.id.custom_edit2)

        custom.setOnClickListener {
            customEdit.requestFocus()
        }

        customEdit.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, b: Boolean) {
                custom.isSelected = b
                if (!b && customEdit.text.toString().isEmpty()) {
                    percent.isVisible = false
                } else if (b) {
                    percent.isVisible = true
                }
            }
        })

        customEdit.doOnTextChanged { text, start, before, count ->
            percent.animate().cancel()
            percent.animate().translationX(
                if (customEdit.text.toString().isEmpty()) 5.dp.toFloat() else 5.dp + customEdit.text.toString().length * 10.dp.toFloat()
            ).setDuration(50).start()
        }

        customEdit.doAfterTextChanged {
            if (!skipTextChangeListener) {
                feature.setValue(customEdit.text.toString())
            }
        }

        custom2.setOnClickListener {
            customEdit2.requestFocus()
        }

        customEdit2.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, b: Boolean) {
                custom2.isSelected = b
                if (!b && customEdit2.text.toString().isEmpty()) {
                    percent2.isVisible = false
                } else if (b) {
                    percent2.isVisible = true
                }
            }
        })

        customEdit2.doOnTextChanged { text, start, before, count ->
            percent2.animate().cancel()
            percent2.animate().translationX(
                if (customEdit2.text.toString().isEmpty()) 5.dp.toFloat() else 5.dp + customEdit2.text.toString().length * 10.dp.toFloat()
            ).setDuration(50).start()
        }

        customEdit2.doAfterTextChanged {
            if (!skipTextChangeListener) {
                feature.setValue2(customEdit2.text.toString())
            }
        }

        continueButton = view.findViewById(R.id.continue_action)
        continueButton.isEnabled = false
        continueButton.setOnClickListener {
            if (customEdit.isFocused) {
                customEdit.hideKeyboard()
            }
            if (customEdit2.isFocused) {
                customEdit2.hideKeyboard()
            }
            feature.operate()
        }
    }

    fun onKeyboardAppear(progress: Float) {
        if (!canAnimateKeyboard) return
        privacy.alpha = 1f - progress
        provider.alpha = 1f - progress
        val to = if (privacy.isVisible) 60.dp.toFloat() else 40.dp.toFloat()
        continueButton.translationY = (to * progress * 1.23f).coerceAtMost(to)
    }

    override fun newUiState(state: BuySellConfirmScreenState) {
        skipTextChangeListener = true
        webViewContainer.isVisible = state.loading
        mainContainer.isVisible = !state.loading
        continueButton.isVisible = !state.loading
        continueButton.isVisible = !state.loading
        continueButton.isVisible = !state.loading
        continueButton.isEnabled = state.canContinue
        provider.isVisible = !state.loading
        state.selectedOperator?.let {
            icon.setImageURI(it.logo, this)
            title.text = it.name
            val fiatValue = (state.amountCrypto * it.rate).toString()
            val cryptoValue =  (state.amountCrypto).toString()
            val youPay = if (state.tradeType == TradeType.BUY) fiatValue else cryptoValue
            val youGet = if (state.tradeType == TradeType.BUY) cryptoValue else fiatValue
            percent.isVisible = true
            percent2.isVisible = true
            if (!customEdit.isFocused) {
                customEdit.setText("")
                customEdit.append(youPay)
            }
            if (!customEdit2.isFocused) {
                customEdit2.setText("")
                customEdit2.append(youGet)
            }
        }

        custom.isActivated = state.error1
        custom2.isActivated = state.error2

        percent.text = if (state.tradeType == TradeType.BUY) state.currency.code else "TON"
        percent2.text = if (state.tradeType == TradeType.BUY) "TON" else state.currency.code
        rate.text = state.rate

        privacy.isVisible = !state.loading && state.selectedOperator?.privacyPolicyUrl?.isNotEmpty() == true
        if (state.selectedOperator?.privacyPolicyUrl?.isNotEmpty() == true) {
            val privacyText = getString(Localization.privacy)
            val termsText = getString(Localization.terms)
            val text = "$privacyText · $termsText"
            val spannableString = SpannableString(text)
            spannableString.setSpan(object : ClickableSpan() {
                override fun onClick(view: View) {
                    navigation?.openURL(state.selectedOperator.privacyPolicyUrl, true)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                }
            }, 0, privacyText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(requireContext().textSecondaryColor), 0, privacyText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            val start = privacyText.length + 3
            spannableString.setSpan(object : ClickableSpan() {
                override fun onClick(view: View) {
                    navigation?.openURL(state.selectedOperator.termsOfUseUrl, true)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                }
            }, start, start + termsText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(requireContext().textSecondaryColor), start, start + termsText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            privacy.text = spannableString
            privacy.movementMethod = LinkMovementMethod.getInstance()
            privacy.invalidate()
        }
        provider.text = getString(Localization.provided, state.selectedOperator?.name)

        subtitle.text = getString(Localization.operation_hint, state.tradeType.type, "${state.buySellType?.title?.lowercase()}")
        if (state.url.isNotEmpty() && !loadedUrl) {
            webView.loadUrl(state.url)
            loadedUrl = true
        }
        skipTextChangeListener = false
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            buySellFeature.setHeaderVisible(false)
            buySellFeature.data.value?.let {
                feature.setData(it)
            }
            canAnimateKeyboard = true
            privacy.alpha = 1f
            provider.alpha = 1f
            continueButton.translationY = 0f
        } else {
            canAnimateKeyboard = false
            if (customEdit.isFocused) {
                customEdit.hideKeyboard()
            }
            if (customEdit2.isFocused) {
                customEdit2.hideKeyboard()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.destroy()
    }
}