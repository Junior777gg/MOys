package audio

import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent

internal class ClipSound(
    private val clip: Clip,
    localVolume: Float,
    masterVolume: Float
) : Sound(localVolume, masterVolume) {
    private var paused = false
    private var pauseFrame = 0

    override val isAlive: Boolean
        get() = clip.isOpen

    init {
        clip.addLineListener { event ->
            if (event.type == LineEvent.Type.STOP && !paused) {
                onFinished?.invoke()
            }
        }
        applyGain()
    }

    override fun play() {
        clip.framePosition = 0
        clip.start()
    }
    override fun stop() {
        clip.stop()
        clip.close()
    }
    override fun pause() {
        if (paused) return
        paused = true
        pauseFrame = clip.framePosition
        clip.stop()
    }
    override fun resume() {
        if (!paused) return
        paused = false
        clip.framePosition = pauseFrame
        clip.start()
    }

    override fun updateGain(newMasterVolume: Float) {
        masterVolume = newMasterVolume
        applyGain()
    }
    private fun applyGain() {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return
        val control = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        control.value = calculateGain().coerceIn(control.minimum, control.maximum)
    }
}