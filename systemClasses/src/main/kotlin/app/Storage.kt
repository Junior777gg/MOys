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
import size
import width
import java.awt.Color
import java.io.File
import kotlin.io.path.Path

class Storage(
    val mother: Mother,
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI
) : Activity {
    private var currentPath = "/"
    private val iconList = listOf("folder.png","file.png","jarp.png")
    override fun main() {
        gs.setContent(true){
            buildUI()
        }
        gs.redraw()
    }

    fun MutableList<View>.buildUI(){
        val files = File(currentPath).listFiles()
        LazyColumn(modifier = Modifier.Companion.fillMaxSize().paddingBottom(60), this).layout {
            if(currentPath!="/") {
                //Button to return back (may conflict with "back" button in nav)
                Row(modifier = Modifier.Companion.height(80).width(640).onClick {
                    currentPath = Path(currentPath).parent.toString()
                    main()
                }, this).layout {
                    Text(modifier = Modifier.height(60).width(640), "...", 17, Color.black, parent = this)
                }
            }
            //Iterate files and folders
            files.forEach {
                file(it)
            }
        }
    }

    fun MutableList<View>.file(file: File){
        Row(modifier = Modifier.Companion.height(80).width(640).onClick {
            if (file.isDirectory){
                currentPath = file.absolutePath
                main()
            } else if (file.extension.toString()=="jarp") mother.installApp(file)
        }, this).layout {
            //Determine icon.
            var iconId=0
            if (file.isFile) {
                iconId=1
                if(file.extension.toString()=="jarp") iconId=2
            }
            //Place icon if the file is
            if (file.isFile || file.isDirectory)
                Image(modifier = Modifier.Companion.size(60), File("${mother.getSystemPath()}/data/storage/${iconList[iconId]}"),this)
            Text(modifier = Modifier.height(60).width(640), file.name,17, Color.black, parent = this)
        }
    }

}