package common

class Vec2i(var x: Int, var y: Int) {
    operator fun unaryMinus() = Vec2i(-x, -y)
    //Basic operators.
    operator fun plus(o: Vec2i) = Vec2i(x+o.x, y+o.y)
    operator fun minus(o: Vec2i) = Vec2i(x-o.x, y-o.y)
    operator fun times(o: Vec2i) = Vec2i(x*o.x, y*o.y)
    operator fun div(o: Vec2i) = Vec2i(x/o.x, y/o.y)
    operator fun rem(o: Vec2i) = Vec2i(x%o.x, y%o.y)
    //Scalar operators.
    operator fun times(o: Int) = Vec2i(x*o, y*o)
    operator fun div(o: Int) = Vec2i(x/o, y/o)
    operator fun rem(o: Int) = Vec2i(x%o, y%o)
    //Transformations.
    fun swap(): Vec2i = Vec2i(y,x)
    fun toFloating(): Vec2 = Vec2(x,y)
    fun fromFloating(o: Vec2) {
        x=o.x.toInt()
        y=o.y.toInt()
    }
}
//Handler for "incorrect" operations.
operator fun Int.times(o: Vec2i) = Vec2i(this*o.x, this*o.y)