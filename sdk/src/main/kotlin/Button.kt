import modifier.Modifier

class Button(override val modifier: Modifier, override val parent: MutableList<View>) : View {
    override val children = mutableListOf<View>()
    override var nodes: Nodes? = null
    init {
        parent.add(this)
    }

    override fun layout(
        lambda: MutableList<View>.() -> Unit
    ) {
        children.lambda()
    }
}