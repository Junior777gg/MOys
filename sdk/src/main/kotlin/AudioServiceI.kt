interface AudioServiceI {
    fun setSound(path: String)
    fun play()
    fun pause()
    fun cancel()
    fun setVolume(volume: Float)
    fun getVolume(): Float
}
