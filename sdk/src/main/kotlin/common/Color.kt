package common

import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round

class Color(var r: Int, var g: Int, var b: Int, var a: Int) {
    companion object {
        val WHITE = Color(255)
        val LIGHT_GRAY = Color(192)
        val GRAY = Color(128)
        val DARK_GRAY = Color(64)
        val BLACK = Color(0)
        val TRANSPARENT = Color(0,0,0,0)

        val RED = Color(255,0,0)
        val GREEN = Color(0,255,0)
        val BLUE = Color(0,0,255)

        val PINK = Color(255, 175, 175)
        val ORANGE = Color(255, 200, 0)
        val YELLOW = Color(255, 255, 0)
        val MAGENTA = Color(255, 0, 255)
        val CYAN = Color(0, 255, 255)

        fun fromHex(hex: String): Color {
            var code=hex.trim().removePrefix("#")
            //Cast #RGB to #RRGGBB.
            if (code.length==3) code=code.map { it->"$it$it" }.joinToString("")
            //Cast #RGBA to #RRGGBBAA.
            if (code.length==4) code=code.map { it->"$it$it" }.joinToString("")
            //Check if length is valid.
            if (code.length !in listOf<Int>(6,8)) {
                Log.error("Given HEX-string must be in one of formats: #RGB, #RRGGBB, #RGBA or #RRGGBBAA")
                return BLACK
            }
            //Extract color data.
            var r=code.substring(0,2).toInt(16)
            var g=code.substring(2,4).toInt(16)
            var b=code.substring(4,6).toInt(16)
            var a=if(code.length==8) code.substring(6,8).toInt(16) else 255
            return Color(r,g,b,a)
        }
        fun fromCMYK(c: Float, m: Float, y: Float, k: Float): Color {
            return Color(255*(1-c)*(1-k),255*(1-m)*(1-k),255*(1-y)*(1-k))
        }
        fun fromHSV(h: Float, s: Float, v: Float): Color {
            var r=0f
            var g=0f
            var b=0f
            val H=h/360
            val S=s/360
            val V=v/360

            val i=floor(H*6)
            val f=H*6-i;
            val p=V*(1-S);
            val q=V*(1-f*S);
            val t=V*(1-(1-f)*S);

            when (i % 6) {
                0f->{r=v;g=t;b=p}
                1f->{r=q;g=v;b=p}
                2f->{r=p;g=v;b=t}
                3f->{r=p;g=q;b=v}
                4f->{r=t;g=p;b=v}
                5f->{r=v;g=p;b=q}
            }

            return Color(round(r * 255).toInt(), round(g * 255).toInt(), round(b * 255).toInt());
        }
    }
    //Aliases.
    var red: Int
        get() = r
        set(v) {r=v}
    var green: Int
        get() = g
        set(v) {g=v}
    var blue: Int
        get() = b
        set(v) {g=v}
    var alpha: Int
        get() = a
        set(v) {a=v}
    //Helper constructors.
    constructor(r: Double, g: Double, b: Double, a: Double) : this((r*255).toInt(),(g*255).toInt(),(b*255).toInt(),(a*255).toInt())
    constructor(r: Float, g: Float, b: Float, a: Float) : this((r*255).toInt(),(g*255).toInt(),(b*255).toInt(),(a*255).toInt())

    constructor(r: Float, g: Float, b: Float) : this((r*255).toInt(),(g*255).toInt(),(b*255).toInt(),255)
    constructor(r: Double, g: Double, b: Double) : this((r*255).toInt(),(g*255).toInt(),(b*255).toInt(),255)
    constructor(r: Int, g: Int, b: Int) : this(r,g,b,255)

    constructor(c: Int) : this(c,c,c,255)
    constructor(c: Float) : this((c*255).toInt(),(c*255).toInt(),(c*255).toInt(),255)
    constructor(c: Double) : this((c*255).toInt(),(c*255).toInt(),(c*255).toInt(),255)
    //Basic operators.
    operator fun plus(o: Color) = Color(r+o.r, g+o.g, b+o.b, a+o.a)
    operator fun minus(o: Color) = Color(r-o.r, g-o.g, b-o.b, a-o.a)
    operator fun times(o: Color) = Color(floatRed()*o.floatRed(), floatGreen()*o.floatGreen(), floatBlue()*o.floatBlue(), floatAlpha()*o.floatAlpha())
    operator fun div(o: Color) = Color(floatRed()/o.floatRed(), floatGreen()/o.floatGreen(), floatBlue()/o.floatBlue(), floatAlpha()/o.floatAlpha())
    operator fun rem(o: Color) = Color(floatRed()%o.floatRed(), floatGreen()%o.floatGreen(), floatBlue()%o.floatBlue(), floatAlpha()%o.floatAlpha())
    /**Converts this color to HEX-string.*/
    fun toHex(includeAlpha: Boolean = false): String {
        return if(includeAlpha) "#%02X%02X%02X%02X".format(r,g,b,a)
        else "#%02X%02X%02X".format(r,g,b)
    }
    /**Converts this color to CMYK color list.*/
    fun toCMYK(): List<Float> {
        val R=floatRed()
        val G=floatGreen()
        val B=floatBlue()
        var K=R
        if (G>K) K=G
        if (B>K) K=B
        K=1-K
        return listOf<Float>((1-R-K)/(1-K),(1-G-K)/(1-K),(1-B-K)/(1-K),K)
    }
    /**Converts this color to HSV color list.*/
    fun toHSV(): List<Float> {
        val R=floatRed()
        val G=floatGreen()
        val B=floatBlue()

        var Max=R
        if (G>Max) Max=G
        if (B>Max) Max=B
        var Min=R
        if (G<Min) Min=G
        if (B<Min) Min=B
        var h=Max
        var s=Max
        var v=Max

        val delta=Max-Min
        s=if (Max==0f) 0f else delta/Max

        if (Max==Min) h=0f
        else {
            when(Max) {
                R->h=(G-B)/delta+(if(G<B) 6f else 0f)
                G->h=(B-R)/delta+2f
                B->h=(R-G)/delta+4f
            }
            h/=6
        }
        return listOf<Float>(h*360,s*360,v*360)
    }
    /**Converts red to float.*/
    fun floatRed(): Float=min(r/255f,1f)
    /**Converts green to float.*/
    fun floatGreen(): Float=min(g/255f,1f)
    /**Converts blue to float.*/
    fun floatBlue(): Float=min(b/255f,1f)
    /**Converts alpha to float.*/
    fun floatAlpha(): Float=min(a/255f,1f)
    /**Converts all values to floats.*/
    fun toFloating(): List<Float> {
        return listOf<Float>(floatRed(),floatGreen(),floatBlue(),floatAlpha())
    }
}