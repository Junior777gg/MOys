import java.awt.Color

class Text(
    override val modifier: Modifier,
    var text: String,
    var textSize: Int = 10,
    var textColor: Color = Color.WHITE,
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