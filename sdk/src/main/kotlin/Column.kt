class Column(override val modifier: Modifier, override val parent: MutableList<View>) : View{
    override val children: MutableList<View> = mutableListOf()
    var scrollOffset: Double = 0.0
    init {
        parent.add(this)
    }
    override fun layout(lambda: MutableList<View>.() -> Unit){
        children.lambda()
    }
}