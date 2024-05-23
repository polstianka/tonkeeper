package com.tonapps.tonkeeper.ui.screen.settings.slippage

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.tonapps.tonkeeperx.R
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.dp
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.widget.HeaderView
import uikit.widget.InputView
import uikit.widget.SwitchView

class SlippageScreen: BaseFragment(R.layout.fragment_slippage), BaseFragment.BottomSheet {

    private lateinit var headerTitle: AppCompatTextView
    private lateinit var headerClose: View
    private lateinit var headerText: LinearLayoutCompat
    private lateinit var headerView: HeaderView
    private lateinit var inputView: InputView
    private lateinit var switchView: SwitchView
    private lateinit var saveButton: Button
    private lateinit var oneButton: Button
    private lateinit var threeButton: Button
    private lateinit var fiveButton: Button
    override val scaleBackgroundInBottomSheet = false
    private val params = LinearLayoutCompat.LayoutParams(
        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
        LinearLayoutCompat.LayoutParams.MATCH_PARENT
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveButton = view.findViewById(R.id.save_button)
        oneButton = view.findViewById(R.id.one)
        threeButton = view.findViewById(R.id.three)
        fiveButton = view.findViewById(R.id.five)
        headerView = view.findViewById(R.id.header)
        switchView = view.findViewById(R.id.check)

        headerView.doOnActionClick = { this.finish() }
        headerView.clipToPadding = false
        headerView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetExtraExtraSmall))

        headerClose = view.findViewById(uikit.R.id.header_close)
        headerClose.visibility = View.GONE

        headerTitle = view.findViewById(uikit.R.id.header_title)
        headerTitle.layoutParams = params
        headerTitle.textAlignment = View.TEXT_ALIGNMENT_TEXT_START

        headerText = view.findViewById(uikit.R.id.header_text)
        headerText.setPaddingHorizontal(0.dp)

        inputView = view.findViewById(R.id.percent)
        inputView.setDecimalFilter(50)
        inputView.inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        inputView.doOnTextChange = {
            if(it == "" || it.toFloat() == 0f){
                inputView.error = true
                saveButton.isEnabled = false
            }
            else{
                inputView.error = false
                saveButton.isEnabled = true
            }
        }

        switchView.doCheckedChanged = {
            inputView.setDecimalFilter(if(it) 100 else 50)
        }

        saveButton.setOnClickListener{
            Log.d("SaveSlippage", inputView.text)
        }
        oneButton.setOnClickListener{
            inputView.text = "1"
        }
        threeButton.setOnClickListener{
            inputView.text = "3"
        }
        fiveButton.setOnClickListener{
            inputView.text = "5"
        }
    }

    companion object {
        fun newInstance() = SlippageScreen()
    }
}