package com.tonapps.tonkeeper.view

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doAfterTextChanged
import com.tonapps.tonkeeperx.R
import uikit.widget.FrescoView

class SwapToContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    var buyTokenTitle: AppCompatTextView
    var buyTokenIcon: FrescoView
    var buyTokenBalance: AppCompatTextView
    var buyAmountInput: AppCompatEditText
    var toAssetItemContainer: View

    // Swap details
    var swapTitleLine: View
    var swapTitleContaienr: View
    var toggleSwapDetails: View
    var swapDetailContainer: View
    var swapDetailSubContainer: View
    var swapTitleTv: AppCompatTextView
    var priceImpactTv: AppCompatTextView
    var minReceivedTv: AppCompatTextView
    var providerFeeTv: AppCompatTextView
    var blockchainFeeTv: AppCompatTextView
    var routeTv: AppCompatTextView

    var priceImpactInfo: View
    var minReceivedInfo: View
    var providerFeeInfo: View

    var doOnToAssetItemClick: ((view: View) -> Unit)? = null
        set(value) {
            field = value
            toAssetItemContainer.setOnClickListener {
                value?.invoke(it)
            }
        }

    var doAfterToAmountInputTextChanged: ((text: Editable?) -> Unit)? = null
        set(value) {
            field = value
            buyAmountInput.doAfterTextChanged {
                value?.invoke(it)
            }
        }

    var onPriceImpactInfoClicked: ((view: View, message: String) -> Unit)? = null
        set(value) {
            field = value
            priceImpactInfo.setOnClickListener {
                value?.invoke(
                    it,
                    context.getString(com.tonapps.wallet.localization.R.string.price_impact_tooltip)
                )
            }
        }

    var onMinReceivedInfoClicked: ((view: View, message: String) -> Unit)? = null
        set(value) {
            field = value
            minReceivedInfo.setOnClickListener {
                value?.invoke(
                    it,
                    context.getString(com.tonapps.wallet.localization.R.string.minimum_received_tooltip)
                )
            }
        }

    var onProviderFeeInfoClicked: ((view: View, message: String) -> Unit)? = null
        set(value) {
            field = value
            providerFeeInfo.setOnClickListener {
                value?.invoke(
                    it,
                    context.getString(com.tonapps.wallet.localization.R.string.provider_fee_tooltip)
                )
            }
        }

    init {

        inflate(context, R.layout.view_cell_swap_to, this)

        buyAmountInput = findViewById(R.id.receive_amount_input)
        buyTokenBalance = findViewById(R.id.receive_balance)
        buyTokenIcon = findViewById(R.id.receive_token_icon)
        buyTokenTitle = findViewById(R.id.receive_token_title)
        toAssetItemContainer = findViewById(R.id.to_asset_item_container)

        // Swap details
        swapTitleLine = findViewById(R.id.swap_title_line)
        swapTitleContaienr = findViewById(R.id.swap_title_container)
        toggleSwapDetails = findViewById(R.id.toggle_swap_details)
        swapDetailContainer = findViewById(R.id.swap_detail_container)
        swapDetailSubContainer = findViewById(R.id.swap_detail_sub_container)
        swapTitleTv = findViewById(R.id.swap_title)
        priceImpactTv = findViewById(R.id.price_impact)
        minReceivedTv = findViewById(R.id.min_received)
        providerFeeTv = findViewById(R.id.provider_fee)
        blockchainFeeTv = findViewById(R.id.blockchain_fee)
        routeTv = findViewById(R.id.route)

        priceImpactInfo = findViewById(R.id.price_impact_info)
        minReceivedInfo = findViewById(R.id.min_received_info)
        providerFeeInfo = findViewById(R.id.provider_fee_info)
    }

    fun setConfirmMode(isConfirmMode : Boolean) {
        if(isConfirmMode) {
            swapTitleLine.visibility = View.GONE
            swapTitleContaienr.visibility = View.GONE
            buyAmountInput.isEnabled = false

        } else {
            swapTitleLine.visibility = View.VISIBLE
            swapTitleContaienr.visibility = View.VISIBLE
            buyAmountInput.isEnabled = true
        }
    }

}