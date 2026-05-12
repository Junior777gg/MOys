package service

import View
import common.Vec2i

interface GraphicService {
    object RESOLUTIONS {
        val R_144p = Vec2i(256, 144)
        val R_240p = Vec2i(426, 420)
        val R_360p = Vec2i(640, 360)
        val R_480p = Vec2i(640, 480)
        val R_960p = Vec2i(960, 640)
        val R_HD = Vec2i(1366, 768)
        val R_720p = Vec2i(1280, 720)
        val R_HD_PLUS = Vec2i(1600, 900)
        val R_FULL_HD = Vec2i(1920, 1080)
        val R_WUXGA = Vec2i(1920, 1200)
        val R_2K = Vec2i(2560, 1440)
        val R_WQXGA = Vec2i(2560, 1600)
        val R_UWQHD = Vec2i(3440, 1440)
        val R_4K = Vec2i(3840, 2160)
        val R_WQUXGA = Vec2i(3840, 2400)
        val R_5K = Vec2i(5120, 2880)
        val R_8K = Vec2i(7680, 4320)

        //All default resolutions in a list.
        val R_ALL = listOf(R_360p, R_480p, R_960p, R_HD, R_720p, R_HD_PLUS, R_FULL_HD, R_WUXGA, R_2K)
    }

    fun setContent(itIsNewScreen: Boolean = false, lambda: MutableList<View>.() -> Unit)
    fun popBackStack()
    fun redraw()
    fun injectUI(lambda: MutableList<View>.() -> Unit)
    fun cancelInject()
}