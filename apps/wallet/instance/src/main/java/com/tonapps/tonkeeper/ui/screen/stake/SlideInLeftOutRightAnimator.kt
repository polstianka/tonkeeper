package com.tonapps.tonkeeper.ui.screen.stake

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.tonapps.tonkeeper.core.history.list.holder.HistoryActionHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonActionsHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonActionsStakedHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonHeaderHolder
import com.tonapps.tonkeeper.fragment.jetton.list.holder.JettonTabHolder

class SlideInLeftOutRightAnimator(private val onEnd: () -> Unit) : SimpleItemAnimator() {

    private val ignore = listOf(
        JettonHeaderHolder::class,
        JettonActionsHolder::class,
        JettonActionsStakedHolder::class,
        JettonTabHolder::class
    )

    private val toLeftEnter = listOf(
        HistoryActionHolder::class
    )

    override fun onAddFinished(item: RecyclerView.ViewHolder?) {
        onEnd()
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        if (holder::class in ignore) return false
        val width = holder.itemView.width.toFloat()
        holder.itemView.translationX = if (holder::class in toLeftEnter) -width else width
        val animator = holder.itemView.animate()
        animator.translationX(0f).setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    dispatchAddFinished(holder)
                }
            }).start()
        return true
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        if (holder::class in ignore) return false
        val animator = holder.itemView.animate()
        val width = holder.itemView.width.toFloat()
        animator.translationX(if (holder::class in toLeftEnter) -width else width).setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    dispatchRemoveFinished(holder)
                    holder.itemView.translationX = 0f
                }
            }).start()
        return true
    }

    override fun runPendingAnimations() {
        // No-op
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        if (item::class in ignore) return
        item.itemView.clearAnimation()
    }

    override fun endAnimations() {
        // No-op
    }

    override fun isRunning(): Boolean = false

    override fun animateMove(
        holder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        return false
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder?,
        newHolder: RecyclerView.ViewHolder?,
        fromLeft: Int,
        fromTop: Int,
        toLeft: Int,
        toTop: Int
    ): Boolean {
        return false
    }
}