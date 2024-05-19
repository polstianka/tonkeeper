package com.tonapps.signer.screen.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import com.tonapps.signer.BuildConfig
import com.tonapps.signer.R
import com.tonapps.signer.screen.change.ChangeFragment
import com.tonapps.signer.screen.debug.DebugFragment
import com.tonapps.signer.screen.legal.LegalFragment
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class SettingsFragment: BaseFragment(R.layout.fragment_settings), BaseFragment.SwipeBack {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var headerView: HeaderView
    private lateinit var scrollView: NestedScrollView
    private lateinit var changeView: View
    private lateinit var supportView: View
    private lateinit var legalView: View
    private lateinit var versionView: AppCompatTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnCloseClick = { finish() }

        scrollView = view.findViewById(R.id.scroll)

        changeView = view.findViewById(R.id.change)
        changeView.setOnClickListener { navigation?.add(ChangeFragment.newInstance()) }

        supportView = view.findViewById(R.id.support)
        supportView.setOnClickListener { openSupport() }

        legalView = view.findViewById(R.id.legal)
        legalView.setOnClickListener {
            navigation?.add(LegalFragment.newInstance())
        }

        versionView = view.findViewById(R.id.version)
        versionView.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        versionView.setOnClickListener {
            navigation?.add(DebugFragment.newInstance())
        }
    }

    private fun openSupport() {
        val uri = Uri.parse("https://t.me/tonkeeper_supportbot")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}