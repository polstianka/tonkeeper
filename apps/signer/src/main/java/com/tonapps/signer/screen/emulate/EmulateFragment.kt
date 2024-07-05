package com.tonapps.signer.screen.emulate

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.tonapps.qr.ui.QRView
import com.tonapps.signer.R
import uikit.base.BaseFragment
import uikit.widget.HeaderView

class EmulateFragment: BaseFragment(R.layout.fragment_emulate), BaseFragment.Modal {

    private val url: String by lazy { arguments?.getString(ARG_URL) ?: "" }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<HeaderView>(R.id.header).doOnActionClick = { finish() }
        view.findViewById<Button>(R.id.done).setOnClickListener { finish() }

        val qrView = view.findViewById<QRView>(R.id.qr)
        qrView.setCorrectionLevelL()
        qrView.animation = false
        qrView.setContent(url)
    }

    companion object {
        private const val ARG_URL = "url"

        fun newInstance(url: String): EmulateFragment {
            val fragment = EmulateFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_URL, url)
            }
            return fragment
        }
    }
}