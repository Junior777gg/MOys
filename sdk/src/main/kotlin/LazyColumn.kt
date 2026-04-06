class LazyColumn(
    override val modifier: Modifier,
    override val parent: MutableList<View>,
    val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Center(),
) : View {
    override val children= mutableListOf<View>()
    var offset = 0.0
    init {
        parent.add(this)
    }

    override fun layout(lambda: MutableList<View>.() -> Unit) {
        super.layout(lambda)
    }
}