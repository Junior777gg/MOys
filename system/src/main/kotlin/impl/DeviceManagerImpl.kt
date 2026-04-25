package impl

import common.Log
import org.bytedeco.ffmpeg.global.avutil
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
        var grabber: FFmpegFrameGrabber? = null

        fun startCamera(){
            grabber?.stop()
            grabber = FFmpegFrameGrabber.createDefault(cameraPath)
            grabber?.start()
            }
        fun cameraDataFrame(path: String = cameraPath): Frame? {
            return grabber?.grab()
        }
    }
    @Deprecated("not done yet")
    object Microphone{
        var microphonePath = "pulse://default"
        fun microphoneDataFrame(path: String = microphonePath){

        }
    }
}