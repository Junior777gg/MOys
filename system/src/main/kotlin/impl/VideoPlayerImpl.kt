package impl

import Column
import Image
import Row
import Text
import View
import common.Color
import common.Log
import modifier.HorizontalArrangement
import modifier.Modifier
import modifier.VerticalAlignment
import modifier.VerticalArrangement
import modifier.background
import modifier.fillMaxSize
import modifier.height
import modifier.onClick
import modifier.size
import modifier.width
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import org.jetbrains.skiko.toImage
import service.GraphicService
import java.awt.image.BufferedImage
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

class VideoPlayerImpl(val gs: GraphicService) {
    lateinit var grabber: FFmpegFrameGrabber

    private var line: SourceDataLine? = null
    private var initialized = false

    private var audioFormat: AudioFormat? = null
    var stopped = false

    var currentTimestamp = 0L

    val converter = Java2DFrameConverter()
    var image: Image? = null
    var lastImage: BufferedImage? = null

    fun isInitialized(): Boolean = initialized

    fun create(path: String) {
        try {
            grabber = FFmpegFrameGrabber.createDefault(path)
            grabber.sampleFormat = avutil.AV_SAMPLE_FMT_S16
            grabber.audioChannels = 2
            grabber.sampleRate = 44100
            grabber.start()
            currentTimestamp = currentTimestamp.coerceIn(0, grabber.lengthInTime)
            audioFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                grabber.sampleRate.toFloat(),
                16,
                grabber.audioChannels,
                grabber.audioChannels * 2,
                grabber.sampleRate.toFloat(),
                false
            )

            line = AudioSystem.getSourceDataLine(audioFormat)
            line!!.open(audioFormat)
            line!!.start()
            initialized=true
        } catch (e: Exception) {
            initialized=false
            Log.error("Failed to initialize video player from \"$path\"",e)
        }
    }

    fun playerUI(parent: MutableList<View>) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT),
            verticalArrangement = VerticalArrangement.SpaceEvenly(),
            parent = parent
        ).layout {
            Row(modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT), this)
            Row(
                modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT),
                horizontalArrangement = HorizontalArrangement.Center(),
                verticalAlignment = VerticalAlignment.Center(),
                parent = this
            ).layout {
                Text(modifier = Modifier.size(100).onClick {
                    currentTimestamp -= 10000000
                }, text = "<-", textSize = 25, parent = this)
                Text(modifier = Modifier.height(200).width(300), text = "Остановлено", textSize = 25, parent = this)
                Text(modifier = Modifier.size(100).onClick {
                    currentTimestamp += 10000000
                }, text = "->", textSize = 25, parent = this)
            }
            Row(modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT), this)
        }
    }

    fun start() {
        gs.injectUI {
            image = Image(modifier = Modifier.fillMaxSize().onClick {
                stop()
                image!!.layout {
                    if (stopped) {
                        playerUI(this)
                    } else {
                        this.clear()
                    }
                }
                gs.redraw()

            }, image = lastImage, parent = this)
        }
        Thread {
            while (true) {
                try {
                    val frame = grabber.grab()

                    if (frame.samples != null && line != null) {
                        val shortBuffer = frame.samples[0] as ShortBuffer
                        val shorts = ShortArray(shortBuffer.remaining())
                        shortBuffer.get(shorts)
                        shortBuffer.rewind()


                        val bytes = ByteArray(shorts.size * 2)
                        for (i in shorts.indices) {
                            bytes[i * 2] = (shorts[i].toInt() and 0xFF).toByte()
                            bytes[i * 2 + 1] = (shorts[i].toInt() shr 8 and 0xFF).toByte()
                        }
                        line!!.write(bytes, 0, bytes.size) // ← вот что забыли
                    }

                    if (frame.image != null) {
                        lastImage = converter.convert(frame)
                        if (image != null) {
                            image!!.image = lastImage?.toImage()
                            gs.redraw()
                            frame.close()
                            Thread.sleep(33)
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }.start()
    }

    fun stop() {
        stopped = !stopped
        if (stopped) {
            line!!.stop()
            currentTimestamp = grabber.timestamp
            grabber.stop()
        } else {
            line!!.start()
            grabber.start()
            grabber.timestamp = currentTimestamp
        }

    }

    fun remove() {
        grabber.stop()
        line!!.stop()
        line!!.close()
        line = null
        image = null
    }
}