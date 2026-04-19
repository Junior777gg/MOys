package app

import Activity
import Button
import Column
import service.GraphicService
import service.StorageService
import service.DeviceManager
import View
import Image
import LazyColumn
import Mother
import MultilineText
import Row
import Text
import common.Color
import common.Log
import impl.GraphicServiceImpl
import modifier.HorizontalArrangement
import modifier.Modifier
import modifier.TextAlignment
import modifier.VerticalArrangement
import modifier.background
import modifier.fillMaxSize
import modifier.fillMaxWidth
import modifier.height
import modifier.onClick
import modifier.paddingBottom
import modifier.paddingTop
import modifier.size
import modifier.width
import java.io.File
import kotlin.io.path.Path

class StorageApp(
    val mother: Mother,
    override val gs: GraphicService,
    override val storage: StorageService,
    override val deviceManager: DeviceManager
) : Activity {
    private var currentPath = "/"
    private var stuckInInstallation = false
    private var fileViewOpen = false
    private var installationAppPath = ""
    private val iconList = listOf("folder.png","file.png","jarp.png", "archive_file.png", "audio_file.png", "image_file.png", "text_file.png", "unknown_file.png")
    override fun main() {
        gs.setContent(true) {
            buildUI()
        }
        gs.redraw()
    }
    fun MutableList<View>.buildUI() {
        val files = File(currentPath).listFiles()
        LazyColumn(modifier = Modifier.fillMaxSize().paddingBottom(60), this).layout {
            files.forEach {
                file(it)
            }
        }
    }
    override fun onNavigationBack(): Boolean {
        if(stuckInInstallation) {
            stuckInInstallation=false
            gs.cancelInject()
            gs.redraw()
            return false
        }
        if(!fileViewOpen) tryGoBack()
        else fileViewOpen=false
        return true
    }
    private fun tryGoBack() {
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
    private fun openImage(img: File) {
        fileViewOpen = true
        gs.setContent(true) {
            Column(modifier = Modifier.fillMaxSize().background(Color.BLACK), parent = this). layout {
                val calcSize=GraphicServiceImpl.getScreenSize()/3
                Image(modifier = Modifier.size(calcSize.x), file = img, parent = this)
            }
        }
    }
    private fun openText(text: File) {
        fileViewOpen = true
        gs.setContent(true) {
            LazyColumn(modifier = Modifier.fillMaxSize().background(Color.BLACK), parent = this). layout {
                MultilineText(modifier = Modifier.fillMaxSize(), textAlign = TextAlignment.TopLeft(), textSize = 16, text = text.readText(), parent = this)
            }
        }
    }

    private fun MutableList<View>.file(file: File){
        Row(modifier = Modifier.height(80).fillMaxWidth().onClick {
            if (file.isDirectory) {
                currentPath = file.absolutePath
                main()
            } else {
                when (file.extension) {
                    "jarp"->{
                        stuckInInstallation = true
                        installationAppPath = file.absolutePath
                        gs.injectUI {
                            installationPopup()
                        }
                    }
                    "zip","rar","7z","gz","tar"->Log.warn("Error: Unzipping files not implemented")
                    "mp3","wav","ogg"->Log.warn("Error: Audio Service not implemented")
                    "png","jpg","jpeg"->openImage(file)
                    "txt","md","cfg","json"->openText(file)
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
                    "txt","md","cfg","json"->iconId=6
                }
            }
            //Place icon if the file is
            if (file.isFile || file.isDirectory)
                Image(modifier = Modifier.size(60), File("${Mother.systemPath}/data/storage/${iconList[iconId]}"), parent = this)
            Text(modifier = Modifier.height(60).fillMaxWidth(), text = file.name, textSize = 17, textColor = Color.BLACK, parent = this)
        }
    }

}