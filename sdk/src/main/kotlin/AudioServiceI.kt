interface AudioServiceI {
    fun getVolume(): Float
    fun setVolume(v: Float)

    fun playFile(path: String, volume: Float=1.0f): SoundHandle?
    fun stop(sound: SoundHandle)
    fun pause(sound: SoundHandle)
    fun resume(sound: SoundHandle)

    fun pauseAll()
    fun resumeAll()
}
data class SoundHandle(val id: String)