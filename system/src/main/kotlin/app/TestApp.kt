package app

import Activity
import Animator
import Animator.Companion.NONE
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
import modifier.animation
import modifier.background
import modifier.fillMaxSize
import modifier.onClick
import modifier.size
import org.bytedeco.javacv.Java2DFrameConverter
import org.jetbrains.skiko.toBitmap
import java.awt.image.BufferedImage


class TestApp(
    override val gs: GraphicServiceImpl,
    override val storage: StorageService,
    override val deviceManager: DeviceManager, override var lastState: MutableList<View>?,
) : Activity {
    override fun main() {
        gs.setContent(true) {
            Column(modifier = Modifier.fillMaxSize().background(Color.ORANGE), this).layout {
                Column(
                    modifier = Modifier.size(50).background(Color.BLUE).animation(
                        Animator(
                            0, NONE,
                            animators = arrayOf(
                                Animator(100, Animator.SLIDE_HORIZONTALLY, argument = 500),
                                Animator(1000, Animator.SHAKE)
                            )
                        )
                    ), this
                )
                Column(
                    modifier = Modifier.size(100).background(Color.CYAN).animation(Animator(4000, Animator.COLORFADE, argument = Color.PINK)


                    ), this
                )
            }
        }
        gs.redraw()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}