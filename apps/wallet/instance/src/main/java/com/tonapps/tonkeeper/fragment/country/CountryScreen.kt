package com.tonapps.tonkeeper.fragment.country

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.fragment.country.list.CountryAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.mvi.UiScreen
import uikit.navigation.Navigation.Companion.navigation
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

    override val feature: CountryScreenFeature by viewModel()

    private val request: String by lazy { arguments?.getString(REQUEST_KEY) ?: "" }
    private val adapter = CountryAdapter { selectCountry(it.code) }

    private lateinit var headerView: HeaderView
    private lateinit var searchInput: SearchInput
    private lateinit var listView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        searchInput = view.findViewById(R.id.search)
        searchInput.doOnTextChanged = { feature.search(it.toString()) }

        listView = view.findViewById(R.id.list)
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(view.context)
        listView.adapter = adapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        navigation?.setFragmentResult(request)
    }
}