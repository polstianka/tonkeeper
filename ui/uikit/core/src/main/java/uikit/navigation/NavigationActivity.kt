package uikit.navigation

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import uikit.R
import uikit.base.BaseActivity
import uikit.base.BaseFragment
import uikit.extensions.primaryFragment
import uikit.extensions.roundTop
import uikit.extensions.scale
import uikit.widget.ToastView

abstract class NavigationActivity : BaseActivity(), Navigation, ViewTreeObserver.OnPreDrawListener {

    companion object {
        val hostFragmentId = R.id.root_container
        val hostSheetId = R.id.sheet_container
    }

    open val isInitialized: Boolean
        get() = supportFragmentManager.fragments.isNotEmpty()

    private val modals: List<BaseFragment.Modal>
        get() = supportFragmentManager.fragments.filterIsInstance<BaseFragment.Modal>()

    private lateinit var baseView: View
    private lateinit var contentView: View
    private lateinit var toastView: ToastView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        enableEdgeToEdge()
        setContentView(R.layout.activity_navigation)

        baseView = findViewById(R.id.base)

        contentView = findViewById(android.R.id.content)
        contentView.viewTreeObserver.addOnPreDrawListener(this)

        toastView = findViewById(R.id.toast)

        val callback = object : OnBackPressedCallback(true) {

            private val target: BaseFragment.PredictiveBackGesture?
                get() = supportFragmentManager.fragments.lastOrNull() as? BaseFragment.PredictiveBackGesture

            override fun handleOnBackCancelled() {
                super.handleOnBackCancelled()
                target?.onPredictiveBackCancelled()
            }

            override fun handleOnBackProgressed(backEvent: BackEventCompat) {
                super.handleOnBackProgressed(backEvent)
                target?.onPredictiveBackProgressed(backEvent)
            }

            override fun handleOnBackStarted(backEvent: BackEventCompat) {
                super.handleOnBackStarted(backEvent)
                target?.onPredictiveOnBackStarted(backEvent)
            }

            override fun handleOnBackPressed() {
                onBackPress()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun onBackPress() {
        val fragment = supportFragmentManager.fragments.lastOrNull() as? BaseFragment ?: return
        if (fragment.onBackPressed()) {
            remove(fragment)
        }
    }

    override fun onPreDraw(): Boolean {
        if (isInitialized) {
            contentView.viewTreeObserver.removeOnPreDrawListener(this)
            contentView.setBackgroundColor(Color.BLACK)
            return true
        }
        return false
    }

    override fun setFragmentResult(requestKey: String, result: Bundle) {
        supportFragmentManager.setFragmentResult(requestKey, result)
    }

    override fun setFragmentResultListener(
        requestKey: String,
        listener: (bundle: Bundle) -> Unit
    ) {
        supportFragmentManager.setFragmentResultListener(requestKey, this) { _, b ->
            listener(b)
        }
    }

    fun setPrimaryFragment(
        fragment: BaseFragment,
        recreate: Boolean = false,
        runnable: Runnable? = null
    ): Boolean {
        if (primaryFragment?.javaClass == fragment.javaClass && !recreate) {
            return false
        }

        if (recreate) {
            clearBackStack()
        }

        val transaction = supportFragmentManager.beginTransaction()
        if (recreate) {
            supportFragmentManager.fragments.forEach { transaction.remove(it) }
        }
        transaction.replace(hostFragmentId, fragment)
        transaction.setPrimaryNavigationFragment(fragment)
        runnable?.let {
            transaction.runOnCommit(it)
        }
        transaction.commitAllowingStateLoss()
        return true
    }

    override fun add(fragment: BaseFragment) {
        val removeModals = fragment !is BaseFragment.Modal
        val transaction = supportFragmentManager.beginTransaction()
        if (fragment is BaseFragment.BottomSheet || fragment is BaseFragment.Modal || fragment is BaseFragment.SwipeBack) {
            transaction.add(hostSheetId, fragment)
        } else {
            transaction.add(hostFragmentId, fragment)
        }
        if (removeModals) {
            dismissModals(transaction)
        }
        transaction.commitAllowingStateLoss()
    }

    override fun remove(fragment: Fragment) {
        if (supportFragmentManager.primaryNavigationFragment == fragment) {
            finish()
        } else {
            supportFragmentManager.commit {
                remove(fragment)
            }
        }
    }

    /**
     * I know that that's wrong doing it but I found the only way to clear backstack
     * and restore root view properties.
     * It's needed when I don't wanna use ViewPager to clear all fragments
     */
    override fun finishAll() {
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach {
            if (supportFragmentManager.primaryNavigationFragment != it) {
                findViewById<View>(R.id.root_container)?.let { view ->
                    view.roundTop(0)
                    view.scale = 1f
                    view.alpha = 1f
                }
                transaction.remove(it)
            }
        }
        transaction.commitAllowingStateLoss()
    }

    private fun clearBackStack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    private fun dismissModals(transaction: FragmentTransaction) {
        val currentModals = modals
        if (currentModals.isEmpty()) {
            return
        }
        for (modal in currentModals) {
            transaction.remove(modal as Fragment)
        }
    }

    override fun toast(
        message: String,
        loading: Boolean,
        color: Int
    ) {
        toastView.show(message, loading, color)
    }
}