package common

import kotlin.math.*

object Math {
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Float, b: Float, t: Float): Float {
        return a+t*(b-a)
    }
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Double, b: Double, t: Float): Double {
        return a+t*(b-a)
    }
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Int, b: Int, t: Float): Int {
        return a+(t*(b-a)).roundToInt()
    }
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Vec2, b: Vec2, t: Float): Vec2 {
        return a+t*(b-a)
    }
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Vec2i, b: Vec2i, t: Float): Vec2i {
        return a+(t*(b-a).toFloating()).toInteger()
    }
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Color, b: Color, t: Float): Color {
        return a+t*(b-a)
    }

    /**Calculate position on theoretical circle from given [radius] and [angle].*/
    fun positionOnCircle(radius: Float, angle: Float): Vec2 {
        return Vec2(
            radius*cos(angle*(java.lang.Math.PI.toFloat()/180f)),
            radius*sin(angle*(java.lang.Math.PI.toFloat()/180f))
        )
    }

    /**Clamps [value] between [min] and [max].*/
    fun clamp(value: Double, min: Double, max: Double): Double {
        return min(max, max(value, min))
    }
    /**Clamps [value] between [min] and [max].*/
    fun clamp(value: Float, min: Float, max: Float): Float {
        return min(max, max(value, min))
    }
}