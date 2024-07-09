package com.tonapps.uikit.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListHolder<I: BaseListItem>(
    view: View
): RecyclerView.ViewHolder(view) {

    constructor(
        parent: ViewGroup,
        @LayoutRes resId: Int
    ) : this(
        LayoutInflater.from(parent.context).inflate(resId, parent, false)
    )

    val context: Context
        get() = itemView.context

    var item: I? = null
        private set

    fun <V : View> findViewById(id: Int): V = itemView.findViewById<V>(id)

    @CallSuper
    open fun bind(item: BaseListItem) {
        this.item = item as I
        onBind(item)
    }

    abstract fun onBind(item: I)


    fun unbind() {
        item = null
    }

    @CallSuper
    open fun onUnbind() {

    }

    fun getString(resId: Int, vararg args: Any): String = context.getString(resId, *args)

    @ColorInt
    fun getColor(resId: Int): Int = context.getColor(resId)

}
