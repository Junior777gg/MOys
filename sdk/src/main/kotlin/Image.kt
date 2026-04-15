import modifier.Modifier
import java.io.File

class Image(
    override val modifier: Modifier,
    val file: File,
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