object TextAlignment {
    //Basic attributes (binary)
    const val H_LEFT = 1 shl 0
    const val H_CENTER = 1 shl 1
    const val H_RIGHT = 1 shl 2
    const val V_TOP = 1 shl 3
    const val V_CENTER = 1 shl 4
    const val V_BOTTOM = 1 shl 5
    //Presets.
    const val LEFT_TOP = H_LEFT or V_TOP
    const val LEFT_CENTER = H_LEFT or V_CENTER
    const val LEFT_BOTTOM = H_LEFT or V_BOTTOM
    const val CENTER_TOP = H_CENTER or V_TOP
    const val CENTER_CENTER = H_CENTER or V_CENTER
    const val CENTER_BOTTOM = H_CENTER or V_BOTTOM
    const val RIGHT_TOP = H_RIGHT or V_TOP
    const val RIGHT_CENTER = H_RIGHT or V_CENTER
    const val RIGHT_BOTTOM = H_RIGHT or V_BOTTOM
    //Masks (exporters for bits).
    private const val H_MASK = 0b000111
    private const val V_MASK = 0b111000
    //Various helper methods.
    fun isValidAlignment(align: Int): Boolean {
        val hFlags = align and H_MASK
        val vFlags = align and V_MASK
        return Integer.bitCount(hFlags) == 1 && Integer.bitCount(vFlags) == 1
    }
    fun getHorizontal(align: Int): Int { return align and H_MASK }
    fun getVertical(align: Int): Int { return align and V_MASK }
    //Builder-styled presets.
    fun Center(): Int = CENTER_CENTER
    fun Left(): Int = LEFT_CENTER
    fun Right(): Int = RIGHT_CENTER
    fun Top(): Int = CENTER_TOP
    fun Bottom(): Int = CENTER_BOTTOM
    fun TopLeft(): Int = LEFT_TOP
    fun TopRight(): Int = RIGHT_TOP
    fun BottomLeft(): Int = LEFT_BOTTOM
    fun BottomRight(): Int = RIGHT_BOTTOM
}