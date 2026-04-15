import service.GraphicService
import service.DeviceManager
import service.StorageService

interface Activity {
    val gs : GraphicService
    val storage : StorageService
    val deviceManager : DeviceManager

    /**
     * Overridable
     *
     * Called on application start.
    */
    fun main()
    /**
     * Overridable
     *
     * Return false to stay on current screen (don't pop back stack).
     * Isn't called on return home.
    */
    fun onNavigationBack(): Boolean = true
}