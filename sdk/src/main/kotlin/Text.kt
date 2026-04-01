import java.awt.Color

class Text(
    override val modifier: Modifier,
    val text: String,
    val textSize: Int = 10,
    val textColor: Color = Color.WHITE,
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