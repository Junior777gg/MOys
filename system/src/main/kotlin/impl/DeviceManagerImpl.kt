package impl

import common.Log
import service.DeviceManager

class DeviceManagerImpl: DeviceManager {
    fun initialize() {
        Log.info("Device manager initialized")
    }
}