package com.tonapps.tonkeeper.ui.screen.swapnative.choose

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.swapnative.choose.list.ChooseTokenAdapter
import com.tonapps.tonkeeper.ui.screen.swapnative.main.TokenSelectionListener
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.widget.HeaderView

class ChooseTokenScreen : BaseFragment(R.layout.fragment_choose_token), BaseFragment.BottomSheet {

    private val args: ChooseTokenArgs by lazy { ChooseTokenArgs(requireArguments()) }

    private val chooseTokenViewModel: ChooseTokenViewModel by viewModel()

    private var tokenSelectionListener: TokenSelectionListener? = null

    private lateinit var headerView: HeaderView
    private lateinit var searchInput: AppCompatEditText
    private lateinit var listView: RecyclerView
    private lateinit var closeButton: Button

    private val adapter = ChooseTokenAdapter {
        it.contractAddress?.also { address ->
            selectToken(address)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        collectFlow(chooseTokenViewModel.uiItemListFlow) { itemList ->
            adapter.submitList(itemList)
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var inputRunnable: Runnable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        searchInput = view.findViewById(R.id.search_input)

        searchInput.doAfterTextChanged {
            inputRunnable?.apply { removeCallbacks(this) }
            inputRunnable = Runnable { chooseTokenViewModel.populateList(it.toString()) }
            inputRunnable?.apply { handler.postDelayed(this, SEARCH_DEBOUNCE) }
        }

        closeButton = view.findViewById(R.id.close)
        closeButton.setOnClickListener {
            finish()
        }

        listView = view.findViewById(R.id.list)
        // listView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.cornerMedium))
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(view.context)
        listView.adapter = adapter


        if (currentMode() == SelectionMode.BUY)
            chooseTokenViewModel.getBuyAssets(args.sellContractAddress!!)
        else chooseTokenViewModel.getSellAssets()

    }

    private fun selectToken(contractAddress: String) {
        if (currentMode() == SelectionMode.SELL)
            tokenSelectionListener?.onSellTokenSelected(contractAddress)
        else tokenSelectionListener?.onBuyTokenSelected(contractAddress)

        finish()
    }

    fun setBottomSheetDismissListener(tokenSelectionListener: TokenSelectionListener) {
        this.tokenSelectionListener = tokenSelectionListener
    }

    private fun currentMode(): SelectionMode {
        return if (args.sellContractAddress != null) SelectionMode.BUY
        else SelectionMode.SELL
    }

    companion object {

        const val SEARCH_DEBOUNCE = 200L

        fun newInstance(sellContractAddress: String?): ChooseTokenScreen {
            val fragment = ChooseTokenScreen()
            fragment.arguments = ChooseTokenArgs(sellContractAddress).toBundle()
            return fragment
        }
    }

    enum class SelectionMode {
        SELL,
        BUY
    }

}