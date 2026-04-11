interface TimerI {
    fun subscribe(id: String, callback: (currentTimeMs: Long)->Unit, intervalS: Long=1L)
    fun unsubscribe(id: String)

    /**Basic structure that holds timer's callback, and it's internal timer.*/
    data class TimerCallback(
        val intervalMs: Long,
        val callback: (Long)->Unit,
        var lastTriggered: Long=0L
    )
}