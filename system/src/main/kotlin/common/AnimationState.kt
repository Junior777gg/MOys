package common

import Animator

class AnimationState(val animator: Animator, val startTimeNano: Long) {
    var progress = 0.0f
    fun update(): Boolean{
        val currentTime = System.nanoTime()
        val duration = animator.durationMillis*1000000L
        progress = animator.easing.apply((currentTime.toFloat() - startTimeNano.toFloat()) / duration.toFloat())
        //progress = progress.coerceIn(0f, 1f)
        return (currentTime - startTimeNano) >= duration
    }
}