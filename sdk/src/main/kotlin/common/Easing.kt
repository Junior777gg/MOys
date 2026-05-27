package common

import java.util.function.Function
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class Easing(val func: Function<Double, Double>) {
    LINEAR(Function { v: Double -> v }),
    QUAD_IN(Function { v: Double -> v * v }),
    QUAD_OUT(Function { v: Double -> -v * (v - 2) }),
    QUAD_IN_OUT(Function { v: Double -> if (v < 0.5) 2 * v * v else -2 * v * v + 4 * v - 1 }),
    CUBIC_IN(Function { v: Double -> v * v * v }),
    CUBIC_OUT(Function { v: Double ->
        var v = v
        v-=1
        v * v * v + 1
    }),
    CUBIC_IN_OUT(Function { v: Double ->
        var v = v
        v *= 2.0
        if (v < 1) return@Function 0.5 * v * v * v
        v -= 2.0
        0.5 * (v * v * v + 2)
    }),
    QUART_IN(Function { v: Double -> v * v * v * v }),
    QUART_OUT(Function { v: Double -> 1 - (1 - v).pow(4.0) }),
    QUART_IN_OUT(Function { v: Double -> if (v < 0.5) 8 * v * v * v * v else 1 - (-2 * v + 2).pow(4.0) / 2 }),
    SINE_IN(Function { v: Double -> 1 - cos((v * java.lang.Math.PI) / 2) }),
    SINE_OUT(Function { v: Double -> sin((v * java.lang.Math.PI) / 2) }),
    SINE_IN_OUT(Function { v: Double -> -0.5 * (cos(java.lang.Math.PI * v) - 1) }),
    EXPO_IN(Function { v: Double -> if (v == 0.0) 0.0 else 2.0.pow(10 * (v - 1)) }),
    EXPO_OUT(Function { v: Double -> if (v == 1.0) 1.0 else 1 - 2.0.pow(-10 * v) }),
    EXPO_IN_OUT(Function { v: Double ->
        if (v == 0.0) 0.0 else if (v == 1.0) 1.0 else if (v < 0.5) 0.5 * 2.0.pow((20 * v) - 10) else -0.5 * 2.0.pow(
            (-20 * v) + 10
        ) + 1
    }),
    CIRC_IN(Function { v: Double -> 1 - sqrt(1 - v.pow(2.0)) }),
    CIRC_OUT(Function { v: Double -> sqrt(1 - (v - 1).pow(2.0)) }),
    CIRC_IN_OUT(Function { v: Double ->
        if (v < 0.5) (1 - sqrt(1 - (2 * v).pow(2.0))) / 2 else (sqrt(
            1 - (-2 * v + 2).pow(
                2.0
            )
        ) + 1) / 2
    }),
    BOUNCE_OUT(Function { v: Double ->
        val numerator=7.5625
        val denominator=2.75

        if (v < 1 / denominator) numerator * v * v
        else if (v < 2 / denominator) {
            val v2 = v - 1.5 / denominator
            numerator * v2 * v2 + 0.75
        } else if (v < 2.5 / denominator) {
            val v2 = v - 2.25 / denominator
            numerator * v2 * v2 + 0.9375
        } else {
            val v2 = v - 2.625 / denominator
            numerator * v2 * v2 + 0.984375
        }
    }),
    BOUNCE_IN(Function { v: Double ->
        1.0 - BOUNCE_OUT.func.apply(1.0 - v)
    }),
    BOUNCE_IN_OUT(Function { v: Double ->
        if (v < 0.5) 0.5 * (1.0 - BOUNCE_OUT.func.apply(1.0 - 2 * v))
        else 0.5 * (1.0 + BOUNCE_OUT.func.apply(2 * v - 1.0))
    }),
    ;

    fun apply(v: Double): Double {
        return func.apply(Math.clamp(v, -1.0, 1.0))
    }
    fun apply(v: Float): Float {
        return func.apply(Math.clamp(v.toDouble(), -1.0, 1.0)).toFloat()
    }
}