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
import fillMaxWidth
import height
import onClick
import padding
import paddingLeft
import size
import width
import java.awt.Color
import java.io.File

class SettingsApp(
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
            Column(modifier = Modifier.padding(5).fillMaxWidth().height(480), horizontalAlignment = HorizontalAlignment.Center(), parent=this).layout {
                Text(modifier = Modifier.width(640).height(50), text = "Launcher", textSize = 24, textColor = Color.BLACK, parent = this)
                Row(modifier = Modifier.height(50).width(500), horizontalArrangement = HorizontalArrangement.Left(), parent=this).layout {
                    Text(
                        modifier = Modifier.height(50).width(50),
                        text = "Background",
                        textSize = 24,
                        textColor = Color.BLACK,
                        parent = this
                    )
                }
                Row(modifier = Modifier.height(140).fillMaxWidth(), horizontalArrangement = HorizontalArrangement.Left(), parent = this).layout {
                    Image(modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/mobile/1.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/mobile/1.png"),
                        parent = this
                    )
                    Image(modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/mobile/2.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/mobile/2.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/mobile/3.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/mobile/3.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/mobile/4.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/mobile/4.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/mobile/5.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/mobile/5.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/desktop/1.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/desktop/1.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/desktop/2.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/desktop/2.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/desktop/3.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/desktop/3.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/desktop/4.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/desktop/4.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/desktop/5.png") },
                        file = File("${mother.getSystemPath()}/data/launcher/backgrounds/desktop/5.png"),
                        parent = this
                    )
                }
                checkbox(get=mother.systemLauncher.getAppsCentering(),set={v->mother.systemLauncher.setAppsCentering(v)},text="Center apps",parent=this)
                checkbox(get=mother.systemLauncher.getTextDisplay(),set={v->mother.systemLauncher.setTextDisplay(v)},text="Display app names",parent=this)
                if(mother.systemLauncher.getTextDisplay()) {
                    checkbox(get=mother.systemLauncher.getTextDark(),set={v->mother.systemLauncher.setTextDark(v)},text="Dark app names",parent=this)
                }
            }
        }
    }
    private fun checkbox(get: Boolean, set: (Boolean)->Unit, text: String, parent: MutableList<View>) {
        Row(
            modifier = Modifier.height(50).width(500),
            horizontalArrangement = HorizontalArrangement.Left(),
            parent = parent
        ).layout {
            var checkboxColor = Color.RED
            if (get) checkboxColor = Color.GREEN
            Button(modifier = Modifier.size(20).background(checkboxColor).onClick {
                set.invoke(!get)
                render(false)
            }, parent = this)
            Text(
                modifier = Modifier.height(50).width(50).paddingLeft(200),
                text = text,
                textSize = 24,
                textAlign = Text.LEFT_CENTER,
                textColor = Color.BLACK,
                parent = this
            )
        }
    }
    private fun setBG(path: String) {
        mother.systemLauncher.setBackground(path)
    }
}