package impl

import Column
import Image
import View
import common.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import modifier.Modifier
import modifier.background
import modifier.fillMaxSize
import modifier.onClick
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.librealsense.frame
import org.jetbrains.skiko.toBitmap
import service.GraphicService
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import java.awt.BorderLayout
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import javax.swing.JFrame

class VideoPlayerImpl(val gs: GraphicService) {
    lateinit var grabber: FFmpegFrameGrabber

    private var line: SourceDataLine? = null
    private var audioFormat: AudioFormat? = null

    var stopped = false

    var currentTimestamp = 0L

    val converter = Java2DFrameConverter()
    var image: Image? = null
    var lastImage: BufferedImage? = null

    fun createVideoPlayer(path: String) {
        grabber = FFmpegFrameGrabber.createDefault(path)
        grabber.sampleFormat = avutil.AV_SAMPLE_FMT_S16
        grabber.audioChannels = 2
        grabber.sampleRate = 44100
        grabber.start()
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
    }

    fun startVideoPlayer(context: MutableList<View>) {
        image = Image(modifier = Modifier.fillMaxSize().onClick {
            stopPlayer()
        }, image = lastImage, parent = context)

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val frame = grabber.grab()
                    when {
                        frame.samples != null && line != null -> {
                            val shortBuffer = frame.samples[0] as ShortBuffer
                            val shorts = ShortArray(shortBuffer.remaining())
                            shortBuffer.get(shorts)
                            shortBuffer.rewind()

                            // Конвертируем ShortArray в ByteArray (little-endian)
                            val bytes = ByteArray(shorts.size * 2)
                            for (i in shorts.indices) {
                                bytes[i * 2] = (shorts[i].toInt() and 0xFF).toByte()
                                bytes[i * 2 + 1] = (shorts[i].toInt() shr 8 and 0xFF).toByte()
                            }
                            line!!.write(bytes, 0, bytes.size) // ← вот что забыли
                        }

                        frame.image != null -> {
                            lastImage = converter.convert(frame)
                            if (image != null) {
                                image!!.image = org.jetbrains.skia.Image.makeFromBitmap(lastImage!!.toBitmap())
                                gs.redraw()
                                frame.close()
                                delay(33)
                            }
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }
    }

    fun stopPlayer() {
        stopped = !stopped
        if (stopped) {
            line!!.stop()
            currentTimestamp = grabber.timestamp
            grabber.stop()
        }else{
            line!!.start()
            grabber.start()
            grabber.timestamp = currentTimestamp
        }

    }

    fun removeVideoPlayer() {
        grabber.stop()
        line!!.stop()
        line!!.close()
        line = null
        image = null
    }
}