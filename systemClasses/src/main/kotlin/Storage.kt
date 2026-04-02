import java.awt.Color
import java.io.File

class Storage(
    val mother: Mother,
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI
) : Activity {
    private var currentPath = "/"
    override fun main() {
        gs.setContent(true){
            buildUI()
        }
        gs.redraw()
    }

    fun MutableList<View>.buildUI(){
        val files = File(currentPath).listFiles()
        LazyColumn(modifier = Modifier.fillMaxSize().paddingBottom(60), this).layout {
            files.forEach {
                file(it)
            }
        }
    }

    fun MutableList<View>.file(file: File){
        Row(modifier = Modifier.height(80).width(640).onClick {
            if (file.isDirectory){
                currentPath = file.absolutePath
                main()
            }else if (file.name.contains(".jarp")){
                mother.installApp(file)
            }
        }, this).layout {
            if (file.isFile) {
                Image(modifier = Modifier.size(60), File("/mnt/e/!Programming/!other/MOys/res/file.jpg"),this)
            }else if (file.isDirectory) {
                Image(modifier = Modifier.size(60), File("/mnt/e/!Programming/!other/MOys/res/folder.png"),this)
            }
            Text(modifier = Modifier.height(60).width(640), file.name,17, Color.black,this)
        }
    }

}