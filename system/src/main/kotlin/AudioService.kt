import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

object AudioService: AudioServiceI {
    private lateinit var playerCoroutine : Job
    override fun setSound(path: String) {
        playerCoroutine = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
            val stream = AudioSystem.getAudioInputStream(File(path))

            // Конвертируем в PCM если нужно
            val baseFormat = stream.format
            val targetFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.sampleRate,
                16,
                baseFormat.channels,
                baseFormat.channels * 2,
                baseFormat.sampleRate,
                false
            )

            val converted = AudioSystem.getAudioInputStream(targetFormat, stream)
            val line = AudioSystem.getSourceDataLine(targetFormat)

            line.open(targetFormat)
            line.start()

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (converted.read(buffer).also { bytesRead = it } != -1) {
                line.write(buffer, 0, bytesRead)
            }
            if (coroutineContext.job.isCancelled){
                line.close()
                converted.close()
                stream.close()
            }
            line.drain()
            line.close()
            converted.close()
            stream.close()
        }
    }

    override fun play() {
        playerCoroutine.start()
    }

    override fun pause() {
    }

    override fun cancel() {
        playerCoroutine.cancel()
    }

    override fun setVolume(volume: Float) {

    }

    override fun getVolume(): Float {
        return 0.0f
    }
}