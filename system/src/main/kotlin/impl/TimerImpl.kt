package impl

import service.Timer
import common.Log
import java.lang.Exception
import java.util.Calendar
import kotlin.collections.iterator
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min

/**
 * Simple timer executed on independent thread in given interval.
 * Outside of testing might be useful for periodic updates, animations and status-bars.
*/
object TimerImpl: Timer {
    private var timerTread: Thread?=null
    private var isRunning=false
    private var callbacks=mutableMapOf<String,Timer.TimerCallback>()
    private val SYSTEM_TIMERS=listOf<String>("TOP_PANEL_CLOCK")

    /**Adds [callback] to the stack and calls it every [intervalS] seconds.*/
    override fun subscribe(id: String, callback: (currentTimeMs: Long)->Unit, intervalS: Long) {
        if(callbacks.containsKey(id)) {
            Log.warn("Couldn't assign duplicate timer callback \"$id\"")
            return
        }
        synchronized(callbacks) {
            callbacks.put(id, Timer.TimerCallback(max(intervalS * 1000L, 1000L), callback, System.currentTimeMillis()))
        }
    }
    /**
     * Removes all instances of [callback].
    */
    override fun unsubscribe(id: String) {
        if(SYSTEM_TIMERS.contains(id)) return
        synchronized(callbacks) {
            callbacks.remove(id)
        }
    }

    /**Starts timer stack execution (automatically starts if a single callback is added).*/
    fun start() {
        if(isRunning) return
        isRunning = true

        timerTread = thread(name = "system-timer", isDaemon = true, block = {
            val startTime = System.currentTimeMillis()
            var lastTick = startTime
            var calendar = Calendar.getInstance()
            //Start execution of the timer stack.
            while (isRunning) {
                val now = System.currentTimeMillis()
                //Check all timers.
                synchronized(callbacks) {
                    //Copy the callbacks list to prevent object blocking.
                    val snapshot = callbacks.toMap()
                    //Execute all callbacks if time is met.
                    for (v in snapshot) {
                        val c = v.value
                        if (now - c.lastTriggered >= c.intervalMs) {
                            try {
                                c.callback(now)
                                c.lastTriggered = now
                            } catch (e: Exception) {
                                Log.warn("Timer callback \"${v.key}\" failed to run: ${e.message}")
                            }
                        }
                    }
                }
                //Sync timer stack to be executed every second and aligned to it.
                val elapsed = System.currentTimeMillis() - now
                val sleep = min(1000L - elapsed, 1000L - calendar.get(Calendar.MILLISECOND))
                try {
                    Thread.sleep(sleep)
                } catch (_: InterruptedException) {
                }
                lastTick = System.currentTimeMillis()
            }
        })
        Log.dbg("Timer stack is active")
    }
    /**Fully stops the timer stack. Can be run by system only.*/
    fun stop() {
        isRunning = false
        timerTread?.join(1000)
        timerTread = null
        synchronized(callbacks) { callbacks.clear() }
        Log.dbg("Timer stack has been stopped")
    }
}