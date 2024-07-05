package com.tonapps.signer.screen.root

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.signer.BuildConfig
import com.tonapps.signer.Key
import com.tonapps.signer.R
import com.tonapps.signer.SimpleState
import com.tonapps.signer.extensions.toast
import com.tonapps.signer.password.Password
import com.tonapps.signer.password.ui.PasswordView
import com.tonapps.signer.screen.intro.IntroFragment
import com.tonapps.signer.screen.main.MainFragment
import com.tonapps.signer.screen.root.action.RootAction
import com.tonapps.signer.screen.sign.SignFragment
import com.tonapps.signer.screen.update.UpdateFragment
import kotlinx.coroutines.Job
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.ton.api.pub.PublicKeyEd25519
import org.ton.crypto.hex
import uikit.dialog.alert.AlertDialog
import uikit.extensions.collectFlow
import uikit.extensions.primaryFragment
import uikit.extensions.setPaddingTop
import uikit.navigation.NavigationActivity

class RootActivity : NavigationActivity() {

    private val rootViewModel: RootViewModel by viewModel()

    private lateinit var baseContainer: View
    private lateinit var rootContainer: View
    private lateinit var sheetContainer: View
    private lateinit var lockView: View
    private lateinit var lockSignOutButton: View
    private lateinit var lockPasswordView: PasswordView

    private var checkPasswordJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Signer)
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        baseContainer = findViewById(R.id.base)

        rootContainer = findViewById(uikit.R.id.root_container)
        sheetContainer = findViewById(uikit.R.id.sheet_container)

        lockView = findViewById(R.id.lock)
        lockView.setOnClickListener { }

        lockSignOutButton = findViewById(R.id.lock_sign_out)
        lockSignOutButton.setOnClickListener { signOut() }

        lockPasswordView = findViewById(R.id.lock_password)
        lockPasswordView.doOnPassword = ::checkPassword

        ViewCompat.setOnApplyWindowInsetsListener(lockView) { _, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            lockView.setPaddingTop(topInset)
            insets
        }

        collectFlow(rootViewModel.hasKeys, ::init)
        collectFlow(rootViewModel.action, ::onAction)

        handleIntent(intent)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.activity_root)
    }

    private fun checkPassword(password: CharArray) {
        setPasswordState(SimpleState.Loading)

        checkPasswordJob?.cancel()
        checkPasswordJob = collectFlow(rootViewModel.checkPassword(password)) {
            if (it) {
                setPasswordState(SimpleState.Success)
            } else {
                setPasswordState(SimpleState.Error)
            }
        }
    }

    private fun setPasswordState(state: SimpleState) {
        when (state) {
            SimpleState.Default -> lockPasswordView.applyDefaultState()
            SimpleState.Error -> lockPasswordView.applyErrorState()
            SimpleState.Success -> lockPasswordView.applySuccessState()
            SimpleState.Loading -> lockPasswordView.applyLoadingState()
        }

        if (state == SimpleState.Success) {
            hideLockPassword()
        }
    }

    private fun hideLockPassword() {
        if (rootContainer.visibility != View.VISIBLE) {
            rootContainer.visibility = View.VISIBLE
            sheetContainer.visibility = View.VISIBLE
            lockView.visibility = View.GONE
        }
    }

    private fun showLockPassword() {
        if (rootContainer.visibility != View.GONE) {
            rootContainer.visibility = View.GONE
            sheetContainer.visibility = View.GONE
            lockView.visibility = View.VISIBLE
            lockPasswordView.reset()
        }

        lockPasswordView.focus()
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(this)
        builder.setColoredButtons()
        builder.setTitle(R.string.sign_out_question)
        builder.setMessage(R.string.sign_out_subtitle)
        builder.setPositiveButton(R.string.cancel)
        builder.setNegativeButton(R.string.sign_out) {
            rootViewModel.signOut()
        }
        builder.show()
    }

    private fun onAction(action: RootAction) {
        when (action) {
            is RootAction.RequestBodySign -> requestSign(action)
            is RootAction.ResponseSignature -> {
                responseSignature(action.signature)
            }

            is RootAction.ResponseKey -> responseKey(action.publicKey, action.name)
            is RootAction.UpdateApp -> updateDialog()
        }
    }

    private fun requestSign(request: RootAction.RequestBodySign) {
        removeSignSheets {
            add(SignFragment.newInstance(request.id, request.body, request.v, request.returnResult))
        }
    }

    private fun removeSignSheets(runnable: Runnable) {
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach {
            if (it is SignFragment) {
                transaction.remove(it)
            }
        }
        transaction.runOnCommit(runnable)
        transaction.commitNow()
    }

    private fun responseSignature(sign: ByteArray) {
        val intent = Intent()
        intent.putExtra(Key.SIGN, hex(sign))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun responseKey(publicKey: PublicKeyEd25519, name: String) {
        val intent = Intent()
        intent.putExtra(Key.PK, publicKey.base64())
        intent.putExtra(Key.NAME, name)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun init(hasKeys: Boolean) {
        if (hasKeys) {
            setMainFragment()
        } else {
            setIntroFragment()
        }
    }

    private fun handleUri(uri: Uri, fromApp: Boolean) {
        if (!rootViewModel.processDeepLink(uri, fromApp)) {
            toast(R.string.wrong_url)
        }
    }

    private fun updateDialog() {
        add(UpdateFragment.newInstance())
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data ?: return

        handleUri(uri, intent.action == Intent.ACTION_SEND)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun setIntroFragment() {
        if (setPrimaryFragment(IntroFragment.newInstance())) {
            hideLockPassword()
        }
    }

    private fun setMainFragment() {
        if (setPrimaryFragment(MainFragment.newInstance()) && !Password.isUnlocked()) {
            showLockPassword()
        }
    }

    override fun openURL(url: String, external: Boolean) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        checkPasswordJob?.cancel()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (BuildConfig.DEBUG) {
            return
        }

        if (hasFocus) {
            baseContainer.visibility = View.VISIBLE
        } else {
            baseContainer.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (primaryFragment is MainFragment && !Password.isUnlocked()) {
            showLockPassword()
        }
    }
}