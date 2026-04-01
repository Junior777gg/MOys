fun main() {
    println("Инициализация")

    val graphicServiceInstance = GraphicService()
    graphicServiceInstance.initialize()

    val deviceManagerInstance = DeviceManager()
    deviceManagerInstance.initialize()

    val storageServiceInstance = StorageService()
    storageServiceInstance.initialize()

    val motherInstance = Mother(
        graphicServiceInstance,
        deviceManagerInstance,
        storageServiceInstance,
    )
    motherInstance.start()

}