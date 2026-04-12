import common.Log

fun main() {
    Log.info("Initialization")

    val deviceManagerInstance = DeviceManager()
    deviceManagerInstance.initialize()

    val graphicServiceInstance = GraphicService()
    graphicServiceInstance.initialize(Mother.getSystemPath())

    val storageServiceInstance = StorageService()
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