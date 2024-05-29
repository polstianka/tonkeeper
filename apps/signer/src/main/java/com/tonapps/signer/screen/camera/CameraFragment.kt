package com.tonapps.signer.screen.camera

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageView
import androidx.camera.core.TorchState
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.tonapps.qr.QRImageAnalyzer
import com.tonapps.signer.Key
import com.tonapps.signer.R
import com.tonapps.signer.extensions.openAppSettings
import com.tonapps.signer.extensions.uriOrNull
import com.tonapps.signer.password.Password
import com.tonapps.signer.screen.root.RootViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.HapticHelper
import uikit.base.BaseFragment
import uikit.extensions.applyBottomInsets
import uikit.widget.HeaderView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment: BaseFragment(R.layout.fragment_camera), BaseFragment.BottomSheet {

    companion object {
        fun newInstance() = CameraFragment()
    }

    private val rootViewModel: RootViewModel by activityViewModel()

    private val activityResultLauncher = registerForPermission { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            showPermissionContainer()
        }
    }

    private var isFlashAvailable = false

    private val qrAnalyzer = QRImageAnalyzer()
    private val chunks = mutableListOf<String>()

    private lateinit var cameraContainer: View
    private lateinit var headerView: HeaderView
    private lateinit var cameraView: PreviewView
    private lateinit var flashView: AppCompatImageView
    private lateinit var buttonSettings: Button

    private lateinit var permissionContainer: View

    private lateinit var cameraExecutor: ExecutorService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraContainer = view.findViewById(R.id.camera_container)

        headerView = view.findViewById(R.id.header)
        headerView.background = null
        headerView.doOnCloseClick = { finish() }

        cameraView = view.findViewById(R.id.camera)
        flashView = view.findViewById(R.id.flash)

        buttonSettings = view.findViewById(R.id.button_settings)
        buttonSettings.applyBottomInsets()
        buttonSettings.setOnClickListener {
            requireContext().openAppSettings()
            finish()
        }

        permissionContainer = view.findViewById(R.id.permission_container)

        cameraExecutor = Executors.newSingleThreadExecutor()

        qrAnalyzer.flow.onEach(::handleBarcode).launchIn(lifecycleScope)

        checkAndStartCamera()
    }

    private fun showPermissionContainer() {
        cameraContainer.visibility = View.GONE
        permissionContainer.visibility = View.VISIBLE
    }

    private fun handleBarcode(barcode: Barcode) {
        val data = barcode.rawValue ?: return

        if (data.startsWith("${Key.SCHEME}://")) {
            if (chunks.size > 0) {
                chunks.clear()
            }
            chunks.add(data)
        } else if (chunks.size > 0 && !data.startsWith("${Key.SCHEME}://")) {
            chunks.add(data)
        }

        val uri = chunks.joinToString("").uriOrNull ?: return
        if (rootViewModel.processDeepLink(uri, false)) {
            finishDelay()
        }
    }

    private fun finishDelay() {
        postDelayed(/*ModalView.animationDuration*/220) {
            finish()
        }
    }

    private fun startCamera() {
        val context = requireContext()
        val cameraController = LifecycleCameraController(context)
        cameraController.setImageAnalysisAnalyzer(mainExecutor, qrAnalyzer)

        cameraController.bindToLifecycle(this)
        cameraView.controller = cameraController

        applyFlash(cameraController)
    }

    private fun applyFlash(cameraController: CameraController) {
        cameraController.torchState.observe(viewLifecycleOwner) { state ->
            if (!isFlashAvailable) {
                isFlashAvailable = true
                flashView.visibility = View.VISIBLE
                return@observe
            }
            if (state == TorchState.ON) {
                flashEnabled()
            } else {
                flashDisabled()
            }

            HapticHelper.impactLight(requireContext())
        }

        flashView.setOnClickListener {
            val flashMode = cameraController.torchState.value == TorchState.ON
            cameraController.enableTorch(!flashMode)
        }
    }

    private fun flashEnabled() {
        setFlashColor(Color.WHITE, Color.BLACK)
    }

    private fun flashDisabled() {
        setFlashColor(Color.parseColor("#14ffffff"), Color.WHITE)
    }

    private fun setFlashColor(backgroundColor: Int, iconColor: Int) {
        flashView.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        flashView.imageTintList = ColorStateList.valueOf(iconColor)
    }

    private fun checkAndStartCamera() {
        if (hasPermission(android.Manifest.permission.CAMERA)) {
            startCamera()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.CAMERA)) {
            showPermissionContainer()
        } else {
            Password.setUnlock()
            activityResultLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    override fun onDestroy() {
        super.onDestroy()
        qrAnalyzer.release()
    }
}