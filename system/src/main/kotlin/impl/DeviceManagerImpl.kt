package impl

import common.Log
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import service.DeviceManager

class DeviceManagerImpl: DeviceManager {
    fun initialize() {
        Log.info("Device manager initialized")
    }
    object Camera {
        var cameraPath = "/dev/video0"
        var grabber: FFmpegFrameGrabber? = null

        fun start(): Boolean {
            try {
                if(!NetworkServiceImpl.isAddressReachable(cameraPath,1000)) throw Exception("$cameraPath not reachable")
                grabber?.stop()
                grabber = FFmpegFrameGrabber.createDefault(cameraPath)
                grabber?.start()
                return true
            } catch (e: Exception) {
                Log.error("Failed to start camera",e)
                return false
            }

        }
        fun getFrame(path: String = cameraPath): Frame? {
            return grabber?.grab()
        }
    }
    @Deprecated("not done yet")
    object Microphone{
        var microphonePath = "pulse://default"
        fun getFrame(path: String = microphonePath) {

        }
    }
}