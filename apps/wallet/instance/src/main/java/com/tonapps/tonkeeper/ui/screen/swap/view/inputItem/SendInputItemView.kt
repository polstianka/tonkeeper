package com.tonapps.tonkeeper.ui.screen.swap.view.inputItem

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.ui.screen.swap.fromNanocoinToCoin
import com.tonapps.tonkeeperx.R
import uikit.widget.FrescoView

class SendInputItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private val editInputFieldValue: AppCompatEditText
    private val textBalance: TextView
    private val textMaxAction: TextView
    private val buttonItem: FrameLayout
    private val imageFresco: FrescoView
    private val txReciveItem: TextView
    private val textToken: TextView

    fun setBalance(balance: Int) {
        textBalance.text = "Balance: ${(balance.fromNanocoinToCoin())}"
    }


    var textMaxActionButtonClick: (() -> Unit)? = null
        set(value) {
            field = value
            textMaxAction.setOnClickListener {
                value?.invoke()
            }
        }


    var buttonItemClick: (() -> Unit)? = null
        set(value)  {
            field = value
            buttonItem.setOnClickListener {
                value?.invoke()
            }
        }

    fun setUpInputFieldValue(newFieldValue: Int) {
        editInputFieldValue.setText(newFieldValue.toString())
    }


    fun setChooseViewType() {
        buttonItem.findViewById<TextView>(R.id.txReciveItem).isVisible = true
        editInputFieldValue.isActivated = false
        textBalance.isVisible = false
        textMaxAction.isVisible = false
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


    fun loadButtonItemToken(uriImage: Uri, nameToken: String) {
        setUpVisibility()
        setVisibilityToken()
        imageFresco.setImageURI(uriImage, this)
        textToken.text = nameToken
    }

    fun setUpLoadingVisibility() {
        textBalance.isVisible = false
        textMaxAction.isVisible = false
        imageFresco.isVisible = false
        txReciveItem.isVisible = false
    }

    private fun setUpVisibility() {
        textBalance.isVisible = true
        textMaxAction.isVisible = true
        imageFresco.isVisible = true
        textToken.isVisible = true
    }


    fun getEditText(): EditText {
        return editInputFieldValue
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.send_input_item_view, this, true)

        editInputFieldValue = findViewById(R.id.inputFieldValue)
        textBalance = findViewById(R.id.textBalance)
        textMaxAction = findViewById(R.id.textMaxAction)
        buttonItem = findViewById(R.id.buttonItem)
        imageFresco = buttonItem.findViewById(R.id.icCurItem)
        txReciveItem = buttonItem.findViewById(R.id.txReciveItem)
        textToken = buttonItem.findViewById<TextView>(R.id.txCurItem)
    }

}