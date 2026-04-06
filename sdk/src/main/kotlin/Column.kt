class Column(
    override val modifier: Modifier,
    override val parent: MutableList<View>,
    val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Center(),
    val verticalArrangement: VerticalArrangement = VerticalArrangement.Center()) : View {
    override val children: MutableList<View> = mutableListOf()

    init {
        parent.add(this)
    }

    override fun layout(lambda: MutableList<View>.() -> Unit) {
        children.lambda()
    }
}