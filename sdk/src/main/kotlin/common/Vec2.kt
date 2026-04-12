package common

class Vec2(var x: Double, var y: Double) {
    constructor(x: Int, y: Int) : this(x.toDouble(),y.toDouble())
    constructor(x: Float, y: Float) : this(x.toDouble(),y.toDouble())

    operator fun unaryMinus() = Vec2(-x, -y)

    operator fun plus(o: Vec2) = Vec2(x+o.x, y+o.y)
    operator fun minus(o: Vec2) = Vec2(x-o.x, y-o.y)
    operator fun times(o: Vec2) = Vec2(x*o.x, y*o.y)
    operator fun div(o: Vec2) = Vec2(x/o.x, y/o.y)
    operator fun rem(o: Vec2) = Vec2(x%o.x, y%o.y)

    fun swap(): Vec2 = Vec2(y,x)
    fun toInteger(): Vec2i = Vec2i(x.toInt(),y.toInt())
}