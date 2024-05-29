package com.tonapps.tonkeeper.fragment.country

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.fragment.country.list.CountryAdapter
import com.tonapps.tonkeeper.ui.component.BlurredRecyclerView
import com.tonapps.uikit.color.backgroundTransparentColor
import uikit.base.BaseFragment
import uikit.drawable.HeaderDrawable
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.topScrolled
import uikit.mvi.UiScreen
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FooterViewEmpty
import uikit.widget.HeaderView
import uikit.widget.SearchInput

class CountryScreen: UiScreen<CountryScreenState, CountryScreenEffect, CountryScreenFeature>(R.layout.fragment_country), BaseFragment.BottomSheet {

    companion object {

        private const val REQUEST_KEY = "request"

        fun newInstance(request: String): CountryScreen {
            val fragment = CountryScreen()
            fragment.arguments = Bundle().apply {
                putString(REQUEST_KEY, request)
            }
            return fragment
        }
    }

    override val feature: CountryScreenFeature by viewModels()

    private val request: String by lazy { arguments?.getString(REQUEST_KEY) ?: "" }
    private val adapter = CountryAdapter { selectCountry(it.code) }

    private lateinit var headerView: HeaderView
    private lateinit var searchInput: SearchInput
    private lateinit var listView: BlurredRecyclerView
    private val drawable by lazy { HeaderDrawable(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val color = requireContext().backgroundTransparentColor
        drawable.setColor(color)

        view.findViewById<View>(R.id.header_wrapper).background = drawable
        view.findViewById<FooterViewEmpty>(R.id.footer).setColor(color)

        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        searchInput = view.findViewById(R.id.search)
        searchInput.doOnTextChanged = { feature.search(it.toString()) }

        listView = view.findViewById(R.id.list)
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(view.context)
        listView.adapter = adapter
        listView.blurredPaddingTop = 128.dp
        listView.unblurredPaddingBottom = 16.dp

        collectFlow(listView.topScrolled, drawable::setDivider)
    }

    override fun newUiState(state: CountryScreenState) {
        adapter.submitList(state.items)
    }

    override fun onEndShowingAnimation() {
        super.onEndShowingAnimation()
        feature.load()
    }

    private fun selectCountry(code: String) {
        feature.setSelection(code)
        finish()
    }

    override fun getViewForNestedScrolling(): View {
        return listView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        navigation?.setFragmentResult(request)
    }
}