import common.Log
import java.net.URL
import java.net.URLClassLoader

class SecurityClassLoader(url: Array<URL>, parent: ClassLoader) : URLClassLoader(url, parent) {
    private val blockedClasses = listOf(
        "java.io.File",
        "java.io.FileInputStream",
        "java.io.FileOutputStream",
        "java.io.RandomAccessFile",
        "java.nio.file.",
        "java.nio.channels.FileChannel",

        // Сеть
        "java.net.Socket",
        "java.net.ServerSocket",
        "java.net.URL",
        "java.net.HttpURLConnection",
        "java.net.URLConnection",

        // Процессы и Runtime
        "java.lang.Runtime",
        "java.lang.ProcessBuilder",
        "java.lang.Process",

        // Рефлексия (обход песочницы)
        "java.lang.reflect.",
        "sun.reflect.",
        "sun.misc.",
        "jdk.internal.",

        // ClassLoader (создание своего загрузчика)
        "java.lang.ClassLoader",
        "java.net.URLClassLoader",

        // Небезопасные операции
        "sun.misc.Unsafe",
        "jdk.internal.misc.Unsafe",
    )

    override fun loadClass(name: String): Class<*>? {
        blockedClasses.forEach {
            if (name.contains(it)) {
                throw SecurityException("Class $it is not secure")
            }
        }
        findLoadedClass(name)?.let { return it }
        if (name.startsWith("java.") || name.startsWith("javax.") ||
            name.startsWith("kotlin.") || name.startsWith("kotlinx.")) {
            return parent.loadClass(name)
        }

        return try {
            findClass(name)
        } catch (e: ClassNotFoundException) {
            parent.loadClass(name)
        }
    }
}