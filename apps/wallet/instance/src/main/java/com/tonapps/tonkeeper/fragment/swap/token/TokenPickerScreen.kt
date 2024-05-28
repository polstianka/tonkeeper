package com.tonapps.tonkeeper.fragment.swap.token

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo
import com.tonapps.tonkeeper.fragment.swap.token.list.TokenAdapter
import com.tonapps.tonkeeper.fragment.swap.token.list.TokenItem
import com.tonapps.tonkeeper.fragment.swap.token.suggestions.SuggestionItemDecoration
import com.tonapps.tonkeeper.fragment.swap.token.suggestions.SuggestionTokenAdapter
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.hideKeyboard
import uikit.extensions.setPaddingHorizontal
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.SearchInput

class TokenPickerScreen: BaseFragment(R.layout.fragment_swap_token_picker), BaseFragment.BottomSheet {

    companion object {

        const val RESULT_KEY = "result_key"
        private const val REQUEST_KEY = "request"
        private const val TOKENS_KEY = "tokens"
        private const val SELECTED_TOKEN_KEY = "selected_token"
        private const val EXCEPT_TOKEN_KEY = "except_token"

        fun newInstance(request: String, tokens: List<TokenInfo>, selected: TokenInfo?, except: TokenInfo?): TokenPickerScreen {
            val fragment = TokenPickerScreen()
            fragment.arguments = Bundle().apply {
                putString(REQUEST_KEY, request)
                putParcelableArray(TOKENS_KEY, tokens.toTypedArray())
                putParcelable(SELECTED_TOKEN_KEY, selected)
                putParcelable(EXCEPT_TOKEN_KEY, except)
            }
            return fragment
        }
    }

    private val feature: TokenPickerScreenFeature by viewModel()

    private val request: String by lazy { arguments?.getString(REQUEST_KEY) ?: "" }
    @Suppress("UNCHECKED_CAST")
    private val tokens: Array<TokenInfo> by lazy { arguments?.getParcelableArray(TOKENS_KEY) as? Array<TokenInfo>? ?: emptyArray() }
    private val selectedToken: TokenInfo? by lazy { arguments?.getParcelable(SELECTED_TOKEN_KEY) }
    private val exceptToken: TokenInfo? by lazy { arguments?.getParcelable(EXCEPT_TOKEN_KEY) }
    private val adapter = TokenAdapter(::pickToken)
    private val suggestionsAdapter = SuggestionTokenAdapter { pickToken(it) }

    private lateinit var headerView: HeaderView
    private lateinit var listView: RecyclerView
    private lateinit var suggestionsView: RecyclerView
    private lateinit var searchInput: SearchInput
    private lateinit var closeView: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.list)
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(view.context)
        listView.adapter = adapter

        suggestionsView = view.findViewById(R.id.suggestions)
        suggestionsView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(view.context, RecyclerView.HORIZONTAL)
        suggestionsView.adapter = suggestionsAdapter
        suggestionsView.addItemDecoration(SuggestionItemDecoration(view.context))

        searchInput = view.findViewById(R.id.search)
        searchInput.doOnTextChanged = { feature.search(it.toString()) }

        headerView = view.findViewById(R.id.title)
        headerView.setAction(com.tonapps.uikit.icon.R.drawable.ic_close_16)
        headerView.closeView.visibility = View.GONE
        val params = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.START
        }
        headerView.titleView.layoutParams = params
        headerView.findViewById<View>(uikit.R.id.header_text).setPaddingHorizontal(0)
        headerView.doOnActionClick = { finish() }

        closeView = view.findViewById(R.id.close)
        closeView.setOnClickListener { finish() }

        feature.setupTokens(tokens = tokens, selectedToken = selectedToken, exceptToken = exceptToken)

        collectFlow(feature.tokensFlow, adapter::submitList)
        collectFlow(feature.suggestionTokens, suggestionsAdapter::submitList)
    }

    override fun onEndShowingAnimation() {
        super.onEndShowingAnimation()
        getCurrentFocus()?.hideKeyboard()
    }

    private fun pickToken(token: TokenItem) {
        pickToken(token.tokenInfo)
    }

    private fun pickToken(token: TokenInfo) {
        navigation?.setFragmentResult(request, bundleOf(RESULT_KEY to token))
        finish()
    }
}