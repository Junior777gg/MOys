import java.awt.Color

class Text(
    override val modifier: Modifier,
    var text: String,
    var textSize: Int = 10,
    var textColor: Color = Color.WHITE,
    val textAlign: Int = CENTER,
    override val parent: MutableList<View>
) : View {
    companion object{
        const val LEFT = 0
        const val TOP = 1
        const val RIGHT = 2
        const val BOTTOM = 3
        const val CENTER = 4
    }
    override val children: MutableList<View> = mutableListOf()
    init {
        parent.add(this)
    }
    override fun layout(lambda: MutableList<View>.() -> Unit){
        children.lambda()
    }
}