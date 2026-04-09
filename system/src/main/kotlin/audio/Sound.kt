package audio

import kotlin.math.log10

internal abstract class Sound(
    protected var localVolume: Float,
    protected var masterVolume: Float
) {
    /**Is the sound paused? (This doesn't determine whether sound is alive or not.)*/
    abstract val isAlive: Boolean
    var onFinished: (() -> Unit)? = null

    abstract fun play()
    abstract fun stop()
    abstract fun pause()
    abstract fun resume()
    abstract fun updateGain(newMasterVolume: Float)

    protected fun calculateGain(): Float {
        val effective = (localVolume * masterVolume).toDouble().coerceIn(0.0001, 1.0)
        return (20.0 * log10(effective)).toFloat()
    }
}