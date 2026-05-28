package common

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPOutputStream
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
            val p=Path(logPath)
            p.createDirectories()
            logFile=File("${p.pathString}/latest.log")
            var distro="linux-undetected"
            try {
                val path = Paths.get("/etc/os-release")
                if (Files.exists(path)) {
                    val lines = Files.readAllLines(path)
                    for (line in lines) {
                        if (line.startsWith("PRETTY_NAME=")) {
                            distro=
                                line.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].replace(
                                    "\"",
                                    ""
                                )
                        }
                    }
                } else {
                    println("Standard os-release file not found.")
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            logFile?.writeText("Enviroment: MOys\n" +
                    "Version: 0.0.1\n" +
                    "Branch: main\n" +
                    "Start timestamp: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n" +
                    "Distro: $distro\n" +
                    "JRE version: ${System.getProperty("java.version")}\n" +
                    "Loaded classes on startup: ${System.getProperty("java.class.path")}\n" +
                    "--------------------\n")
        }
        dbg("Log will be written to: \"${logFile?.path}\"")
    }
    fun saveLog(logPath: String) {
        val time=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
        val source = Paths.get("$logPath/latest.log")
        val target = Paths.get("$logPath/$time.log")
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        val targetFile=target.toFile()
        val targetArchive = target.pathString+".gz"
        FileInputStream(targetFile).use { input ->
            GZIPOutputStream(FileOutputStream(targetArchive)).use { output ->
                input.copyTo(output)
            }
        }
        targetFile.delete()
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