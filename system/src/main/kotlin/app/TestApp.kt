package app

import Activity
import AudioService
import GraphicServiceI
import StorageServiceI
import DeviceManagerI
import Button
import Text
import TextField
import background
import fillMaxSize
import Column
import MavenRepository
import fillMaxWidth
import height
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import onClick
import width
import java.awt.Color

class TestApp(
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI,
) : Activity{
    override fun main() {
        val sound = AudioService
        sound.setSound("/mnt/c/Users/MSI/Desktop/discord.mp3")
        gs.setContent(true) {
            Column(
                modifier = Modifier.fillMaxSize().background(Color.CYAN),
                parent = this, verticalArrangement = VerticalArrangement.SpaceEvenly(),
                horizontalAlignment = HorizontalAlignment.Left()
            ).layout {
                TextField(modifier = Modifier.fillMaxWidth().height(100).background(Color.YELLOW), textColor = Color.BLACK, parent = this)
                Button(modifier = Modifier.height(100).fillMaxWidth().background(Color.GREEN).onClick {
                    CoroutineScope(Dispatchers.IO).launch {}
                }, cornerRadius = 50,parent = this).layout {
                    Text(modifier = Modifier.height(14).width(20), text = "Play sound", parent = this)
                }

            }
        }
        gs.redraw()
    }
}