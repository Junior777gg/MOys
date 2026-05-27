import common.Easing

class Animator(
    val durationMillis: Long,
    val type: Int,
    val easing: Easing = Easing.LINEAR,
    val argument: Any? = null,
    val animators: Array<Animator>? = null,
) {
    var parentView: View? = null
    companion object {
        const val NONE = 0
        const val FADEIN = 1
        const val FADEOUT = 2
        const val SLIDE_HORIZONTALLY = 3
        const val SLIDE_VERTICALLY = 4
        const val SCALE_X = 5
        const val SCALE_Y = 6
        const val ROTATE = 7
        const val SHAKE = 8
        const val COLORFADE = 9
        const val SLIME = 10

    }
}