package uikit.animator

import android.animation.TimeInterpolator

class BoolAnimator (
    val duration: Long,
    val interpolator: TimeInterpolator,
    initialValue: Boolean = false,
    onApplyValue: (state: State, animatedValue: Float, stateChanged: Boolean, prevState: State) -> Unit) {

    enum class State {
        FALSE,
        TRUE,
        INTERMEDIATE
    }

    private val animated = FloatAnimator(duration, interpolator, if (initialValue) 1.0f else 0.0f) {
        val state = when (it) {
            0.0f -> State.FALSE
            1.0f -> State.TRUE
            else -> State.INTERMEDIATE
        }
        val oldState = this.state
        this.state = state
        onApplyValue(state, it, oldState != state, oldState)
    }

    var state: State = if (initialValue) State.TRUE else State.FALSE
        private set

    var value: Boolean = initialValue
        private set
    var forcedValue: Boolean
        get() = value
        set(newValue) = changeValue(newValue, false)
    var animatedValue: Boolean
        get() = value
        set(newValue) = changeValue(newValue, true)

    val floatValue: Float
        get() = animated.value

    @JvmOverloads
    fun changeValue(newValue: Boolean, animated: Boolean = true) {
        if (this.value != newValue || !animated) {
            this.animated.stopAnimation()
            value = newValue
            val newFloatValue = if (newValue) 1.0f else 0.0f
            this.animated.changeValue(newFloatValue, animated)
        }
    }
}