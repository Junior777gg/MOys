package common

data class Bounds(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val onClick: (() -> Unit)?,
    val onHold: (() -> Unit)?
)