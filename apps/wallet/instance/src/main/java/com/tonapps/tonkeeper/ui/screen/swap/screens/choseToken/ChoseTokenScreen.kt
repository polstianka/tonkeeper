package com.tonapps.tonkeeper.ui.screen.swap.screens.choseToken

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeper.ui.screen.swap.SwapViewModel
import com.tonapps.tonkeeper.ui.screen.swap.model.assets.Asset
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.widget.ModalHeader
import uikit.widget.SearchInput
import uikit.widget.SimpleRecyclerView
import java.util.Locale

class ChoseTokenScreen : BaseFragment(R.layout.fragment_chos_token), BaseFragment.BottomSheet {

    private val args: ChoseTokenArgs by lazy { ChoseTokenArgs(requireArguments()) }

    private lateinit var headerView: ModalHeader
    private lateinit var listView: SimpleRecyclerView
    private lateinit var closeBtn: Button
    private lateinit var searchToken: SearchInput
    private val adapter by lazy {  ChoseTokenAdapter(args.selectedToken) }
    private val swapViewModel: SwapViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        listView = view.findViewById(R.id.list)
        closeBtn = view.findViewById(R.id.closeBtn)
        searchToken = view.findViewById(R.id.searchToken)
        headerView.onCloseClick = { finish() }
        closeBtn.setOnClickListener {
            finish()
        }
        adapter.onClickToItem = {
            if(args.type) {
                swapViewModel.updateSendToken(it)
            } else {
                swapViewModel.updateReceiveToken(it)
            }
            finish()
        }
        listView.adapter = adapter
        adapter.submitList(changeAssetToChoseToken(args.listItemToken))

        searchToken.doOnTextChanged = {
            adapter.submitList(changeAssetToChoseToken(searchModels(it.toString())))
        }

    }

    private fun searchModels (query: String): List<Asset> {
        val lowercaseQuery = query.toLowerCase(Locale.ROOT)

        return args.listItemToken.filter { model ->
            model.symbol.toLowerCase(Locale.ROOT).contains(lowercaseQuery)
        }
    }

    private fun changeAssetToChoseToken(listItem: List<Asset>): List<Item.ChoseToken> {
        return listItem.mapIndexed { index, asset ->
            val position = if(listItem.size == 1) {
                ListCell.Position.SINGLE
            } else {
                when (index) {
                    0 -> ListCell.Position.FIRST
                    listItem.size - 1 -> ListCell.Position.LAST
                    else -> ListCell.Position.MIDDLE
                }
            }

            Item.ChoseToken(
                position = position,
                contract_address = asset.contract_address,
                symbol = asset.symbol,
                display_name = asset.display_name,
                image_url = asset.image_url,
                decimals = asset.decimals,
                kind = asset.kind,
                wallet_address = asset.wallet_address,
                balance = asset.balance,
                third_party_usd_price = asset.third_party_usd_price,
                dex_usd_price = asset.dex_usd_price
            )
        }
    }

    companion object {

        fun newInstance(
            listItemToken: List<Asset>,
            selectedToken: Asset?,
            type: Boolean
        ): ChoseTokenScreen {
            val fragment = ChoseTokenScreen()
            fragment.arguments = ChoseTokenArgs(listItemToken, selectedToken, type).toBundle()
            return fragment
        }
    }
}