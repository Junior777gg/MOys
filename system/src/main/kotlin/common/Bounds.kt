package common

data class Bounds(
    val x1: Double,
    val y1: Double,
    val x2: Double,
    val y2: Double,
    val onClick: (() -> Unit)?,
    val onHold: (() -> Unit)?
)