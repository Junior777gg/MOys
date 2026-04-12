package common

import kotlin.math.cos
import kotlin.math.sin

object Math {
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Float, b: Float, t: Float): Float {
        return a+t*(b-a)
    }
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Double, b: Double, t: Double): Double {
        return a+t*(b-a)
    }
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Int, b: Int, t: Int): Int {
        return a+t*(b-a)
    }
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Vec2, b: Vec2, t: Vec2): Vec2 {
        return a+t*(b-a)
    }
    /**Get value from [a] to [b] based on time [t]*/
    fun lerp(a: Vec2i, b: Vec2i, t: Vec2i): Vec2i {
        return a+t*(b-a)
    }

    /**Calculate position on theoretical circle from given [radius] and [angle].*/
    fun positionOnCircle(radius: Float, angle: Float): Vec2 {
        return Vec2(radius*cos(angle*(3.14f/180f)),radius*sin(angle*(3.14f/180f)));
    }
}