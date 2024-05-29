package com.tonapps.tonkeeper.fragment.fiat.dialog

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.tonkeeper.core.fiat.models.FiatButton
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textSecondaryColor
import uikit.base.BaseSheetDialog
import uikit.extensions.dp
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.CheckBoxView

internal class ConfirmationDialog(
    context: Context
) : BaseSheetDialog(context) {

    private val iconView: SimpleDraweeView
    private val titleView: AppCompatTextView
    private val subtitleView: AppCompatTextView
    private val infoView: LinearLayoutCompat
    private val button: Button
    private val checkboxContainer: View
    private val checkbox: CheckBoxView

    init {
        setContentView(R.layout.dialog_fiat_open)
        iconView = findViewById(R.id.icon)!!
        titleView = findViewById(R.id.title)!!
        subtitleView = findViewById(R.id.subtitle)!!
        infoView = findViewById(R.id.info)!!
        button = findViewById(R.id.button)!!
        checkboxContainer = findViewById(R.id.checkbox_container)!!
        checkbox = findViewById(R.id.checkbox)!!
        checkboxContainer.setOnClickListener { checkbox.toggle() }
    }

    fun show(
        item: FiatItem,
        doOnOpen: (disableConfirm: Boolean) -> Unit
    ) {
        super.show()
        checkbox.checked = false
        iconView.setImageURI(item.iconUrl)
        titleView.text = item.title
        subtitleView.text = item.subtitle
        button.text = item.actionButton.title
        infoButtons(item.infoButtons)

        button.setOnClickListener {
            dismiss()
            doOnOpen(checkbox.checked)
        }
    }

    private fun infoButtons(buttons: List<FiatButton>) {
        infoView.removeAllViews()
        if (buttons.isEmpty()) {
            infoView.visibility = View.GONE
            return
        }
        infoView.visibility = View.VISIBLE
        for (button in buttons) {
            infoView.addView(createInfoButton(button))
            infoView.addView(View(context), ViewGroup.LayoutParams(6.dp, 0))
        }
    }

    private fun createInfoButton(button: FiatButton): View {
        val buttonView = AppCompatTextView(context)
        buttonView.text = button.title
        buttonView.setOnClickListener {
            navigation?.openURL(button.url, true)
        }
        buttonView.setTextAppearance(uikit.R.style.TextAppearance_Body1)
        buttonView.setTextColor(context.textSecondaryColor)
        return buttonView
    }

}