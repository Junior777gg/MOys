import java.awt.Color


class TestApp(
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI
) : Activity{
    override fun main() {
        gs.setContent(true){
            LazyColumn(modifier = Modifier.fillMaxSize().background(Color.CYAN), this).layout {
                TextField(modifier = Modifier.width(640).height(100),"",15, Color.black, this)
                for (i in 0..10){
                    Button(modifier = Modifier.size(640).background(Color.YELLOW),this)
                    Button(modifier = Modifier.size(640).background(Color.GREEN),this)
                }
            }
        }
        gs.redraw()
    }
}