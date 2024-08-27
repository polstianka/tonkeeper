package com.tonapps.tonkeeper.ui.screen.battery.refill.list.holder

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.Item
import com.tonapps.uikit.list.BaseListHolder
import uikit.navigation.Navigation

abstract class Holder<I: Item>(
    parent: ViewGroup,
    @LayoutRes resId: Int,
): BaseListHolder<I>(parent, resId) {

    val navigation = Navigation.from(context)
}