package com.tonapps.tonkeeper.ui.screen.stake.options

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.tonapps.uikit.list.BaseListHolder

abstract class StakeOptionsHolder<I : OptionItem>(
    parent: ViewGroup,
    @LayoutRes resId: Int,
) : BaseListHolder<I>(parent, resId)