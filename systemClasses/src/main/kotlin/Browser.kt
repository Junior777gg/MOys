class РКН(
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI
): Activity{
    override fun main() {
        gs.setContent {
            ОлегСогрешил_С_дровой_собакой()
        }
        gs.redraw()
    }

    fun MutableList<View>.ОлегСогрешил_С_дровой_собакой(){
        Column
    }
}