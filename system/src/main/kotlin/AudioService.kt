import common.Log
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent
import javax.sound.sampled.UnsupportedAudioFileException
import kotlin.math.log10

//Service for playing audio in the "main" channel.
class AudioService : AudioServiceI {
    private var masterVolume=0.5f
    private var globalSoundID=0L
    private val soundPool=mutableMapOf<String,Sound>()

    /**Returns current system master volume.*/
    override fun getVolume(): Float {
        return masterVolume
    }
    /**Sets current system master volume to [newVolume].*/
    override fun setVolume(newVolume: Float) {
        masterVolume=newVolume.coerceIn(0.0f,1.0f)
        synchronized(soundPool) {
            cleanupFinished()
            for (sound in soundPool.values) applyGain(sound)
        }
    }

    /**
     * Plays given audio file on master channel.
     * Supported formats: OGG, MP3. (WAV and AU are supported but may be buggy.)
    */
    override fun playFile(path: String, volume: Float): SoundHandle? {
        val file = File(path)
        if(!file.exists()) {
            Log.error("Sound file not found: \"$path\"")
            return null
        }
        return try {
            //Load the sound.
            val sound = loadAndPlay(file, volume.coerceIn(0.0f, 1.0f))
                ?: return null
            //Add sound instance to pool.
            val id: String
            synchronized(soundPool) {
                id = "sound_${globalSoundID++}"
                soundPool[id] = sound
            }
            //Create new sound handle and attach sound to it.
            val handle = SoundHandle(id)
            //Remove on playback finish.
            sound.clip.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP && !sound.isPaused()) {
                    synchronized(soundPool) {
                        soundPool.remove(handle.id)?.stop()
                    }
                }
            }
            //Start playback and return handle.
            sound.clip.start()
            handle
        } catch (e: Exception) {
            Log.error("Failed to play sound from \"$path\": ${e.toString()}")
            null
        }
    }
    /**Internal function to load to play sound from [file].*/
    private fun loadAndPlay(file: File, localVolume: Float): Sound? {
        return try {
            //Get audio stream.
            val rawStream = AudioSystem.getAudioInputStream(file)
            val rawFormat = rawStream.format
            //If not using PCM: MP3→MPEG1L3, OGG→VORBIS, etc.
            val decodedStream = if (rawFormat.encoding != AudioFormat.Encoding.PCM_SIGNED) {
                val pcmFormat = AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    rawFormat.sampleRate,
                    16, //Bit resolution.
                    rawFormat.channels,
                    rawFormat.channels * 2, //Frame size.
                    rawFormat.sampleRate,
                    false //Little-endian.
                )
                AudioSystem.getAudioInputStream(pcmFormat, rawStream)
            } else {
                rawStream
            }
            //Open clip handle.
            val clip = AudioSystem.getClip()
            clip.open(decodedStream)
            //Create sound and apply gain.
            val sound = Sound(
                clip = clip,
                gain = localVolume
            )
            applyGain(sound)

            sound
        } catch (e: UnsupportedAudioFileException) {
            Log.error("Unsupported audio format \"${file.extension}\": $e")
            null
        } catch (e: Exception) {
            Log.error("Failed to load sound clip from \"${file.name}\": $e")
            null
        }
    }
    /**Fully stops playback of [sound] and destroys it.*/
    override fun stop(sound: SoundHandle) {
        synchronized(soundPool) {
            soundPool.remove(sound.id)?.stop()
        }
    }
    /**Pauses playback of [sound].*/
    override fun pause(sound: SoundHandle) {}
    /**Resumes playback of [sound].*/
    override fun resume(sound: SoundHandle) {}

    /**Pauses playback of all sound in master channel.*/
    override fun pauseAll() {}
    /**Resumes playback of all sound in master channel.*/
    override fun resumeAll() {}

    /**Applies dB to [sound].*/
    private fun applyGain(sound: Sound) {
        val clip = sound.clip
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return
        //Create volume control.
        val control = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        val gainDb = volumeToGain(sound.gain * masterVolume)
        //Limit gain.
        control.value = gainDb.coerceIn(control.minimum, control.maximum)
    }

    /**Simple conversion of float to dB.*/
    private fun volumeToGain(linear: Float): Float {
        val clamped = linear.toDouble().coerceIn(0.0001, 1.0)
        return (20.0 * log10(clamped)).toFloat()
    }
    /**Remove played or stopped sounds.*/
    private fun cleanupFinished() {
        val dead = soundPool.entries.filter { !it.value.isAlive() }
        for (entry in dead) {
            entry.value.stop()
            soundPool.remove(entry.key)
        }
    }
}

private class Sound(
    val clip: Clip,
    val gain: Float
) {
    private var paused: Boolean = false
    /**Position of playback frame.*/
    private var pauseFrame: Int = 0

    /**Is sound still playing or can be played.*/
    fun isAlive(): Boolean {
        return clip.isOpen
    }
    /**Is the sound paused? (This doesn't determine whether sound is alive or not.)*/
    fun isPaused(): Boolean {
        return paused
    }

    fun pause() {
        if (paused || !clip.isRunning) return
        paused = true
        pauseFrame = clip.framePosition
        clip.stop()
    }
    fun resume() {
        if (!paused) return
        paused = false
        clip.framePosition = pauseFrame
        clip.start()
    }
    fun stop() {
        try {
            clip.stop()
            clip.close()
        } catch (ex: Exception) {
            Log.warn("Couldn't close sound: ${ex.toString()}")
        }
    }
}