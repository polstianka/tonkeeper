package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat

open class StartTextHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : HeaderView(context, attrs, defStyle) {


    init {
        closeView.visibility = View.GONE

        val lp = (titleView.layoutParams as LinearLayoutCompat.LayoutParams).apply {
            gravity = Gravity.START
        }
        titleView.layoutParams = lp
        textView.setPadding(0,0,0,0)
    }

}