package app

import Activity
import GraphicServiceI
import StorageServiceI
import DeviceManagerI
import View
import Image
import LazyColumn
import Mother
import Row
import Text
import fillMaxSize
import height
import onClick
import paddingBottom
import paddingLeft
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
    private val iconList = listOf("folder.png","file.png","jarp.png", "archive_file.png", "audio_file.png", "image_file.png", "text_file.png", "unknown_file.png")
    override fun main() {
        gs.setContent(true){
            buildUI()
        }
        gs.redraw()
    }

    override fun onNavigationBack(): Boolean {
        tryGoBack()
        return true
    }

    fun tryGoBack() {
        if(currentPath=="/") return
        currentPath = Path(currentPath).parent.toString()
    }

    fun MutableList<View>.buildUI(){
        val files = File(currentPath).listFiles()
        LazyColumn(modifier = Modifier.fillMaxSize().paddingBottom(60), this).layout {
            /*if(currentPath!="/") {
                //Button to return back (may conflict with "back" button in nav)
                Row(modifier = Modifier.height(80).width(640).onClick {
                    tryGoBack()
                    main()
                }, this).layout {
                    Text(modifier = Modifier.height(60).width(640), "...", 17, Color.black, parent = this)
                }
            }*/
            //Iterate files and folders
            files.forEach {
                file(it)
            }
        }
    }

    fun MutableList<View>.file(file: File){
        Row(modifier = Modifier.height(80).width(640).paddingLeft(60).onClick {
            if (file.isDirectory) {
                currentPath = file.absolutePath
                main()
            } else if (file.extension=="jarp") {
                mother.installApp(file)
            }
        }, this).layout {
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
                Image(modifier = Modifier.size(60), File("${mother.getSystemPath()}/data/storage/${iconList[iconId]}"),this)
            Text(modifier = Modifier.height(60).width(640), file.name,17, Color.black, parent = this)
        }
    }

}