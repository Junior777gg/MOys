class Stack<T> {
    private val list = mutableListOf<T>()
    fun push(value: T) {
        list.add(value)
    }

    fun popBackStack() {
        list.removeLast()
    }

    fun peek(): T = list.last()
    fun stackSize(): Int = list.size
}