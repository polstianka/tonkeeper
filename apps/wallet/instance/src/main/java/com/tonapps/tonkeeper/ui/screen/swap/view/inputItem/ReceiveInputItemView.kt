package com.tonapps.tonkeeper.ui.screen.swap.view.inputItem

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.swap.fromNanocoinToCoin
import com.tonapps.tonkeeper.ui.screen.swap.screens.ReceiveMoreItemAdapter
import com.tonapps.tonkeeperx.R
import uikit.widget.FrescoView

class ReceiveInputItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private val editInputFieldValue: AppCompatEditText
    private val textBalance: TextView
    private val buttonItem: FrameLayout
    private val imageFresco: FrescoView
    private val txReciveItem: TextView
    private val textToken: TextView
    private val dopContent: LinearLayout
    private val list: RecyclerView


    private val adapter = ReceiveMoreItemAdapter()


    var buttonItemClick: (() -> Unit)? = null
        set(value) {
            field = value
            buttonItem.setOnClickListener {
                value?.invoke()
            }
        }

    fun setBalance(balance: Int) {
        textBalance.text = "Balance: ${(balance.fromNanocoinToCoin())}"
    }


    fun setUpInputFieldValue(newFieldValue: Int) {
        editInputFieldValue.setText(newFieldValue.toString())
    }

    fun loadButtonItemToken(uriImage: Uri, nameToken: String) {
        setVisibilityToken()
        val imageFresco = buttonItem.findViewById<FrescoView>(R.id.icCurItem)
        imageFresco.setImageURI(uriImage, this)
        textToken.text = nameToken
    }

    fun setChooseViewType() {
        buttonItem.findViewById<TextView>(R.id.txReciveItem).isVisible = true
        editInputFieldValue.isActivated = false
        textBalance.isVisible = false
        imageFresco.isVisible = false
        textToken.isVisible = false
    }

    private fun setVisibilityToken() {
        textBalance.isVisible = true
        buttonItem.findViewById<TextView>(R.id.txReciveItem).isVisible = false
        editInputFieldValue.isActivated = true
        imageFresco.isVisible = true
        textToken.isVisible = true
    }

    fun setUpLoadingVisibility() {
        textBalance.isVisible = false
        imageFresco.isVisible = false
        txReciveItem.isVisible = false
    }

    fun updateStateDopContent(newState: Boolean) {
        dopContent.isVisible = newState
    }

    private fun basicSetUpList() {
        list.adapter = adapter
    }

    fun getEditText(): EditText {
        return editInputFieldValue
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.receive_input_item_view, this, true)

        editInputFieldValue = findViewById(R.id.inputFieldValue)
        textBalance = findViewById(R.id.textBalance)
        buttonItem = findViewById(R.id.buttonItem)
        imageFresco = buttonItem.findViewById(R.id.icCurItem)
        txReciveItem = buttonItem.findViewById(R.id.txReciveItem)
        textToken = buttonItem.findViewById<TextView>(R.id.txCurItem)
        dopContent = findViewById(R.id.dopContent)
        list = dopContent.findViewById(R.id.list)
        basicSetUpList()

    }

}