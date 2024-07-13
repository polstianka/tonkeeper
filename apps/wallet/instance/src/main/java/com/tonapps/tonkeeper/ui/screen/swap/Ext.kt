package com.tonapps.tonkeeper.ui.screen.swap

import android.animation.Animator
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import uikit.extensions.scale

fun View.switchToAnimated(viewTo: View) {
    this.animate().cancel()
    viewTo.animate().cancel()
    this.isVisible = true
    this.alpha = 1f
    this.scale = 1f
    this.animate().alpha(0f).scale(0.2f).setDuration(150).start()
    viewTo.isVisible = true
    viewTo.alpha = 0f
    viewTo.scale = 0.2f
    viewTo.animate().alpha(1f).scale(1f).setDuration(150).start()
}

fun viewSwitcher(view1: View, view2: View, showView1: Boolean) {
    if (showView1) {
        view2.switchToAnimated(view1)
    } else {
        view1.switchToAnimated(view2)
    }
}

fun View.visibilityHeightAnimated(show: Boolean, maxHeight: Float) {
    if (show && this.visibility != View.VISIBLE && this.tag == null) {
        this.tag = true
        this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            height = 1
        }
        this.visibility = View.VISIBLE
        this.animate().setUpdateListener(null).setListener(null).cancel()
        this.animate().setUpdateListener {
            val progress = it.animatedFraction
            this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height = (maxHeight * progress).toInt().coerceAtLeast(1)
            }
        }.setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) = Unit
            override fun onAnimationRepeat(p0: Animator) = Unit
            override fun onAnimationCancel(p0: Animator) {
                this@visibilityHeightAnimated.tag = null
            }

            override fun onAnimationEnd(p0: Animator) {
                this@visibilityHeightAnimated.tag = null
            }
        }).setDuration(200).start()
    } else if (!show && this.visibility != View.GONE && this.tag == null) {
        this.tag = true
        this.animate().setUpdateListener(null).setListener(null).cancel()
        this.animate().setUpdateListener {
            val progress = it.animatedFraction
            this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height = (maxHeight * (1f - progress)).toInt().coerceAtLeast(1)
            }
        }.setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) = Unit
            override fun onAnimationRepeat(p0: Animator) = Unit
            override fun onAnimationCancel(p0: Animator) {
                this@visibilityHeightAnimated.tag = null
            }

            override fun onAnimationEnd(p0: Animator) {
                this@visibilityHeightAnimated.tag = null
                this@visibilityHeightAnimated.visibility = View.GONE
            }
        }).setDuration(200).start()
    }
}