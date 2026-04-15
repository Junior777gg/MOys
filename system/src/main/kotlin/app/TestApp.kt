package app

import Activity
import service.AudioService
import service.GraphicService
import service.StorageService
import service.DeviceManager
import Button
import Text
import TextField
import Column
import MultilineText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import common.Color
import modifier.HorizontalAlignment
import modifier.Modifier
import modifier.TextAlignment
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
        //Idk if this even works. It doesn't for me.
        //val sound = AudioService
        //sound.setSound("/mnt/c/Users/MSI/Desktop/discord.mp3")
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
                MultilineText(text = """
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                    Sed id dictum sem. Duis quis porttitor sapien.
                    Phasellus tincidunt elit leo, vitae suscipit mi ullamcorper nec.
                    Sed vulputate magna vel purus dapibus placerat.
                    Curabitur ac blandit enim, id placerat enim.
                    Duis eu consectetur ex, nec fermentum nunc.
                    Pellentesque egestas finibus mi, efficitur ultricies nisl commodo quis.
                    Sed fringilla dapibus enim nec volutpat.
                    Curabitur gravida pharetra elit non malesuada.
                    Duis iaculis dui mauris, in vestibulum enim semper sit amet.
                """.trimIndent(), lineSpacing = 1, textSize = 14, modifier = Modifier.fillMaxSize(), textAlign = TextAlignment.Top(), textColor = Color.BLACK, parent = this)

            }
        }
        gs.redraw()
    }
}