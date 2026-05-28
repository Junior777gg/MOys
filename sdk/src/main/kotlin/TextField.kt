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
) : View, IKeyboardContract {
    override val children: MutableList<View> = mutableListOf()
    init {
        parent.add(this)
    }

    override fun onKey(key: Char): Boolean {
        text += key
        return true
    }

    override fun onText(text: String): Boolean {
        this.text += text
        return true
    }

    override fun onErase(amount: Int): Boolean {
        if(text.isEmpty()) return false
        text=text.dropLast(amount)
        return true
    }
}