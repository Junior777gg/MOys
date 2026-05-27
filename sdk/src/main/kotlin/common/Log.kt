package common

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.pathString

/**
 * Basic system logger.
 */
object Log {
    //Disable if debug messages are not needed.
    private const val DEBUG_CTX=true
    private var logFile: File? = null

    fun initialize(logPath: String) {
        if(logFile==null) {
            val time=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
            val p=Path(logPath)
            p.createDirectories()
            logFile=File("${p.pathString}/${time}.log")
            logFile?.writeText("MOys 0.0.1\n$time\n")
        }
        dbg("Log will be written to: \"${logFile?.path}\"")
    }

    fun dbg(v: String) {
        if(!DEBUG_CTX) return
        print("\u001b[90m")
        deliver(timeCode("DBG")+v)
        println("\u001b[0m")
    }
    fun info(v: String) {
        deliver(timeCode("INF")+v)
        println("")
    }
    fun warn(v: String) {
        print("\u001b[33m")
        deliver(timeCode("WRN")+v)
        println("\u001b[0m")
    }
    fun error(v: String) {
        print("\u001b[31m")
        deliver(timeCode("ERR")+v)
        println("\u001b[0m")
    }
    fun error(m: String, e: Exception, stackTrace: Boolean = true) {
        print("\u001b[31m")
        deliver("${timeCode("ERR")} $m \"${e.message}\"")
        if(DEBUG_CTX&&stackTrace ) {
            println("")
            deliver(e.stackTraceToString())
        }
        println("\u001b[0m")
    }
    private fun deliver(message: String) {
        print(message)
        logFile?.appendText(message+"\n")
    }
    private fun timeCode(code: String): String {
        return "[${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))} $code]: "
    }
}