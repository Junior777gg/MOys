import common.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.SourceDataLine
import kotlin.math.log10
import kotlin.math.pow

object AudioService : AudioServiceI {
    private lateinit var playerCoroutine: Job

    private var currentPath = ""

    private var isPaused = false

    private var stream: AudioInputStream? = null
    private var convertedStream: AudioInputStream? = null
    private var line: SourceDataLine? = null

    private fun buildCoroutine(path: String) = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
        stream = AudioSystem.getAudioInputStream(File(path))

        try {
            val baseFormat = stream!!.format
            val targetFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.sampleRate,
                16,
                baseFormat.channels,
                baseFormat.channels * 2,
                baseFormat.sampleRate,
                false
            )

            convertedStream = AudioSystem.getAudioInputStream(targetFormat, stream)
            line = AudioSystem.getSourceDataLine(targetFormat)

            line!!.open(targetFormat)
            line!!.start()

            val buffer = ByteArray(65536)
            var bytesRead: Int
            while (convertedStream!!.read(buffer).also { bytesRead = it } != -1) {
                while (isPaused && coroutineContext.job.isActive) {
                    delay(100)
                }
                line!!.write(buffer, 0, bytesRead)
            }

            line!!.drain()
            line!!.close()
            convertedStream!!.close()
            stream!!.close()
        } catch (e: Exception) {
            Log.error(e.toString())
        } finally {
            convertedStream!!.close()
            stream!!.close()
            line!!.close()
            isPaused = false
        }
    }

    override fun setSound(path: String) {
        currentPath = path
        playerCoroutine = buildCoroutine(currentPath)
    }

    override fun play() {
        if (playerCoroutine.isCancelled || playerCoroutine.isCompleted) {
            playerCoroutine = buildCoroutine(currentPath)
        }
        playerCoroutine.start()
    }

    override fun pause() {
        isPaused = !isPaused
        if (isPaused) {
            line!!.stop()
        } else {
            line!!.start()
        }
    }

    override fun cancel() {
        stream?.close()
        convertedStream?.close()
        line?.close()
        playerCoroutine.cancel()
    }

    override fun setVolume(volume: Float) {
        line!!.let {
            val control = it.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            val db = (log10(volume.coerceIn(0.0001f, 1f).toDouble()) * 20).toFloat()
            control.value = db.coerceIn(control.minimum, control.maximum)
        }
    }

    override fun getVolume(): Float {
        line?.let {
            val control = it.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            return 10.0.pow(control.value / 20.0).toFloat()
        }
        return 1f
    }
}
