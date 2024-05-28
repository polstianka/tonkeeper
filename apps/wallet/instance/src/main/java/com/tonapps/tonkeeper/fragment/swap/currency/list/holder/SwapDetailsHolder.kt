package com.tonapps.tonkeeper.fragment.swap.currency.list.holder

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem

abstract class SwapDetailsHolder<I: SwapDetailsItem>(
    parent: ViewGroup,
    @LayoutRes resId: Int
): com.tonapps.uikit.list.BaseListHolder<I>(parent, resId)