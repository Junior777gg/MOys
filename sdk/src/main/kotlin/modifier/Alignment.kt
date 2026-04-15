package modifier

sealed class HorizontalAlignment{
    class Left:HorizontalAlignment()
    class Right:HorizontalAlignment()
    class Center:HorizontalAlignment()
}
sealed class VerticalAlignment{
    class Top:VerticalAlignment()
    class Bottom:VerticalAlignment()
    class Center:VerticalAlignment()
}
