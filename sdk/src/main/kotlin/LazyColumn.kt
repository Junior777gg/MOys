import modifier.FillMaxHeight
import modifier.Height
import modifier.HorizontalAlignment
import modifier.Modifier
import modifier.Size

class LazyColumn(
    override val modifier: Modifier,
    override val parent: MutableList<View>,
    val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Center(),
) : View {
    override val children = mutableListOf<View>()
    override var nodes: Nodes? = null
    var offset = 0.0
    private var calculatedContentHeight: Double? = null
    init {
        parent.add(this)
    }

    /**Checks and clamps scroll bounds.*/
    fun updateScrollBound(containerHeight: Double) {
        if (calculatedContentHeight==null) {
            calculatedContentHeight = children.sumOf { child->
                child.modifier.get<Height>()?.height?.toDouble()
                    ?: child.modifier.get<Size>()?.size?.toDouble()
                    ?: child.modifier.get<FillMaxHeight>()?.let { containerHeight }
                    ?: 0.0
            }
        }
        val minimum = (-calculatedContentHeight!! + containerHeight).coerceAtMost(0.0)
        offset = offset.coerceIn(minimum, 0.0)
    }

    override fun layout(lambda: MutableList<View>.() -> Unit) {
        super.layout(lambda)
        calculatedContentHeight = null
    }
}