package com.tonapps.tonkeeper.ui.screen.root

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.biometric.BiometricPrompt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.ui.screen.transaction.TransactionScreen
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.fragment.tonconnect.auth.TCAuthFragment
import com.tonapps.tonkeeper.ui.screen.backup.main.BackupScreen
import com.tonapps.tonkeeper.ui.screen.battery.BatteryScreen
import com.tonapps.tonkeeper.ui.screen.init.InitArgs
import com.tonapps.tonkeeper.ui.screen.init.InitScreen
import com.tonapps.tonkeeper.ui.screen.main.MainScreen
import com.tonapps.tonkeeper.ui.screen.notifications.enable.NotificationsEnableScreen
import com.tonapps.tonkeeper.ui.screen.purchase.main.PurchaseScreen
import com.tonapps.tonkeeper.ui.screen.purchase.web.PurchaseWebScreen
import com.tonapps.tonkeeper.ui.screen.send.main.SendScreen
import com.tonapps.tonkeeper.ui.screen.staking.stake.StakingScreen
import com.tonapps.tonkeeper.ui.screen.staking.viewer.StakeViewerScreen
import com.tonapps.tonkeeper.ui.screen.start.StartScreen
import com.tonapps.tonkeeper.ui.screen.web.WebScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import com.tonapps.wallet.data.passcode.PasscodeBiometric
import com.tonapps.wallet.data.passcode.ui.PasscodeView
import com.tonapps.wallet.data.rn.RNLegacy
import com.tonapps.wallet.data.tonconnect.entities.DAppEventEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.dialog.alert.AlertDialog
import uikit.extensions.activity
import uikit.extensions.collectFlow
import uikit.extensions.findFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.navigation.NavigationActivity

class RootActivity: NavigationActivity() {

    private val rootViewModel: RootViewModel by viewModel()
    private val legacyRN: RNLegacy by inject()

    private lateinit var uiHandler: Handler

    private lateinit var lockView: View
    private lateinit var lockPasscodeView: PasscodeView
    private lateinit var lockSignOut: View

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(rootViewModel.theme.resId)
        super.onCreate(savedInstanceState)
        legacyRN.setActivity(this)
        windowInsetsController.isAppearanceLightStatusBars = rootViewModel.theme.light
        windowInsetsController.isAppearanceLightNavigationBars = rootViewModel.theme.light
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

        collectFlow(rootViewModel.tonConnectEventsFlow, ::onDAppEvent)
        collectFlow(rootViewModel.hasWalletFlow) { init(it) }
        collectFlow(rootViewModel.eventFlow) { event(it) }
        collectFlow(rootViewModel.passcodeFlow, ::passcodeFlow)

        collectFlow(rootViewModel.themeFlow) {
            recreate()
        }
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

    private suspend fun onDAppEvent(event: DAppEventEntity) {
        if (event.method != "sendTransaction") {
            return
        }

        val params = event.params
        for (i in 0 until params.length()) {
            val param = DAppEventEntity.parseParam(params.get(i))
            val request = SignRequestEntity(param)
            try {
                val boc = rootViewModel.requestSign(this, event.wallet, request)
                rootViewModel.tonconnectBoc(event.id, event.connect, boc)
            } catch (e: Throwable) {
                rootViewModel.tonconnectReject(event.id, event.connect)
            }
        }
    }

    private fun checkPasscode(code: String) {
        rootViewModel.checkPasscode(this, code).catch {
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
            is RootEvent.TonConnect -> add(TCAuthFragment.newInstance(event.request))
            is RootEvent.Browser -> add(WebScreen.newInstance(event.uri))
            is RootEvent.Transfer -> openSend(
                targetAddress = event.address,
                tokenAddress = event.jettonAddress ?: TokenEntity.TON.address,
                amountNano = event.amount?.toLongOrNull() ?: 0L,
                text = event.text
            )
            is RootEvent.Transaction -> this.navigation?.add(TransactionScreen.newInstance(event.event))
            is RootEvent.BuyOrSell -> {
                if (event.methodEntity == null) {
                    add(PurchaseScreen.newInstance())
                } else {
                    add(PurchaseWebScreen.newInstance(event.methodEntity))
                }
            }
            is RootEvent.OpenBackups -> add(BackupScreen.newInstance())
            is RootEvent.Staking -> add(StakingScreen.newInstance())
            is RootEvent.StakingPool -> add(StakeViewerScreen.newInstance(event.poolAddress, ""))
            is RootEvent.Battery -> add(BatteryScreen.newInstance(event.promocode))
            else -> { }
        }
    }

    private fun openSend(
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
                targetAddress = targetAddress,
                tokenAddress = tokenAddress,
                amountNano = amountNano,
                text = text,
                nftAddress = nftAddress,
            ))
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
            rootViewModel.signOut()
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
        val uri = intent.data ?: return
        processDeepLink(uri)
    }

    override fun openURL(url: String, external: Boolean) {
        if (url.isBlank()) {
            return
        }

        val uri = Uri.parse(url)

        if (external) {
            val action = if (url.startsWith("mailto:")) {
                Intent.ACTION_SENDTO
            } else {
                Intent.ACTION_VIEW
            }
            val intent = Intent(action, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            processDeepLink(uri)
        }
    }

    private fun processDeepLink(uri: Uri) {
        rootViewModel.processDeepLink(uri, false, getReferrer())
    }
}