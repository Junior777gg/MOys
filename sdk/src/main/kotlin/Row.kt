class Row(
    override val modifier: Modifier,
    override val parent: MutableList<View>,
    val verticalAlignment: VerticalAlignment,
    val horizontalArrangement: HorizontalArrangement) : View {
    override val children: MutableList<View> = mutableListOf()
    init {
        parent.add(this)
    }
    override fun layout(lambda: MutableList<View>.() -> Unit){
        children.lambda()
    }
}