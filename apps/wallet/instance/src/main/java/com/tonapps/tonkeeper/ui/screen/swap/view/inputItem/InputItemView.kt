package com.tonapps.tonkeeper.ui.screen.swap.view.inputItem

import android.animation.ObjectAnimator
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.tonapps.tonkeeperx.R
import uikit.extensions.useAttributes

class InputItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {


    private val sendInputItemView: SendInputItemView
    private val receiveInputItemView: ReceiveInputItemView
    private val icChangeToken: AppCompatImageView
    private val btnChangeToken: FrameLayout

    var buttonChangeTokenHandler: (() -> Unit)? = null
        set(value)  {
            field = value
            btnChangeToken.setOnClickListener {
                val rotationAnimator = ObjectAnimator.ofFloat(icChangeToken, "rotation", 0f, 180f)
                rotationAnimator.duration = 450
                rotationAnimator.start()
                value?.invoke()
            }
        }

    fun setUpClickHandlerToSendItem(itemClick: () -> Unit, buttonMaxClick: () -> Unit) {
        sendInputItemView.buttonItemClick = itemClick
        sendInputItemView.textMaxActionButtonClick = buttonMaxClick
    }

    fun setUpClickHandlerToReceiveItem(itemCLick: () -> Unit) {
        receiveInputItemView.buttonItemClick = itemCLick
    }

    private fun loadingVisibility() {
        sendInputItemView.setUpLoadingVisibility()
        receiveInputItemView.setUpLoadingVisibility()
    }

    fun setUpInputFieldValueSend(newFieldValue: Int) {
        sendInputItemView.setUpInputFieldValue(newFieldValue)
    }

    fun setUpInputFieldValueReceive(newFieldValue: Int) {
        receiveInputItemView.setUpInputFieldValue(newFieldValue)
    }


    fun loadItemSendDataToken(itemModel: InputItemModel?) {
        if (itemModel != null) {
            sendInputItemView.loadButtonItemToken(itemModel.uriImage, itemModel.nameToken)
            if (itemModel.balance != null) sendInputItemView.setBalance(itemModel.balance)
        } else {
            sendInputItemView.setChooseViewType()
        }
    }

    fun setChooseViewTypReceive() {
        receiveInputItemView.setChooseViewType()
    }

    fun loadItemReceiveDataToken(itemModel: InputItemModel?) {
        if (itemModel != null) {
            receiveInputItemView.loadButtonItemToken(itemModel.uriImage, itemModel.nameToken)
            if (itemModel.balance != null) receiveInputItemView.setBalance(itemModel.balance)
        } else {
            receiveInputItemView.setChooseViewType()
        }
    }

    fun updateStatusVisibleDopContent(status: Boolean) {
        receiveInputItemView.updateStateDopContent(status)
    }


    fun getSendEditText(): EditText {
        return sendInputItemView.getEditText()
    }

    fun getReceiveEditText(): EditText {
        return receiveInputItemView.getEditText()
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.input_item_view, this, true)

        sendInputItemView = findViewById(R.id.sendInputItemView)
        receiveInputItemView = findViewById(R.id.receiveInputItemView)
        btnChangeToken = findViewById(R.id.btnChangeToken)
        icChangeToken = findViewById(R.id.ic_change_token)
        loadingVisibility()
    }
}

data class InputItemModel(
    val uriImage: Uri,
    val nameToken: String,
    val balance: Int?
)