import java.net.URL
import java.net.URLClassLoader

//deprecated
@Deprecated("да не работает и что ну и что")
class SecurityClassLoader(url: Array<URL>, parent: ClassLoader) : URLClassLoader(url, parent) {
    private val blockedClasses = listOf(
        "java.io.File",
        "sun.misc",
        "sun.reflect"
    )


    override fun loadClass(name: String?): Class<*>? {
        if (isBlockedClass(name!!)) {
            return null
        }else{
            return super.loadClass(name)
        }
    }

    private fun isBlockedClass(name: String): Boolean {
        blockedClasses.forEach {
            if (name.startsWith(it)) {
                return true
            }
        }
        return false
    }
}