import java.awt.Color

class TextField(
    override val modifier: Modifier,
    var text: String = "",
    var textSize: Int = 10,
    var textColor: Color = Color.WHITE,
    override val parent: MutableList<View>,
) : View, KeyboardInterface {
    override val children: MutableList<View> = mutableListOf()
    init {
        parent.add(this)
    }
    override fun layout(lambda: MutableList<View>.() -> Unit) {
        super.layout(lambda)
    }

    override fun onKeyPress(key: String): Boolean{
        if (key == "<"){
            text = text.dropLast(1)
        }else{
            text+=key
        }
        return true
    }
}