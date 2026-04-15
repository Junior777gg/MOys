package app

import Activity
import AudioService
import service.GraphicService
import service.StorageService
import service.DeviceManager
import Button
import Text
import TextField
import Column
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import common.Color
import modifier.HorizontalAlignment
import modifier.Modifier
import modifier.VerticalArrangement
import modifier.background
import modifier.cornerRadius
import modifier.fillMaxSize
import modifier.fillMaxWidth
import modifier.height
import modifier.onClick
import modifier.width

class TestApp(
    override val gs: GraphicService,
    override val storage: StorageService,
    override val deviceManager: DeviceManager,
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
                TextField(modifier = Modifier.fillMaxWidth().height(100).background(Color.YELLOW).cornerRadius(40), textColor = Color.BLACK, parent = this)
                Button(modifier = Modifier.height(100).fillMaxWidth().background(Color.GREEN).onClick {
                    CoroutineScope(Dispatchers.IO).launch {}
                },parent = this).layout {
                    Text(modifier = Modifier.height(14).width(20), text = "Play sound", parent = this)
                }

            }
        }
        gs.redraw()
    }
}