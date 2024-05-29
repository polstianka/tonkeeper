package com.tonapps.tonkeeper.fragment.swap.tokens

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.ui.adapter.Adapter
import com.tonapps.tonkeeper.ui.adapter.ItemDecoration
import com.tonapps.tonkeeper.ui.component.BlurredRecyclerView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import com.tonapps.wallet.api.entity.TokenEntity
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.HeaderDrawable
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.topScrolled
import uikit.widget.FooterViewEmpty
import uikit.widget.HeaderViewSimple
import uikit.widget.SearchInput

class TokensSelectorScreen(
    private val onSelectListener: ((t: TokenEntity) -> Unit),
    private val token: TokenEntity?
) : BaseFragment(R.layout.fragment_token_selector), BaseFragment.BottomSheet {
    private val selectorViewModel: TokenSelectorViewModel by viewModel()

    private lateinit var headerView: HeaderViewSimple
    private lateinit var searchInput: SearchInput
    private lateinit var listView: BlurredRecyclerView
    private val drawable by lazy { HeaderDrawable(requireContext()) }

    private val adapter = Adapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val color = requireContext().backgroundTransparentColor
        drawable.setColor(color)

        view.findViewById<View>(R.id.header_wrapper).background = drawable
        view.findViewById<FooterViewEmpty>(R.id.footer).setColor(color)

        selectorViewModel.listener = { token ->
            onSelectListener.invoke(token)
            finish()
        }

        token?.let { selectorViewModel.setToken(it) }

        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        searchInput = view.findViewById(R.id.search)
        searchInput.doOnTextChanged = { selectorViewModel.search(it.toString()) }

        listView = view.findViewById(R.id.list)
        listView.addItemDecoration(ItemDecoration)
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(view.context)
        listView.adapter = adapter
        listView.blurredPaddingTop = 128.dp
        listView.unblurredPaddingBottom = 16.dp

        collectFlow(listView.topScrolled, drawable::setDivider)
        collectFlow(selectorViewModel.uiItemsFlow, adapter::submitList)
    }

    companion object {
        fun newInstance(onSelectListener: ((t: TokenEntity) -> Unit), token: TokenEntity? = null) =
            TokensSelectorScreen(onSelectListener, token)
    }

    override fun getViewForNestedScrolling(): View {
        return listView
    }
}