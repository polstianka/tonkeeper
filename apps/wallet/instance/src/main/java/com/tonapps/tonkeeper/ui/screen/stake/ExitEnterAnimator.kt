package com.tonapps.tonkeeper.ui.screen.stake

import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tonapps.tonkeeper.core.history.list.holder.HistoryActionHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartActionsStakedHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartHeaderHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartLineHolder
import com.tonapps.tonkeeper.fragment.chart.list.holder.ChartPriceHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonActionsHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonHeaderHolder

class ExitEnterAnimator : DefaultItemAnimator() {
    private val pending = ArrayList<Anim>()
    private val additions = HashMap<ViewHolder, Anim>()

    private val ignore = listOf(
        JettonHeaderHolder::class,
        JettonActionsHolder::class,
        ChartHeaderHolder::class,
        ChartActionsStakedHolder::class,
        ChartPriceHolder::class,
        ChartLineHolder::class
    )

    private val toLeftEnter = listOf(
        HistoryActionHolder::class
    )

    override fun animateAdd(holder: ViewHolder): Boolean {
        if (holder::class in ignore) return false

        val anim = if (holder::class in toLeftEnter) AddRightToLeftHolder(holder, false)
        else AddLeftToRightHolder(holder, false)

        pending.add(anim)
        return true
    }

    override fun animateRemove(holder: ViewHolder): Boolean {
        if (holder::class in ignore) return false

        val anim = if (holder::class in toLeftEnter) AddRightToLeftHolder(holder, true)
        else AddLeftToRightHolder(holder, true)

        pending.add(anim)
        return true
    }

    override fun runPendingAnimations() {
        for (ah in pending) {
            ah.start()
        }
        pending.clear()
        super.runPendingAnimations()
    }

    override fun endAnimation(item: ViewHolder) {
        val ah = additions[item]
        ah?.endAnimation()
        super.endAnimation(item)
    }

    override fun endAnimations() {
        for (ah in pending) {
            ah.resetViewHolderState()
            dispatchAddFinished(ah.holder)
        }
        for (ah in additions.values) {
            ah.resetViewHolderState()
            dispatchAddFinished(ah.holder)
        }
        pending.clear()
        additions.clear()
        super.endAnimations()
    }

    override fun isRunning(): Boolean {
        return super.isRunning() && pending.isNotEmpty() && additions.isNotEmpty()
    }

    interface Anim {
        fun start()
        fun resetViewHolderState()
        fun endAnimation()

        val holder: ViewHolder
    }

    private inner class AddLeftToRightHolder(override val holder: ViewHolder, remove: Boolean) :
        Animation.AnimationListener, Anim {
        init {
            val from = if (remove) 0f else 1f
            val to = if (remove) 1f else 0f
            val anim = TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, from, Animation.RELATIVE_TO_PARENT, to,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
            ).apply {
                duration = 300
                setAnimationListener(this@AddLeftToRightHolder)
            }
            holder.itemView.animation = anim
            dispatchAddStarting(holder)
        }

        override fun start() {
            val itemView = holder.itemView
            val a = itemView.animation
            if (a != null) {
                a.start()
                additions[holder] = this
            } else {
                endAnimation()
            }
        }

        override fun resetViewHolderState() {
            val a = holder.itemView.animation
            if (a != null) {
                a.setAnimationListener(null)
                a.cancel()
                holder.itemView.clearAnimation()
            }
            holder.itemView.translationX = 0f
        }

        override fun endAnimation() {
            additions.remove(holder)
            resetViewHolderState()
            dispatchAddFinished(holder)
            if (!isRunning) dispatchAnimationsFinished()
        }

        override fun onAnimationStart(animation: Animation) {
        }

        override fun onAnimationEnd(animation: Animation) {
            endAnimation()
        }

        override fun onAnimationRepeat(animation: Animation) {
        }
    }

    private inner class AddRightToLeftHolder(
        override val holder: ViewHolder,
        private val remove: Boolean
    ) :
        Animation.AnimationListener, Anim {
        init {
            val from = if (remove) 0f else -1f
            val to = if (remove) -1f else 0f
            val anim = TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, from, Animation.RELATIVE_TO_PARENT, to,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
            ).apply {
                duration = 300
                setAnimationListener(this@AddRightToLeftHolder)
            }
            holder.itemView.animation = anim
            dispatchAddStarting(holder)
        }

        override fun start() {
            val itemView = holder.itemView
            val a = itemView.animation
            if (a != null) {
                a.start()
                additions[holder] = this
            } else {
                endAnimation()
            }
        }

        override fun resetViewHolderState() {
            val a = holder.itemView.animation
            if (a != null) {
                a.setAnimationListener(null)
                a.cancel()
                holder.itemView.clearAnimation()
            }
            holder.itemView.translationX = 0f
        }

        override fun endAnimation() {
            additions.remove(holder)
            resetViewHolderState()
            dispatchAddFinished(holder)
            if (!isRunning) dispatchAnimationsFinished()
        }

        override fun onAnimationStart(animation: Animation) {
        }

        override fun onAnimationEnd(animation: Animation) {
            endAnimation()
        }

        override fun onAnimationRepeat(animation: Animation) {
        }
    }
}