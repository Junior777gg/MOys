import common.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.io.File

object MavenRepository {
    private const val MAVENLINK = "https://repo1.maven.org/maven2/"
    val client = HttpClient()

    fun linkBuilder(packageName: String): String {
        val packet = packageName.split(":")[0]
        val libName = packageName.split(":")[1]
        val version = packageName.split(":")[2]
        return "$MAVENLINK${packet.replace(".", "/")}/$libName/$version/$libName-$version.jar"
    }

    suspend fun getLibs(packageName: String, mother: Mother){
        val libFile = File("${mother.getLibsPath()}/$packageName.jar")
        if (libFile.exists()){
            return
        }
        libFile.createNewFile()
        try {
            val inputStream = client.get(linkBuilder(packageName)).body<ByteArray>()
            libFile.writeBytes(inputStream)
        } catch (e: Exception) {
            Log.warn("incorrect package name: $packageName, ${e.message}")
        }
    }
}