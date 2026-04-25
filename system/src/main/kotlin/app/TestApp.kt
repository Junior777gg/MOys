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
import View
import common.Color
import common.Log
import common.Stack
import impl.AudioServiceImpl
import impl.DeviceManagerImpl
import impl.GraphicServiceImpl
import impl.VideoPlayerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modifier.HorizontalAlignment
import modifier.Modifier
import modifier.TextAlignment
import modifier.VerticalArrangement
import modifier.background
import modifier.fillMaxSize
import org.bytedeco.javacv.Java2DFrameConverter
import org.jetbrains.skiko.toBitmap
import java.awt.image.BufferedImage


class TestApp(
    override val gs: GraphicServiceImpl,
    override val storage: StorageService,
    override val deviceManager: DeviceManager,
) : Activity {
    val player = VideoPlayerImpl(gs)
    override fun main() {
        gs.setContent(true){
            Column(modifier = Modifier.fillMaxSize().background(Color.BLUE), this).layout {
                player.createVideoPlayer("/mnt/c/Users/MSI/Desktop/zxc2.mp4")
                player.startVideoPlayer(this@layout)
            }
        }
        gs.redraw()
    }

    override fun onDestroy() {
        player.removeVideoPlayer()
    }
}