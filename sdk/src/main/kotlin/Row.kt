import modifier.HorizontalArrangement
import modifier.Modifier
import modifier.VerticalAlignment

class Row(
    override val modifier: Modifier,
    override val parent: MutableList<View>,
    val verticalAlignment: VerticalAlignment = VerticalAlignment.Center(),
    val horizontalArrangement: HorizontalArrangement = HorizontalArrangement.Center()) : View {
    override val children: MutableList<View> = mutableListOf()
    override var nodes: Nodes? = null
    init {
        parent.add(this)
    }
    override fun layout(lambda: MutableList<View>.() -> Unit){
        children.lambda()
    }
}