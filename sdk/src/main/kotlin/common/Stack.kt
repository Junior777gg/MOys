package common

class Stack<T> {
    private val list = mutableListOf<T>()
    fun push(value: T) {
        list.add(value)
    }

    fun popBack() {
        list.removeLast()
    }

    fun peek(): T = list.last()
    fun size(): Int = list.size
}