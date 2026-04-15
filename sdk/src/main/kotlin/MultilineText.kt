import common.Color
import modifier.Modifier
import modifier.TextAlignment

/**
 * @param text Content of the element.
 * @param textSize Size of given text.
 * @param lineSpacing Spacing between lines (in pixels).
 * @param lineWrap Wrap lines? If true words that don't fit horizontally will be moved to a new line.
 * @param textColor Color of rendered text.
 * @param textAlign Vertical and horizontal alignment of inner text.
*/
class MultilineText(
    override val modifier: Modifier,
    var text: String,
    var textSize: Int = 10,
    var lineSpacing: Int = 2,
    var lineWrap: Boolean = false, //TODO: Implement line wrapping (can be resource and time consuming).
    var textColor: Color = Color.WHITE,
    val textAlign: Int = TextAlignment.Center(),
    override val parent: MutableList<View>
) : View {
    override val children: MutableList<View> = mutableListOf()
    val lines: List<String>
        get() {
            return text
                .replace("\\n","\n") //New lines.
                .replace("\\t","\t") //Tabulation.
                .replace("\\r","") //Windows end lines (remove - they don't work on Linux).
                .replace("\r","")
                .split("\n")
        }
    init {
        parent.add(this)
    }
    override fun layout(lambda: MutableList<View>.() -> Unit){
        children.lambda()
    }
}