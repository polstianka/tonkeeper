package uikit.extensions

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

inline fun <reified F> FragmentManager.findFragment(): F? {
    val f = fragments.find { it is F }
    return f as? F
}

inline fun <reified F> FragmentManager.isFragmentExists(): Boolean {
    return findFragment<F>() != null
}


fun FragmentManager.findFragments(id: Int): List<Fragment> {
    return fragments.filter { fragment ->
        val parent = fragment.view?.parent as? View ?: return@filter false
        parent.id == id
    }
}

inline fun FragmentManager.commitChildAsSlide(
    allowStateLoss: Boolean = false,
    body: FragmentTransaction.() -> Unit
) {
    val transaction = beginTransaction()
    transaction.setCustomAnimations(uikit.R.anim.fragment_enter_from_right, uikit.R.anim.fragment_exit_to_left, uikit.R.anim.fragment_enter_from_left, uikit.R.anim.fragment_exit_to_right)
    transaction.body()
    if (allowStateLoss) {
        transaction.commitAllowingStateLoss()
    } else {
        transaction.commit()
    }
}
