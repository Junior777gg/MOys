package impl

import common.Log
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import service.DeviceManager
import java.io.InputStream
import java.nio.Buffer

class DeviceManagerImpl: DeviceManager {
    fun initialize() {
        Log.info("Device manager initialized")
    }
    object Camera{
        var cameraPath = "/dev/video0"
        fun cameraDataFrame(path: String = cameraPath): Frame? {
            val grabber = FFmpegFrameGrabber(path)
            grabber.start()
            val frame = grabber.grab()
            return frame
        }
    }
    @Deprecated("not done yet")
    object Microphone{
        var microphonePath = "pulse://default"
        fun microphoneDataFrame(path: String = microphonePath){

        }
    }
}