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
    override val deviceManager: DeviceManager, override var lastState: MutableList<View>?,
) : Activity {
    val player = VideoPlayerImpl(gs)
    override fun main() {
        gs.setContent(true) {
            player.createVideoPlayer("https://eun13.playlist.ttvnw.net/v1/playlist/CtoEdOV4tCkSfgLQ5UJPW50sl4AhjH4TEEoLqsz54X2_l7c2g6dGjmBJCl3aHA720BSaINBfub8IYiEG-AqUQ2fNR7xDaoZpDzht7b6LqbrujWXZaBdNTU2Rn7794qVBP_OsVg1Rjs1Rn63wy3RBIdAtYw92nm73vD4LnjHDf8TgvKRgnoQSRY9_BJBZVHfYOQ5q9FsostD8EChIDirKVH9v0IxLZSel6uLnFTk6x_DPhFNt7XN_zVlVtvUbZS4DlQJWPEuIUk7LVuJ0Ztj6snTpgeqN5V38Ndkry-biDw1uIus5QjzCEmTfq2Vbfzmcn49TScy9HP08zErBP5yvo9baeq7bhuDIEbu6-Dy6HCln1rKnQgr-mr9bzLjsMYhPpUD_FngbPzqNdf3vb0sXXhM79OFaylDvDAkwtsQR3teKepiA-3dmTm9mNWnr-TIGl5a0ba6MUJljBcKLWpqwF_7t4OHHnBSDnnnMhXzKsHCiXWrq_pRgV5FYw6dr88XUTGDowOceUCx5k1me8tJBW1X95PoLLPkcIaAtsKxwVdlewUZaR_tFdo707LJrnGWW0v4Doii_iWcivqvtF9jrBCHQhpkn3AVgRsQqRCTMRqoQWFGlzSDKAb2saFyWl4uFbSI49lrfvh6VpQF841hERzDQyShsH6ykfLYh0ASvz-7S4x_0NZL2bXMgvb4nd9wp5un-KinuY16SSGmg6zjTM1UfClaoOaf_hjihtil_P87DvRoMYrP7OklDpjS-MZD7rGEi4DOXemImjlXGLyGNX_oedTJykPJnRnTbl30aDHi1Rc6mtd-G06kIeCABKglldS13ZXN0LTIwkQ8.m3u8")
            player.startVideoPlayer()
        }
        gs.redraw()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.removeVideoPlayer()
    }
}