package audio

import AudioPlayerI
import SoundHandle
import common.Log
import java.io.File
import javax.sound.sampled.AudioSystem

//Service for playing audio in the "main" channel.
class AudioPlayer : AudioPlayerI {
    private var masterVolume=0.5f
    private var globalSoundID=0L
    private val soundPool=mutableMapOf<String, Sound>()

    /**Returns current system master volume.*/
    override fun getVolume(): Float {
        return masterVolume
    }
    /**Sets current system master volume to [newVolume].*/
    override fun setVolume(newVolume: Float) {
        masterVolume=newVolume.coerceIn(0.0f,1.0f)
        synchronized(soundPool) {
            cleanupFinished()
            for (sound in soundPool.values) sound.updateGain(masterVolume)
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
        val localVolume = volume.coerceIn(0.0f, 1.0f)
        return try {
            //Load the sound.
            val extension = file.extension.lowercase()
            val sound: Sound? = when (extension) {
                "wav", "au", "aiff" -> loadClipSound(file, localVolume)
                "mp3" -> loadMp3Sound(file, localVolume)
                "ogg" -> loadOggSound(file, localVolume)
                else -> {
                    Log.error("Unsupported audio format: \"$extension\"")
                    null
                }
            }

            if (sound == null) return null
            //Add sound instance to pool.
            val id: String
            synchronized(soundPool) {
                id = "sound_${globalSoundID++}"
                soundPool[id] = sound
            }
            //Create new sound handle and attach sound to it.
            val handle = SoundHandle(id)
            //Remove on playback finish.
            sound.onFinished = {
                synchronized(soundPool) {
                    soundPool.remove(id)
                }
            }
            //Start playback and return handle.
            sound.play()
            handle
        } catch (e: Exception) {
            Log.error("Failed to play sound from \"$path\": ${e.toString()}")
            null
        }
    }
    private fun loadClipSound(file: File, localVolume: Float): Sound? {
        return try {
            val stream = AudioSystem.getAudioInputStream(file)
            val clip = AudioSystem.getClip()
            clip.open(stream)

            ClipSound(clip, localVolume, masterVolume)
        } catch (e: Exception) {
            Log.error("Failed to load clip: $e")
            null
        }
    }
    private fun loadMp3Sound(file: File, localVolume: Float): Sound? {
        return try {
            Mp3Sound(file, localVolume, masterVolume)
        } catch (e: Exception) {
            Log.error("Failed to load MP3: $e")
            null
        }
    }
    private fun loadOggSound(file: File, localVolume: Float): Sound? {
        return try {
            OggSound(file, localVolume, masterVolume)
        } catch (e: Exception) {
            Log.error("Failed to load OGG: $e")
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
    override fun pause(sound: SoundHandle) {
        synchronized(soundPool) {
            soundPool[sound.id]?.pause()
        }
    }
    /**Resumes playback of [sound].*/
    override fun resume(sound: SoundHandle) {
        synchronized(soundPool) {
            soundPool[sound.id]?.resume()
        }
    }

    /**Pauses playback of all sound in master channel.*/
    override fun pauseAll() {
        synchronized(soundPool) {
            soundPool.values.forEach { it.pause() }
        }
    }
    /**Resumes playback of all sound in master channel.*/
    override fun resumeAll() {
        synchronized(soundPool) {
            soundPool.values.forEach { it.resume() }
        }
    }

    /**Remove played or stopped sounds.*/
    private fun cleanupFinished() {
        val dead = soundPool.filter { !it.value.isAlive }
        dead.keys.forEach { soundPool.remove(it) }
    }
}