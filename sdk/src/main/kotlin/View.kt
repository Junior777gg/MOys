
interface View {
    val modifier: Modifier
    val children: MutableList<View>
    val parent: MutableList<View>
    fun layout(lambda: MutableList<View>.() -> Unit){
        children.lambda()
    }
}

