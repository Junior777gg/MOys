package common

class Vec2i(var x: Int, var y: Int) {
    operator fun unaryMinus() = Vec2i(-x, -y)

    operator fun plus(o: Vec2i) = Vec2i(x+o.x, y+o.y)
    operator fun minus(o: Vec2i) = Vec2i(x-o.x, y-o.y)
    operator fun times(o: Vec2i) = Vec2i(x*o.x, y*o.y)
    operator fun div(o: Vec2i) = Vec2i(x/o.x, y/o.y)
    operator fun rem(o: Vec2i) = Vec2i(x%o.x, y%o.y)

    fun swap(): Vec2i = Vec2i(y,x)
    fun toFloating(): Vec2 = Vec2(x,y)
    fun fromFloating(o: Vec2) {
        x=o.x.toInt()
        y=o.y.toInt()
    }
}