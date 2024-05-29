package com.tonapps.tonkeeper.fragment.staking.deposit.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import uikit.extensions.dp

class StakeFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            if (ev.y < 64.dp) {
                return false
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}