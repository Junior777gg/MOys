import common.Color
import modifier.Modifier
import modifier.TextAlignment

class TextField(
    override val modifier: Modifier,
    var text: String = "",
    var textSize: Int = 15,
    var textColor: Color = Color.BLACK,
    val textAlign: Int = TextAlignment.Center(),
    override val parent: MutableList<View>,
) : View, IKeyboard {
    override val children: MutableList<View> = mutableListOf()
    override var nodes: Nodes? = null
    init {
        parent.add(this)
    }

    override fun layout(lambda: MutableList<View>.() -> Unit) {
        super.layout(lambda)
    }

    override fun onKeyPress(key: String): Boolean {
        if (key == "<") {
            text = text.dropLast(1)
        } else {
            text += key
        }
        return true
    }
}