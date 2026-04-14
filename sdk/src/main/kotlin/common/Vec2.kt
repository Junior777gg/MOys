package common

import kotlin.math.sqrt

class Vec2(var x: Double, var y: Double) {
    constructor(x: Int, y: Int) : this(x.toDouble(),y.toDouble())
    constructor(x: Float, y: Float) : this(x.toDouble(),y.toDouble())
    //Basic operators.
    operator fun unaryMinus() = Vec2(-x, -y)
    operator fun plus(o: Vec2) = Vec2(x+o.x, y+o.y)
    operator fun minus(o: Vec2) = Vec2(x-o.x, y-o.y)
    operator fun times(o: Vec2) = Vec2(x*o.x, y*o.y)
    operator fun div(o: Vec2) = Vec2(x/o.x, y/o.y)
    operator fun rem(o: Vec2) = Vec2(x%o.x, y%o.y)
    //Scalar operators.
    operator fun times(o: Double) = Vec2(x*o, y*o)
    operator fun div(o: Double) = Vec2(x/o, y/o)
    operator fun rem(o: Double) = Vec2(x%o, y%o)
    operator fun times(o: Float) = Vec2(x*o, y*o)
    operator fun div(o: Float) = Vec2(x/o, y/o)
    operator fun rem(o: Float) = Vec2(x%o, y%o)
    operator fun times(o: Int) = Vec2(x*o, y*o)
    operator fun div(o: Int) = Vec2(x/o, y/o)
    operator fun rem(o: Int) = Vec2(x%o, y%o)
    //Vec2i-compatibility operators.
    operator fun plus(o: Vec2i) = Vec2(x+o.x, y+o.y)
    operator fun minus(o: Vec2i) = Vec2(x-o.x, y-o.y)
    operator fun times(o: Vec2i) = Vec2(x*o.x, y*o.y)
    operator fun div(o: Vec2i) = Vec2(x/o.x, y/o.y)
    operator fun rem(o: Vec2i) = Vec2(x%o.x, y%o.y)
    //Utilities.
    infix fun dot(o: Vec2): Double = x*o.x+y*o.y
    fun delta(): Double = sqrt(x*x+y*y)
    fun normalized(): Vec2 {
        val d=delta()
        return if (d>0) Vec2(x/d, y/d) else Vec2(0,0)
    }
    //Transformations.
    fun swap(): Vec2 = Vec2(y,x)
    fun toInteger(): Vec2i = Vec2i(x.toInt(),y.toInt())
}
//Handlers for "incorrect" operations.
operator fun Double.times(o: Vec2) = Vec2(this*o.x, this*o.y)
operator fun Int.times(o: Vec2) = Vec2(this*o.x, this*o.y)
operator fun Float.times(o: Vec2) = Vec2(this*o.x, this*o.y)