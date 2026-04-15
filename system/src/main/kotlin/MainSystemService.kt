import common.Log
import impl.DeviceManagerImpl
import impl.GraphicServiceImpl
import impl.StorageServiceImpl

fun main() {
    Log.info("Initialization")

    val deviceManagerInstance = DeviceManagerImpl()
    deviceManagerInstance.initialize()

    val graphicServiceInstance = GraphicServiceImpl()
    graphicServiceInstance.initialize(Mother.systemPath)

    val storageServiceInstance = StorageServiceImpl()
    storageServiceInstance.initialize()

    val motherInstance = Mother(
        graphicServiceInstance,
        deviceManagerInstance,
        storageServiceInstance,
    )
    motherInstance.start()
    Log.info("System ready")

    Runtime.getRuntime().addShutdownHook(Thread {
        motherInstance.shutdown()
        Log.info("System shut down")
    })
}