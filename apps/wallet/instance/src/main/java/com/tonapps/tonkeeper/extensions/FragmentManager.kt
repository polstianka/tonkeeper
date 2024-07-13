package com.tonapps.tonkeeper.extensions

import androidx.fragment.app.FragmentManager

fun FragmentManager.removeAllFragments() {
    val transaction = beginTransaction()
    for (fragment in fragments) {
        transaction.remove(fragment)
    }
    transaction.commitNow()
}