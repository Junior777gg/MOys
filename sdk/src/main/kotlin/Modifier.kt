import java.awt.Color

interface ModifierElements

data class Height(val height: Int) : ModifierElements

data class Width(val width: Int) : ModifierElements

data class Size(val size: Int) : ModifierElements

class FillMaxSize : ModifierElements

data class Padding(val padding: Int) : ModifierElements

data class PaddingTop(val top: Int) : ModifierElements

data class PaddingRight(val right: Int) : ModifierElements

data class PaddingBottom(val bottom: Int) : ModifierElements

data class PaddingLeft(val left: Int) : ModifierElements

data class Background(val color: Color) : ModifierElements

data class OnClick(val onClick: () -> Unit) : ModifierElements

data class OnHold(val onHold: () -> Unit) : ModifierElements

open class Modifier private constructor(
    val elements : List<ModifierElements> = listOf()
    ){
    companion object: Modifier()

    fun add(element: ModifierElements): Modifier {
        return Modifier(elements + element)
    }

    inline fun <reified T : ModifierElements> get(): T? {
        return elements.filterIsInstance<T>().lastOrNull()
    }

}

fun Modifier.height(height: Int) = add(Height(height))
fun Modifier.width(width: Int) = add(Width(width))
fun Modifier.size(size: Int) = add(Size(size))
fun Modifier.background(color: Color) = add(Background(color))
fun Modifier.onClick(onClick: () -> Unit) = add(OnClick(onClick))
fun Modifier.onHold(onHold: () -> Unit) = add(OnHold(onHold))
fun Modifier.padding(padding: Int) = add(Padding(padding))
fun Modifier.paddingTop(padding: Int) = add(PaddingTop(padding))
fun Modifier.paddingRight(padding: Int) = add(PaddingRight(padding))
fun Modifier.paddingBottom(padding: Int) = add(PaddingBottom(padding))
fun Modifier.paddingLeft(padding: Int) = add(PaddingLeft(padding))
fun Modifier.fillMaxSize() = add(FillMaxSize())


