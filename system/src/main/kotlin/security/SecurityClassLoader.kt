package security

import java.net.URL
import java.net.URLClassLoader

class SecurityClassLoader(url: Array<URL>, parent: ClassLoader) : URLClassLoader(url, parent) {
    private val blockedClasses = listOf(
        //Files.
        "java.io.File",
        "java.io.FileInputStream",
        "java.io.FileOutputStream",
        "java.io.RandomAccessFile",
        "java.nio.file.",
        "java.nio.channels.FileChannel",

        //Network.
        "java.net.Socket",
        "java.net.ServerSocket",
        "java.net.URL",
        "java.net.HttpURLConnection",
        "java.net.URLConnection",

        //Processes and Runtime.
        "java.lang.Runtime",
        "java.lang.ProcessBuilder",
        "java.lang.Process",

        //Reflection (sandbox bypass).
        "java.lang.reflect.",
        "sun.reflect.",
        "sun.misc.",
        "jdk.internal.",

        //ClassLoader (creation of custom class loader).
        "java.lang.ClassLoader",
        "java.net.URLClassLoader",

        //Unsafe operations.
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
        } catch (_: ClassNotFoundException) {
            parent.loadClass(name)
        }
    }
}