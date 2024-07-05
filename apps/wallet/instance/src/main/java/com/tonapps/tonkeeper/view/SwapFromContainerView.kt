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

class SwapFromContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    var sellTokenTitle: AppCompatTextView
    var sellTokenIcon: FrescoView
    var sellTokenBalance: AppCompatTextView
    var selectMaxSellBalance: AppCompatTextView
    var sellAmountInput: AppCompatEditText
    var fromAssetItemContainer: View

    var doOnFromAssetItemClick: ((view: View) -> Unit)? = null
        set(value) {
            field = value
            fromAssetItemContainer.setOnClickListener {
                value?.invoke(it)
            }
        }

    var doOnMaxBalanceClick: ((view: View) -> Unit)? = null
        set(value) {
            field = value
            selectMaxSellBalance.setOnClickListener {
                value?.invoke(it)
            }
        }

    var doAfterFromAmountInputTextChanged: ((text: Editable?) -> Unit)? = null
        set(value) {
            field = value
            sellAmountInput.doAfterTextChanged {
                value?.invoke(it)
            }
        }


    var fromAssetTitle: CharSequence
        get() = sellTokenTitle.text
        set(value) {
            sellTokenTitle.text = value
        }

    var isInputEnabled: Boolean
        get() = sellAmountInput.isEnabled
        set(value) {
            sellAmountInput.isEnabled = value
        }

    init {

        inflate(context, R.layout.view_cell_swap_from, this)

        sellAmountInput = findViewById(R.id.send_amount_input)
        selectMaxSellBalance = findViewById(R.id.max)
        sellTokenBalance = findViewById(R.id.send_balance)
        sellTokenIcon = findViewById(R.id.send_token_icon)
        sellTokenTitle = findViewById(R.id.send_token_title)
        fromAssetItemContainer = findViewById(R.id.from_asset_item_container)

    }

    fun setConfirmMode(isConfirmMode: Boolean) {
        if (isConfirmMode) {
            selectMaxSellBalance.visibility = View.GONE
            sellAmountInput.isEnabled = false
        } else {
            selectMaxSellBalance.visibility = View.VISIBLE
            sellAmountInput.isEnabled = true
        }
    }


}