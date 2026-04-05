package app

import Activity
import GraphicServiceI
import StorageServiceI
import DeviceManagerI
import Button
import LazyColumn
import TextField
import background
import fillMaxSize
import height
import size
import width
import java.awt.Color

class TestApp(
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI
) : Activity {
    override fun main() {
        gs.setContent(true){
            LazyColumn(modifier = Modifier.Companion.fillMaxSize().background(Color.CYAN), this).layout {
                TextField(modifier = Modifier.Companion.width(640).height(100),"",15, Color.black, this)
                for (i in 0..10){
                    Button(modifier = Modifier.Companion.size(640).background(Color.YELLOW),this)
                    Button(modifier = Modifier.size(640).background(Color.GREEN),this)
                }
            }
        }
        gs.redraw()
    }
}