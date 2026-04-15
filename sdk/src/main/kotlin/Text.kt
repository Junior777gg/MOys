import common.Color
import modifier.Modifier
import modifier.TextAlignment

class Text(
    override val modifier: Modifier,
    var text: String,
    var textSize: Int = 10,
    var textColor: Color = Color.WHITE,
    val textAlign: Int = TextAlignment.Center(),
    override val parent: MutableList<View>
) : View {
    override val children: MutableList<View> = mutableListOf()
    init {
        parent.add(this)
    }
    override fun layout(lambda: MutableList<View>.() -> Unit){
        children.lambda()
    }
}