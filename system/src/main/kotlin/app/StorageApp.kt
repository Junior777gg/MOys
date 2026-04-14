package app

import Activity
import Button
import Column
import GraphicServiceI
import StorageServiceI
import DeviceManagerI
import View
import Image
import LazyColumn
import Mother
import Row
import Text
import background
import fillMaxSize
import fillMaxWidth
import height
import onClick
import paddingBottom
import paddingLeft
import paddingTop
import size
import width
import java.awt.Color
import java.io.File
import kotlin.io.path.Path

class StorageApp(
    val mother: Mother,
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI
) : Activity {
    private var currentPath = "/"
    private var stuckInInstallation = false
    private var installationAppPath = ""
    private val iconList = listOf("folder.png","file.png","jarp.png", "archive_file.png", "audio_file.png", "image_file.png", "text_file.png", "unknown_file.png")
    override fun main() {
        gs.setContent(true){
            buildUI()
        }
        gs.redraw()
    }

    override fun onNavigationBack(): Boolean {
        if(stuckInInstallation) {
            stuckInInstallation=false
            gs.cancelInject()
            gs.redraw()
            return false
        }
        tryGoBack()
        return true
    }

    fun tryGoBack() {
        if(currentPath=="/") return
        currentPath = Path(currentPath).parent.toString()
    }

    fun MutableList<View>.installationPopup() {
        val appFile = File(installationAppPath)
        val manifest = mother.unpackApp(appFile)
        if (manifest != null) {
            Column(modifier = Modifier.fillMaxSize().background(Color(0,0,0,100)), parent = this).layout {
                Column(modifier = Modifier.width(250).height(250).background(Color.LIGHT_GRAY), verticalArrangement = VerticalArrangement.Top(), parent = this).layout {
                    Text(modifier = Modifier.fillMaxWidth().height(20).paddingTop(10), text = "Install app?", textSize = 18, parent = this)
                    Text(modifier = Modifier.fillMaxWidth().height(20).paddingTop(10), text = manifest.app_name, textSize = 12, parent = this)
                    Column(modifier = Modifier.fillMaxSize().background(Color(0,0,0,0)), verticalArrangement = VerticalArrangement.Bottom(), parent = this).layout {
                        Button(modifier = Modifier.fillMaxWidth().height(40).background(Color.WHITE).onClick {
                            mother.installApp(File(installationAppPath))
                            stuckInInstallation=false
                            gs.cancelInject()
                            gs.redraw()
                        }, this).layout { Text(modifier = Modifier.fillMaxSize(), textColor = Color.BLACK, text = "Install", textSize = 12, parent = this) }
                        Button(modifier = Modifier.fillMaxWidth().height(40).background(Color.WHITE).onClick {
                            stuckInInstallation=false
                            gs.cancelInject()
                            gs.redraw()
                        }, this).layout { Text(modifier = Modifier.fillMaxSize(), textColor = Color.BLACK, text = "Cancel", textSize = 12, parent = this) }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().background(Color(0,0,0,100)), parent = this).layout {
                Column(modifier = Modifier.width(300).height(150).background(Color.LIGHT_GRAY), verticalArrangement = VerticalArrangement.Top(), parent = this).layout {
                    Text(modifier = Modifier.fillMaxWidth().height(20).paddingTop(10), text = "Invalid app", textSize = 18, parent = this)
                    Text(modifier = Modifier.fillMaxWidth().height(20).paddingTop(10), text = "Selected app has invalid manifest or is corrupted.", textSize = 12, parent = this)
                    Column(modifier = Modifier.fillMaxSize().background(Color(0,0,0,0)), verticalArrangement = VerticalArrangement.Bottom(), parent = this).layout {
                        Button(modifier = Modifier.fillMaxWidth().height(40).background(Color.WHITE).onClick {
                            stuckInInstallation=false
                            gs.cancelInject()
                            gs.redraw()
                        }, this).layout { Text(modifier = Modifier.fillMaxSize(), textColor = Color.BLACK, text = "Cancel", textSize = 12, parent = this) }
                    }
                }
            }
        }
    }

    fun MutableList<View>.buildUI() {
        val files = File(currentPath).listFiles()
        LazyColumn(modifier = Modifier.fillMaxSize().paddingBottom(60), this).layout {
            files.forEach {
                file(it)
            }
        }
    }

    fun MutableList<View>.file(file: File){
        Row(modifier = Modifier.height(80).fillMaxWidth().onClick {
            if (file.isDirectory) {
                currentPath = file.absolutePath
                main()
            } else if (file.extension=="jarp") {
                stuckInInstallation = true
                installationAppPath = file.absolutePath
                gs.injectUI {
                    installationPopup()
                }
                gs.redraw()
            }
        }, horizontalArrangement = HorizontalArrangement.Left(), parent = this).layout {
            //Determine icon.
            var iconId=0
            if (file.isFile) {
                iconId=1
                when (file.extension) {
                    "jarp"->iconId=2
                    "zip","rar","7z","gz","tar"->iconId=3
                    "mp3","wav","ogg"->iconId=4
                    "png","jpg","jpeg"->iconId=5
                    "txt","md"->iconId=6
                }
            }
            //Place icon if the file is
            if (file.isFile || file.isDirectory)
                Image(modifier = Modifier.size(60), File("${Mother.systemPath}/data/storage/${iconList[iconId]}"),this)
            Text(modifier = Modifier.height(60).fillMaxWidth(), file.name,17, Color.black, parent = this)
        }
    }

}