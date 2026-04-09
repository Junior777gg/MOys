package audio

import com.jcraft.jogg.Packet
import com.jcraft.jogg.Page
import com.jcraft.jogg.StreamState
import com.jcraft.jogg.SyncState
import com.jcraft.jorbis.Block
import com.jcraft.jorbis.Comment
import com.jcraft.jorbis.DspState
import com.jcraft.jorbis.Info
import common.Log
import java.io.File
import java.io.FileInputStream
import javax.sound.sampled.*
import kotlin.concurrent.thread

internal class OggSound(
    private val file: File,
    localVolume: Float,
    masterVolume: Float
) : Sound(localVolume, masterVolume) {
    @Volatile private var playing = false
    @Volatile private var paused = false
    @Volatile private var stopped = false
    private var playThread: Thread? = null
    private var sourceLine: SourceDataLine? = null

    override val isAlive: Boolean
        get() = playing && !stopped

    override fun play() {
        if (playing) return
        playing = true

        playThread = thread(name = "OGG-${file.name}") {
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)

                val syncState = SyncState()
                val streamState = StreamState()
                val page = Page()
                val packet = Packet()
                val info = Info()
                val comment = Comment()
                val dspState = DspState()
                val block = Block(dspState)

                syncState.init()
                info.init()
                comment.init()

                var convsize = 4096 * 2
                val convbuffer = ByteArray(convsize)

                //Read ogg header.
                var headersRead = 0
                while (headersRead < 3) {
                    var index = syncState.buffer(4096)
                    var bytes = fis.read(syncState.data, index, 4096)
                    if (bytes <= 0) break
                    syncState.wrote(bytes)

                    while (headersRead < 3) {
                        val result = syncState.pageout(page)
                        if (result == 0) break //Code: need more data
                        if (result == -1) continue //Code: not a page

                        if (headersRead == 0) {
                            streamState.init(page.serialno())
                        }

                        if (streamState.pagein(page) < 0) {
                            throw Exception("Error reading OGG page")
                        }

                        while (headersRead < 3) {
                            val packetResult = streamState.packetout(packet)
                            if (packetResult == 0) break
                            if (packetResult == -1) continue

                            if (info.synthesis_headerin(comment, packet) < 0) {
                                throw Exception("Error reading OGG headers")
                            }
                            headersRead++
                        }
                    }
                }

                convsize = 4096 / info.channels

                dspState.synthesis_init(info)
                block.init(dspState)

                val format = AudioFormat(
                    info.rate.toFloat(),
                    16,
                    info.channels,
                    true,
                    false
                )

                val line = AudioSystem.getSourceDataLine(format)
                line.open(format, 4096)
                sourceLine = line
                applyGain()
                line.start()

                val pcmInfo = Array(1) { arrayOfNulls<FloatArray>(info.channels) }
                val pcmIndex = IntArray(info.channels)

                outer@ while (!stopped) {
                    //Wait until unpaused.
                    if (paused) {
                        Thread.sleep(50)
                        continue
                    }
                    //Read audio page.
                    val pageResult = syncState.pageout(page)
                    //Code: need more data
                    if (pageResult == 0) {
                        val index = syncState.buffer(4096)
                        val bytes = fis.read(syncState.data, index, 4096)
                        if (bytes <= 0) break
                        syncState.wrote(bytes)
                        continue
                    }
                    //Code: not a page
                    if (pageResult == -1) continue
                    //Decode page.
                    streamState.pagein(page)
                    while (!stopped && !paused) {
                        val packetResult = streamState.packetout(packet)
                        if (packetResult == 0) break
                        if (packetResult == -1) continue

                        if (block.synthesis(packet) == 0) {
                            dspState.synthesis_blockin(block)
                        }

                        var samples: Int
                        while (!stopped && !paused) {
                            samples = dspState.synthesis_pcmout(pcmInfo, pcmIndex)
                            if (samples <= 0) break
                            //Encode sound data to buffer.
                            val bout = minOf(samples, convsize)
                            for (i in 0 until info.channels) {
                                val pcmChannel = pcmInfo[0][i]!!
                                val pcmOff = pcmIndex[i]
                                for (j in 0 until bout) {
                                    var value = (pcmChannel[pcmOff + j] * 32767f).toInt()
                                    value = value.coerceIn(-32768, 32767)
                                    val outIndex = (j * 2 * info.channels) + (i * 2)
                                    convbuffer[outIndex] = (value and 0xFF).toByte()
                                    convbuffer[outIndex + 1] = (value shr 8).toByte()
                                }
                            }

                            line.write(convbuffer, 0, 2 * info.channels * bout)
                            dspState.synthesis_read(bout)
                        }
                    }
                    //If end of file is reached - we are over.
                    if (page.eos() != 0) break
                }
                //Close sound source.
                line.drain()
                line.stop()
                line.close()
            } catch (e: Exception) {
                if (!stopped) Log.error("OGG playback error: $e")
            } finally {
                //Close bitstream.
                try { fis?.close() } catch (_: Exception) {}
                playing = false
                if (!stopped) onFinished?.invoke()
            }
        }
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