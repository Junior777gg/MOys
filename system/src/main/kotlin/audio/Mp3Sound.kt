package audio

import common.Log
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Decoder
import javazoom.jl.decoder.Header
import javazoom.jl.decoder.SampleBuffer
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import javax.sound.sampled.*
import kotlin.concurrent.thread

internal class Mp3Sound(
    private val file: File,
    localVolume: Float,
    masterVolume: Float
) : Sound(localVolume, masterVolume) {
    //Multi-thread values.
    @Volatile private var playing = false
    @Volatile private var paused = false
    @Volatile private var stopped = false
    //Playback holders.
    private var playThread: Thread? = null
    private var sourceLine: SourceDataLine? = null

    override val isAlive: Boolean
        get() = playing && !stopped

    override fun play() {
        if (playing) return
        playing = true

        playThread = thread(name = "MP3-${file.name}") {
            var bitstream: Bitstream? = null
            try {
                //Initialize file read.
                val fis = BufferedInputStream(FileInputStream(file))
                bitstream = Bitstream(fis)
                val decoder = Decoder()
                //Get file header.
                var header: Header? = bitstream.readFrame()
                if (header == null) {
                    playing = false
                    return@thread
                }
                //Decode frame to get audio format.
                val output = decoder.decodeFrame(header, bitstream) as SampleBuffer
                val format = AudioFormat(
                    output.sampleFrequency.toFloat(), 16,
                    output.channelCount, true, false
                )
                //Read first frame.
                val line = AudioSystem.getSourceDataLine(format)
                line.open(format, 4096)
                sourceLine = line
                applyGain()
                line.start()
                //Play first frame.
                writeFrame(output, line)
                bitstream.closeFrame()
                //Play the rest of the frames.
                while (!stopped) {
                    //Wait until unpaused.
                    if (paused) {
                        Thread.sleep(50)
                        continue
                    }
                    //Read and play the frame.
                    header = bitstream.readFrame() ?: break
                    val samples = decoder.decodeFrame(header, bitstream) as SampleBuffer
                    writeFrame(samples, line)
                    bitstream.closeFrame()
                }
                //Close sound source.
                line.drain()
                line.stop()
                line.close()
            } catch (e: Exception) {
                if (!stopped) Log.error("MP3 playback error: $e")
            } finally {
                //Close bitstream.
                try { bitstream?.close() } catch (_: Exception) {}
                playing = false
                if (!stopped) onFinished?.invoke()
            }
        }
    }

    //Magic frame decoding.
    private fun writeFrame(buffer: SampleBuffer, line: SourceDataLine) {
        val samples = buffer.buffer
        val len = buffer.bufferLength
        val bytes = ByteArray(len * 2)

        for (i in 0 until len) {
            val sample = samples[i]
            bytes[i*2] = (sample.toInt() and 0xFF).toByte()
            bytes[i*2+1] = (sample.toInt() shr 8).toByte()
        }
        line.write(bytes, 0, bytes.size)
    }

    override fun stop() {
        stopped = true
        paused = false
        sourceLine?.stop()
        playThread?.interrupt()
    }
    override fun pause() {
        paused = true
        sourceLine?.stop()
    }
    override fun resume() {
        paused = false
        sourceLine?.start()
    }

    override fun updateGain(newMasterVolume: Float) {
        masterVolume = newMasterVolume
        applyGain()
    }
    private fun applyGain() {
        val line = sourceLine ?: return
        if (!line.isControlSupported(FloatControl.Type.MASTER_GAIN)) return
        val control = line.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        control.value = calculateGain().coerceIn(control.minimum, control.maximum)
    }
}