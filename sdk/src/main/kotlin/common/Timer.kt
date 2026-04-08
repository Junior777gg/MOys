package common

import java.lang.Exception

/**
 * Simple timer executed on independent thread in given interval.
 * Outside of testing might be useful for periodic updates, animations and status-bars.
*/
@Deprecated("Causes main thread to freeze.")
object Timer {
    private var timerTread: Thread?=null
    private var isRunning=false
    private var callbacks=mutableListOf<TimerCallback>()

    /**Adds [callback] to the stack and calls it every [intervalMs] milliseconds.*/
    fun subscribe(callback: (currentTimeMs: Long)->Unit, intervalMs: Long=1000L) {
        synchronized(callbacks) {
            callbacks.add(TimerCallback(intervalMs, callback))
        }
        if(!isRunning) start()
    }
    /**
     * Removes all instances of [callback].
     * TODO: Disallow disabling system timer callbacks if tried not by system itself.
    */
    fun unsubscribe(callback: (Long)->Unit) {
        synchronized(callbacks) {
            callbacks.removeAll { it.callback==callback }
        }
    }

    /**Starts timer stack execution (automatically starts if a single callback is added).*/
    fun start() {
        if(isRunning) return
        isRunning = true

        /*CoroutineScope(Dispatchers.Default).launch {
            val startTime = System.currentTimeMillis()
            var lastTick=startTime
            //Start execution of the timer stack.
            while(isRunning) {
                val now = System.currentTimeMillis()
                //Check all timers.
                synchronized(callbacks) {
                    //Copy the callbacks list to prevent object blocking.
                    val snapshot = callbacks.toList()
                    //Execute all callbacks if time is met.
                    for(c in snapshot) {
                        if(now-c.lastTriggered>=c.intervalMs) {
                            try {
                                c.callback(now)
                                c.lastTriggered = now
                            } catch (e: Exception) {
                                //Log.warn("Timer callback failed to run: ${e.message}")
                            }
                        }
                    }
                }
                //Sync timer stack to be executed ONLY in 60 or fewer FPS.
                val sleepMs = (lastTick+16)-(System.currentTimeMillis()-startTime)
                //Log.dbg("Timer stack is alive")
                if(sleepMs>0) {
                    try {
                        delay(sleepMs)
                    } catch (_: InterruptedException) { }
                }
                lastTick = System.currentTimeMillis()
            }
        }*/
    }
    /**
     * Fully stops the timer stack.
     * TODO: Disallow stopping the timer stack from SDK. Only allow the system to stop it.
    */
    fun stop() {
        isRunning = false
        timerTread?.join(1000)
        timerTread = null
        synchronized(callbacks) { callbacks.clear() }
    }

    /**Basic structure that holds timer's callback, and it's internal timer.*/
    private data class TimerCallback(
        val intervalMs: Long,
        val callback: (Long)->Unit,
        var lastTriggered: Long=0L
    )
}