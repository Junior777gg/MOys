package app

import Activity
import Column
import DeviceManagerI
import GraphicServiceI
import Image
import LazyColumn
import Mother
import Row
import StorageServiceI
import View
import Text
import background
import childrenWidthCentering
import fillMaxSize
import height
import onClick
import padding
import size
import width
import java.awt.Color
import java.io.File

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
        LazyColumn(modifier = Modifier.fillMaxSize().background(Color.WHITE), this).layout {
            Column(modifier = Modifier.padding(5).width(640).height(480), parent=this).layout {
                Text(modifier = Modifier.width(640).height(50), text = "Personalization", textSize = 24, textColor = Color.BLACK, parent = this)
                Column(modifier = Modifier.childrenWidthCentering(Modifier.Companion.LEFT).height(50), parent=this).layout {
                    Text(
                        modifier = Modifier.height(50).width(100),
                        text = "Background",
                        textSize = 24,
                        textColor = Color.BLACK,
                        parent = this
                    )
                }
                Row(modifier = Modifier.height(140).width(640), parent = this).layout {
                    Image(modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/1.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/1.png"),
                        parent = this
                    )
                    Image(modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/2.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/2.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/3.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/3.png"),
                        parent = this
                    )
                }
            }
        }
    }
    private fun setBG(path: String) {
        mother.systemLauncher.setBackground(path)
    }
}