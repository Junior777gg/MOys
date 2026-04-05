sealed class HorizontalArrangement(val position : Int){
    companion object{
        const val LEFT = 7
        const val RIGHT = 8
        const val CENTER = 9
        const val SPASEEVENLY = 10
    }
    class Left:HorizontalArrangement(LEFT)
    class Right:HorizontalArrangement(RIGHT)
    class Center:HorizontalArrangement(CENTER)
    class SpaceEvenly:HorizontalArrangement(SPASEEVENLY)
}
sealed class VerticalArrangement(val position : Int){
    companion object{
        const val TOP = 11
        const val BOTTOM = 12
        const val CENTER = 13
        const val SPASEEVENLY = 14
    }
    class Top:VerticalArrangement(TOP)
    class Bottom:VerticalArrangement(BOTTOM)
    class Center:VerticalArrangement(CENTER)
    class SpaceEvenly:VerticalArrangement(SPASEEVENLY)
}