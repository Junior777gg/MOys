package common

class Math {
    class Vec2(var x: Double, var y: Double) {
        operator fun Vec2.unaryMinus() = Vec2(-x, -y)

        operator fun Vec2.plus(o: Vec2) = Vec2(x+o.x, y+o.y)
        operator fun Vec2.minus(o: Vec2) = Vec2(x-o.x, y-o.y)
        operator fun Vec2.times(o: Vec2) = Vec2(x*o.x, y*o.y)
        operator fun Vec2.div(o: Vec2) = Vec2(x/o.x, y/o.y)
        operator fun Vec2.rem(o: Vec2) = Vec2(x%o.x, y%o.y)
    }
}