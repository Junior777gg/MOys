class Box(
    override val modifier: Modifier, override val parent: MutableList<View>,
) : View{
    override val children: MutableList<View> = mutableListOf()
    init {
        parent.add(this)
    }
    override fun layout(lambda: MutableList<View>.() -> Unit){
        children.lambda()
    }
}