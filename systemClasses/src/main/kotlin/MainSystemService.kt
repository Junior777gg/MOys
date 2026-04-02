fun main() {
    Log.info("Initialization")

    val deviceManagerInstance = DeviceManager()
    deviceManagerInstance.initialize()

    val graphicServiceInstance = GraphicService()
    graphicServiceInstance.initialize()

    val storageServiceInstance = StorageService()
    storageServiceInstance.initialize()

    val motherInstance = Mother(
        graphicServiceInstance,
        deviceManagerInstance,
        storageServiceInstance,
    )
    motherInstance.start()
    Log.info("System ready")
}