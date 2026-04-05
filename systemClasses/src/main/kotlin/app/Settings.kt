package app

import Activity
import DeviceManagerI
import GraphicServiceI
import Mother
import StorageServiceI
import View
import Text
import fillMaxSize

class Settings(
    val mother: Mother,
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI
) : Activity {
    override fun main() {
        render(true)
    }
    private fun render(newScreen: Boolean) {
        gs.setContent(itIsNewScreen = newScreen) { buildUI() }
        gs.redraw()
    }
    private fun MutableList<View>.buildUI() {
        Text(
            modifier=Modifier.fillMaxSize(),
            text="Settings App",
            textSize=48,
            parent=this
        )
    }
}