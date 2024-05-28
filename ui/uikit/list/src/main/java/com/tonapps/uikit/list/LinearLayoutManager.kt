package com.tonapps.uikit.list

import android.content.Context
import androidx.recyclerview.widget.RecyclerView

open class LinearLayoutManager @JvmOverloads constructor(
    context: Context,
    orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false,
    private val canScrollVertically: Boolean = true,
): androidx.recyclerview.widget.LinearLayoutManager(context, orientation, reverseLayout) {

    override fun supportsPredictiveItemAnimations(): Boolean = false

    override fun canScrollVertically(): Boolean = canScrollVertically
}