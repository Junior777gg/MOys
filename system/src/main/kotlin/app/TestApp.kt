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
import height
import onClick
import size
import width
import java.awt.Color

class TestApp(
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI,
) : Activity {
    override fun main() {
        val sound = AudioService
        sound.setSound("/mnt/c/Users/MSI/Desktop/discord.mp3")
        gs.setContent(true) {
            TextField(modifier = Modifier.width(640).height(30),textSize = 15, parent = this)
            Column(
                modifier = Modifier.fillMaxSize().background(Color.CYAN),
                parent = this, verticalArrangement = VerticalArrangement.SpaceEvenly(),
                horizontalAlignment = HorizontalAlignment.Left()
            ).layout {
                Button(modifier = Modifier.size(100).background(Color.YELLOW).onClick{
                    sound.play()
                }, parent = this)
                Button(modifier = Modifier.size(100).background(Color.ORANGE), parent = this)
                Button(modifier = Modifier.size(100).background(Color.GREEN).onClick {
                    sound.cancel()
                }, parent = this).layout {
                    Text(modifier = Modifier.height(14).width(20), text = "Play sound", parent = this)
                }

            }
        }
        gs.redraw()
    }
}