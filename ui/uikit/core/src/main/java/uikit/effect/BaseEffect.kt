package uikit.effect

import android.view.View

abstract class BaseEffect(
    private vararg val targets: View?
) {
    val haveAttachedViews: Boolean
        get() {
            for (view in targets) {
                if (view != null && view.isAttachedToWindow) {
                    return true
                }
            }
            return false
        }
}