class Button(override val modifier: Modifier,val cornerRadius:Int = 0,  override val parent: MutableList<View>) : View {
    override val children = mutableListOf<View>()

    init {
        parent.add(this)
    }

    override fun layout(
        lambda: MutableList<View>.() -> Unit
    ) {
        children.lambda()
    }
}