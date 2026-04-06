package app

import Activity
import Button
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
import fillMaxSize
import height
import onClick
import padding
import paddingLeft
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
                Text(modifier = Modifier.width(640).height(50), text = "Launcher", textSize = 24, textColor = Color.BLACK, parent = this)
                Column(modifier = Modifier.height(50), parent=this).layout {
                    Text(
                        modifier = Modifier.height(50).width(50),
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
                Row(
                    modifier = Modifier.height(50),
                    parent = this
                ).layout {
                    var checkboxColor = Color.RED
                    if (mother.systemLauncher.getTextDisplay()) checkboxColor = Color.GREEN
                    Button(modifier = Modifier.size(20).background(checkboxColor).onClick {
                        mother.systemLauncher.setTextDisplay(!mother.systemLauncher.getTextDisplay())
                        render(false)
                    }, parent = this)
                    Text(
                        modifier = Modifier.height(50).width(50).paddingLeft(20),
                        text = "Display app names",
                        textSize = 24,
                        textColor = Color.BLACK,
                        parent = this
                    )
                }
                if (mother.systemLauncher.getTextDisplay()) {
                    Row(
                        modifier = Modifier.height(50),
                        parent = this
                    ).layout {
                        var checkboxColor = Color.RED
                        if (mother.systemLauncher.getTextDark()) checkboxColor = Color.GREEN
                        Button(modifier = Modifier.size(20).background(checkboxColor).onClick {
                            mother.systemLauncher.setTextDark(!mother.systemLauncher.getTextDark())
                            render(false)
                        }, parent = this)
                        Text(
                            modifier = Modifier.height(50).width(50).paddingLeft(20),
                            text = "Dark app names",
                            textSize = 24,
                            textColor = Color.BLACK,
                            parent = this
                        )
                    }
                }
            }
        }
    }
    private fun setBG(path: String) {
        mother.systemLauncher.setBackground(path)
    }
}