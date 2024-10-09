package com.tonapps.tonkeeper.ui.screen.init.step

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.tonapps.tonkeeper.ui.screen.init.InitViewModel
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.getDimensionPixelSize

class PushScreen: BaseFragment(R.layout.fragment_notifications_enable) {

    private val initViewModel: InitViewModel by viewModel(ownerProducer = { requireParentFragment() })

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            enablePush()
        } else {
            disablePush()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val laterView = view.findViewById<View>(R.id.later)
        laterView.setOnClickListener { disablePush() }

        val button = view.findViewById<Button>(R.id.button)
        button.setOnClickListener { requestPermission() }

        val offsetMedium = requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium)
        val offsetLarge = requireContext().getDimensionPixelSize(uikit.R.dimen.offsetLarge)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars() + WindowInsetsCompat.Type.navigationBars())
            laterView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBarInsets.top + offsetMedium
            }

            button.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBarInsets.bottom + offsetLarge
            }
            insets
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            enablePush()
        }
    }

    private fun enablePush() {
        initViewModel.enablePush(true)
    }

    private fun disablePush() {
        initViewModel.enablePush(false)
    }

    companion object {
        fun newInstance() = PushScreen()
    }

}