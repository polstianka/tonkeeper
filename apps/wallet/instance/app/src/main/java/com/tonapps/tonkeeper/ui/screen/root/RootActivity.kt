package com.tonapps.tonkeeper.ui.screen.root

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.biometric.BiometricPrompt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.tonapps.extensions.toUriOrNull
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.ui.screen.transaction.TransactionScreen
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.helper.BrowserHelper
import com.tonapps.tonkeeper.ui.base.BaseWalletActivity
import com.tonapps.tonkeeper.ui.base.QRCameraScreen
import com.tonapps.tonkeeper.ui.base.WalletFragmentFactory
import com.tonapps.tonkeeper.ui.screen.backup.main.BackupScreen
import com.tonapps.tonkeeper.ui.screen.battery.BatteryScreen
import com.tonapps.tonkeeper.ui.screen.browser.dapp.DAppScreen
import com.tonapps.tonkeeper.ui.screen.init.InitArgs
import com.tonapps.tonkeeper.ui.screen.init.InitScreen
import com.tonapps.tonkeeper.ui.screen.ledger.sign.LedgerSignScreen
import com.tonapps.tonkeeper.ui.screen.main.MainScreen
import com.tonapps.tonkeeper.ui.screen.purchase.main.PurchaseScreen
import com.tonapps.tonkeeper.ui.screen.purchase.web.PurchaseWebScreen
import com.tonapps.tonkeeper.ui.screen.send.main.SendScreen
import com.tonapps.tonkeeper.ui.screen.staking.stake.StakingScreen
import com.tonapps.tonkeeper.ui.screen.staking.viewer.StakeViewerScreen
import com.tonapps.tonkeeper.ui.screen.start.StartScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.passcode.PasscodeBiometric
import com.tonapps.wallet.data.passcode.ui.PasscodeView
import com.tonapps.wallet.data.rn.RNLegacy
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.dialog.alert.AlertDialog
import uikit.extensions.collectFlow
import uikit.extensions.findFragment
import uikit.navigation.Navigation.Companion.navigation

class RootActivity: BaseWalletActivity() {

    private var rootViewModel: Lazy<RootViewModel>? = null

    override val viewModel: RootViewModel
        get() = rootViewModel!!.value

    private val legacyRN: RNLegacy by inject()

    private lateinit var uiHandler: Handler

    private lateinit var lockView: View
    private lateinit var lockPasscodeView: PasscodeView
    private lateinit var lockSignOut: View

    override fun onCreate(savedInstanceState: Bundle?) {
        rootViewModel = viewModel<RootViewModel>()
        setTheme(viewModel.theme.resId)
        supportFragmentManager.fragmentFactory = WalletFragmentFactory()
        super.onCreate(savedInstanceState)
        legacyRN.setActivity(this)
        windowInsetsController.isAppearanceLightStatusBars = viewModel.theme.light
        windowInsetsController.isAppearanceLightNavigationBars = viewModel.theme.light
        uiHandler = Handler(mainLooper)

        handleIntent(intent)

        lockView = findViewById(R.id.lock)
        lockPasscodeView = findViewById(R.id.lock_passcode)
        lockPasscodeView.doOnCheck = ::checkPasscode

        lockSignOut = findViewById(R.id.lock_sign_out)
        lockSignOut.setOnClickListener { signOutAll() }

        ViewCompat.setOnApplyWindowInsetsListener(lockView) { _, insets ->
            val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            lockView.updatePadding(top = statusInsets.top, bottom = navInsets.bottom)
            insets
        }

        collectFlow(viewModel.hasWalletFlow) { init(it) }
        collectFlow(viewModel.eventFlow) { event(it) }
        collectFlow(viewModel.passcodeFlow, ::passcodeFlow)

        App.applyConfiguration(resources.configuration)
    }

    override fun isNeedRemoveModals(fragment: BaseFragment): Boolean {
        if (fragment is QRCameraScreen || fragment is LedgerSignScreen) {
            return false
        }
        return super.isNeedRemoveModals(fragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelStore.clear()
        rootViewModel = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        App.applyConfiguration(newConfig)
    }

    private fun passcodeFlow(config: RootViewModel.Passcode) {
        if (!config.show) {
            lockView.visibility = View.GONE
            return
        }
        lockView.visibility = View.VISIBLE
        if (config.biometric) {
            PasscodeBiometric.showPrompt(this, getString(Localization.app_name), object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    lockView.visibility = View.GONE
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    toast(Localization.authorization_required)
                }
            })
        }
    }

    private fun checkPasscode(code: String) {
        viewModel.checkPasscode(this, code).catch {
            lockPasscodeView.setError()
        }.onEach {
            lockPasscodeView.setSuccess()
        }.launchIn(lifecycleScope)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.activity_root)
    }

    fun event(event: RootEvent) {
        when (event) {
            is RootEvent.Toast -> toast(event.resId)
            is RootEvent.Singer -> add(InitScreen.newInstance(if (event.qr) InitArgs.Type.SignerQR else InitArgs.Type.Signer, event.publicKey, event.name))
            is RootEvent.Ledger -> add(InitScreen.newInstance(type = InitArgs.Type.Ledger, ledgerConnectData = event.connectData, accounts = event.accounts))
            is RootEvent.Browser -> add(DAppScreen.newInstance(event.wallet, url = event.uri))
            is RootEvent.Transfer -> openSend(
                targetAddress = event.address,
                tokenAddress = event.jettonAddress ?: TokenEntity.TON.address,
                amountNano = event.amount?.toLongOrNull() ?: 0L,
                text = event.text,
                wallet = event.wallet
            )
            is RootEvent.OpenSend -> openSend(event.wallet)
            is RootEvent.Transaction -> this.navigation?.add(TransactionScreen.newInstance(event.event))
            is RootEvent.BuyOrSell -> {
                if (event.method == null) {
                    add(PurchaseScreen.newInstance(event.wallet))
                } else {
                    PurchaseWebScreen.open(this, event.method)
                }
            }
            is RootEvent.OpenBackups -> add(BackupScreen.newInstance(event.wallet))
            is RootEvent.Staking -> add(StakingScreen.newInstance(event.wallet))
            is RootEvent.StakingPool -> add(StakeViewerScreen.newInstance(event.wallet, event.poolAddress, ""))
            is RootEvent.Battery -> add(BatteryScreen.newInstance(event.wallet, event.promocode))
            else -> { }
        }
    }

    private fun openSend(
        wallet: WalletEntity,
        targetAddress: String? = null,
        tokenAddress: String = TokenEntity.TON.address,
        amountNano: Long = 0,
        text: String? = null,
        nftAddress: String? = null
    ) {
        val fragment = supportFragmentManager.findFragment<SendScreen>()
        if (fragment == null) {
            add(
                SendScreen.newInstance(
                    wallet = wallet,
                    targetAddress = targetAddress,
                    tokenAddress = tokenAddress,
                    amountNano = amountNano,
                    text = text,
                    nftAddress = nftAddress,
                )
            )
        } else {
            fragment.initializeArgs(
                targetAddress = targetAddress,
                tokenAddress = tokenAddress,
                amountNano = amountNano,
                text = text,
            )
        }
    }

    private fun signOutAll() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(Localization.sign_out_all_title)
        builder.setMessage(Localization.sign_out_all_description)
        builder.setNegativeButton(Localization.sign_out) {
            viewModel.signOut()
            setIntroFragment()
        }
        builder.setPositiveButton(Localization.cancel)
        builder.show()
    }

    fun init(hasWallet: Boolean) {
        if (hasWallet) {
            setMainFragment()
        } else {
            setIntroFragment()
        }
    }

    private fun setIntroFragment() {
        setPrimaryFragment(StartScreen.newInstance())
    }

    private fun setMainFragment() {
        setPrimaryFragment(MainScreen.newInstance())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data
        val extras = intent.extras
        if (uri != null) {
            processDeepLink(uri)
        } else if (extras != null && !extras.isEmpty) {
            viewModel.processIntentExtras(extras)
        }
    }

    override fun openURL(url: String) {
        if (url.isBlank()) {
            return
        }
        val uri = url.toUriOrNull() ?: return
        if (uri.scheme == "tonkeeper" || uri.scheme == "ton" || uri.scheme == "tc" || uri.host == "app.tonkeeper.com") {
            processDeepLink(uri)
        } else {
            openExternalLink(uri)
        }
    }

    private fun openExternalLink(uri: Uri) {
        return if (uri.host == "t.me" || uri.scheme == "tg") {
            openTelegramLink(uri)
        } else if (uri.scheme == "mailto") {
            openEmail(uri)
        } else {
            openBrowser(uri)
        }
    }

    private fun openTelegramLink(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.`package` = "org.telegram.messenger"
        if (!safeStartActivity(intent)) {
            openBrowser(uri)
        }
    }

    private fun openBrowser(uri: Uri) {
        BrowserHelper.open(this, uri)
        // val intent = Intent(Intent.ACTION_VIEW, uri)
        // safeStartActivity(intent)
    }

    private fun openEmail(uri: Uri) {
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        safeStartActivity(intent)
    }

    private fun safeStartActivity(intent: Intent): Boolean {
        try {
            startActivity(intent)
            return true
        } catch (e: Throwable) {
            toast(Localization.unknown_error)
            return false
        }
    }

    private fun processDeepLink(uri: Uri) {
        if (!viewModel.processDeepLink(uri, false, getReferrer())) {
            openBrowser(uri)
        }
    }
}