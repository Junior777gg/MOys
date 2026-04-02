import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Basic system logger.
 */
object Log {
    //Disable if debug messages are not needed.
    private const val DEBUG_CTX=true;

    fun dbg(v: String) {
        if(!DEBUG_CTX) return
        print("\u001b[90m")
        timecode("DBG")
        println("$v\u001b[0m")
    }
    fun info(v: String) {
        timecode("INF")
        println(v)
    }
    fun warn(v: String) {
        print("\u001b[33m")
        timecode("WRN")
        println("$v\u001b[0m")
    }
    fun error(v: String) {
        print("\u001b[31m")
        timecode("ERR")
        println("$v\u001b[0m")
    }
    private fun timecode(code: String) {
        print("["+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+" $code]: ")
    }
}