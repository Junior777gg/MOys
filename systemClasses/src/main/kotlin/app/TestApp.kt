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
import Column
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
        gs.setContent(true) {
            Column(
                modifier = Modifier.fillMaxSize().background(Color.CYAN),
                parent = this, verticalArrangement = VerticalArrangement.SpaceEvenly(),
                horizontalAlignment = HorizontalAlignment.Left()
            ).layout {
                Button(modifier = Modifier.size(100).background(Color.YELLOW), this)
                Button(modifier = Modifier.size(100).background(Color.ORANGE), this)
                Button(modifier = Modifier.size(100).background(Color.GREEN), this)

            }
        }
        gs.redraw()
    }
}