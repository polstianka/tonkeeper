package com.tonapps.tonkeeper.extensions

import androidx.fragment.app.Fragment

inline fun <reified T: Fragment>Fragment.findParent(): T {
    var f = this
    while (true) {
        if (f is T) {
            return f
        } else {
            f = f.requireParentFragment()
        }
    }
}