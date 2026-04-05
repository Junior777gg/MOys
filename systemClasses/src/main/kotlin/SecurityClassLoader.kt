import java.net.URL
import java.net.URLClassLoader

class SecurityClassLoader(url: Array<URL>, parent: ClassLoader) : URLClassLoader(url, parent) {
    private val blockedClasses = listOf(
        "java",
        "jdk",
        "sun.misc",
        "sun.reflect"
    )

    override fun loadClass(name: String?): Class<*>? {
        if (!isBlockedClass(name!!)) {
            return super.loadClass(name)
        } else throw ClassNotFoundException("Class $name not found")
    }

    private fun isBlockedClass(name: String): Boolean {
        blockedClasses.forEach {
            if (blockedClasses.contains(it)) {
                return true
            }
        }
        return false
    }
}