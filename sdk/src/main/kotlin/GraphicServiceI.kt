interface GraphicServiceI {
    fun setContent(itIsNewScreen: Boolean = false, lambda: MutableList<View>.() -> Unit)
    fun popBackStack()
    fun redraw()
    fun injectUI(lambda: MutableList<View>.() -> Unit)
    fun cancelInject()
    fun getSystemResource(path: String):String
}