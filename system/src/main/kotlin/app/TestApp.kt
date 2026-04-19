package app

import Activity
import service.AudioService
import service.GraphicService
import service.StorageService
import service.DeviceManager
import Button
import Text
import TextField
import Column
import Image
import MultilineText
import View
import common.Color
import impl.DeviceManagerImpl
import modifier.HorizontalAlignment
import modifier.Modifier
import modifier.TextAlignment
import modifier.VerticalArrangement
import modifier.background
import modifier.cornerRadius
import modifier.fillMaxSize
import modifier.fillMaxWidth
import modifier.height
import modifier.onClick
import modifier.width
import org.bytedeco.javacv.Java2DFrameConverter
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.awt.BorderLayout
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.WindowConstants.EXIT_ON_CLOSE

class TestApp(
    override val gs: GraphicService,
    override val storage: StorageService,
    override val deviceManager: DeviceManager,
) : Activity {
    override fun main() {
        gs.setContent(true) {
            buildUI()
        }
        gs.redraw()
        val vlcPlayer = EmbeddedMediaPlayerComponent()
        JFrame("📹 Камера").apply {
            contentPane.add(vlcPlayer, BorderLayout.CENTER)
            size = java.awt.Dimension(800, 600)
            isVisible = true
        }
        vlcPlayer.mediaPlayer().media().play("http://192.168.0.15:8080/video")
        vlcPlayer.stopped(vlcPlayer.mediaPlayer())
        vlcPlayer.remove(vlcPlayer)
    }

    fun MutableList<View>.buildUI() {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.CYAN),
            parent = this, verticalArrangement = VerticalArrangement.SpaceEvenly(),
            horizontalAlignment = HorizontalAlignment.Left()
        ).layout {

        }
    }

    fun updateUI() {
        gs.setContent {
            buildUI()
        }
        gs.redraw()
    }
}