sealed class HorizontalAlignment(val orientation : Int){
    companion object{
        const val LEFT = 0
        const val RIGHT = 1
        const val CENTER = 2
    }
    class Left:HorizontalAlignment(LEFT)
    class Right:HorizontalAlignment(RIGHT)
    class Center:HorizontalAlignment(CENTER)
}
sealed class VerticalAlignment(val orientation : Int){
    companion object{
        const val TOP = 4
        const val BOTTOM = 5
        const val CENTER = 6
    }
    class Top:VerticalAlignment(TOP)
    class Bottom:VerticalAlignment(BOTTOM)
    class Center:VerticalAlignment(CENTER)
}
