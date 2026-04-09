interface Activity {
    val gs : GraphicServiceI
    val storage : StorageServiceI
    val deviceManager : DeviceManagerI
    fun main()

    fun onNavigationBack(): Boolean = true
}