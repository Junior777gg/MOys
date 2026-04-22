import modifier.Modifier

interface View {
    val modifier: Modifier
    val children: MutableList<View>
    val parent: MutableList<View>
    var nodes: Nodes?
    fun layout(lambda: MutableList<View>.() -> Unit){
        children.lambda()
    }
}

