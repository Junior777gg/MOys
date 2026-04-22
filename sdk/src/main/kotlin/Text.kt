import common.Color
import modifier.Modifier
import modifier.TextAlignment

/**
 * @param text Content of the element.
 * @param textSize Size of given text.
 * @param textColor Color of rendered text.
 * @param textAlign Vertical and horizontal alignment of inner text.
 */
class Text(
    override val modifier: Modifier,
    var text: String,
    var textSize: Int = 10,
    var textColor: Color = Color.WHITE,
    val textAlign: Int = TextAlignment.Center(),
    override val parent: MutableList<View>
) : View {
    override val children: MutableList<View> = mutableListOf()
    override var nodes: Nodes? = null
    init {
        parent.add(this)
    }
    override fun layout(lambda: MutableList<View>.() -> Unit){
        children.lambda()
    }
}