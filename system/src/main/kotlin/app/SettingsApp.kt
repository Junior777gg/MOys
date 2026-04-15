package app

import Activity
import Button
import service.DeviceManager
import impl.GraphicServiceImpl
import Image
import LazyColumn
import Mother
import Row
import service.StorageService
import View
import Text
import common.Color
import modifier.HorizontalArrangement
import modifier.Modifier
import modifier.TextAlignment
import modifier.background
import modifier.fillMaxSize
import modifier.fillMaxWidth
import modifier.height
import modifier.onClick
import modifier.padding
import modifier.paddingLeft
import modifier.size
import modifier.width
import java.io.File

class SettingsApp(
    val mother: Mother,
    override val gs: GraphicServiceImpl,
    override val storage: StorageService,
    override val deviceManager: DeviceManager
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
            Text(
                modifier = Modifier.fillMaxWidth().height(50),
                text = "Launcher",
                textSize = 24,
                textColor = Color.BLACK,
                parent = this
            )
            Text(
                modifier = Modifier.fillMaxWidth().height(50).paddingLeft(10),
                text = "Background",
                textSize = 24,
                textColor = Color.BLACK,
                textAlign = TextAlignment.Left(),
                parent = this
            )
            Row(
                modifier = Modifier.height(140).fillMaxWidth(),
                horizontalArrangement = HorizontalArrangement.Left(),
                parent = this
            ).layout {
                if(!GraphicServiceImpl.isDesktopResolution()) {
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/mobile/1.png") },
                        file = File("${Mother.systemPath}/data/launcher/backgrounds/mobile/1.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/mobile/2.png") },
                        file = File("${Mother.systemPath}/data/launcher/backgrounds/mobile/2.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/mobile/3.png") },
                        file = File("${Mother.systemPath}/data/launcher/backgrounds/mobile/3.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/mobile/4.png") },
                        file = File("${Mother.systemPath}/data/launcher/backgrounds/mobile/4.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/mobile/5.png") },
                        file = File("${Mother.systemPath}/data/launcher/backgrounds/mobile/5.png"),
                        parent = this
                    )
                } else {
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/desktop/1.png") },
                        file = File("${Mother.systemPath}/data/launcher/backgrounds/desktop/1.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/desktop/2.png") },
                        file = File("${Mother.systemPath}/data/launcher/backgrounds/desktop/2.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/desktop/3.png") },
                        file = File("${Mother.systemPath}/data/launcher/backgrounds/desktop/3.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/desktop/4.png") },
                        file = File("${Mother.systemPath}/data/launcher/backgrounds/desktop/4.png"),
                        parent = this
                    )
                    Image(
                        modifier = Modifier.size(135).padding(5).onClick { setBG("backgrounds/desktop/5.png") },
                        file = File("${Mother.systemPath}/data/launcher/backgrounds/desktop/5.png"),
                        parent = this
                    )
                }
            }
            checkbox(
                get = mother.systemLauncher.getAppsCentering(),
                set = { v -> mother.systemLauncher.setAppsCentering(v) },
                text = "Center apps",
                parent = this
            )
            checkbox(
                get = mother.systemLauncher.getTextDisplay(),
                set = { v -> mother.systemLauncher.setTextDisplay(v) },
                text = "Display app names",
                parent = this
            )
            if (mother.systemLauncher.getTextDisplay()) {
                checkbox(
                    get = mother.systemLauncher.getTextDark(),
                    set = { v -> mother.systemLauncher.setTextDark(v) },
                    text = "Dark app names",
                    parent = this
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth().height(50),
                text = "Screen",
                textSize = 24,
                textColor = Color.BLACK,
                parent = this
            )
            Text(
                modifier = Modifier.fillMaxWidth().height(50).paddingLeft(10),
                text = "Desktop",
                textSize = 24,
                textColor = Color.BLACK,
                textAlign = TextAlignment.Left(),
                parent = this
            )
            for (r in GraphicServiceImpl.RESOLUTIONS.R_ALL) {
                Button(
                    modifier = Modifier.fillMaxWidth().height(50).onClick { gs.setScreenResolution(r) },
                    parent = this
                ).layout {
                    Text(
                        modifier = Modifier.fillMaxSize(),
                        text = "${r.x.toInt()}x${r.y.toInt()}",
                        textSize = 24,
                        textColor = Color.BLACK,
                        parent = this
                    )
                }
            }
            Text(
                modifier = Modifier.fillMaxWidth().height(50).paddingLeft(10),
                text = "Mobile",
                textSize = 24,
                textColor = Color.BLACK,
                textAlign = TextAlignment.Left(),
                parent = this
            )
            for (s in GraphicServiceImpl.RESOLUTIONS.R_ALL) {
                val r = s.swap()
                Button(
                    modifier = Modifier.width(100).height(50).onClick { gs.setScreenResolution(r) },
                    parent = this
                ).layout {
                    Text(
                        modifier = Modifier.fillMaxSize(),
                        text = "${r.x.toInt()}x${r.y.toInt()}",
                        textSize = 24,
                        textColor = Color.BLACK,
                        parent = this
                    )
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
                textAlign = TextAlignment.Left(),
                textColor = Color.BLACK,
                parent = this
            )
        }
    }
    private fun setBG(path: String) {
        mother.systemLauncher.setBackground(path)
    }
}