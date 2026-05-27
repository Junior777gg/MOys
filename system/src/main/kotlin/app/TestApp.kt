package app

import Activity
import Animator
import Animator.Companion.NONE
import service.StorageService
import service.DeviceManager
import Column
import View
import common.Color
import common.Easing
import impl.GraphicServiceImpl
import modifier.Modifier
import modifier.animation
import modifier.background
import modifier.fillMaxSize
import modifier.size

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
                                Animator(500, Animator.SLIDE_HORIZONTALLY, Easing.BOUNCE_OUT, 500),
                                Animator(1000, Animator.SHAKE),
                            )
                        )
                    ), this
                )
                Column(
                    modifier = Modifier.size(100).background(Color.CYAN).animation(Animator(
                        4000,
                        Animator.COLORFADE,
                        Easing.SINE_IN_OUT,
                        Color.PINK)),
                    this
                )
            }
        }
        gs.redraw()
    }
}