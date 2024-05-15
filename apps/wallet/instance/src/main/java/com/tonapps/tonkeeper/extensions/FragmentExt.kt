package com.tonapps.tonkeeper.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.tonapps.tonkeeper.fragment.stake.root.StakeFragment
import uikit.base.BaseFragment
import kotlin.reflect.KClass

fun <T: BaseFragment> Fragment.popBackToRootFragment(
    includingRoot: Boolean = false,
    rootClass: KClass<T>
) {

    val fragmentManager = requireActivity().supportFragmentManager
    fragmentManager.commit {
        val iterator = fragmentManager.fragments.iterator()
        var visitedRoot = false
        val toRemove = mutableListOf<Fragment>()
        while (iterator.hasNext()) {
            val current = iterator.next()
            if (visitedRoot) {
                if (iterator.hasNext()) {
                    toRemove.add(current)
                }
            } else {
                if (rootClass.isInstance(current)) {
                    visitedRoot = true
                    if (includingRoot) {
                        toRemove.add(current)
                    }
                }
            }
        }
        toRemove.forEach { remove(it) }
    }
}