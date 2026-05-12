package common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class App(
    val app_id: String,
    val app_name: String,
    val version: String,
    val icon_file_name: String,
    val jar_file_name: String,
    val activity_name: String,
    val install_date: String,
    val update_date: String,
    val libs: List<String>,
)
@Serializable
data class Apps(
    val apps: MutableList<App>,
)
@Serializable
data class Manifest(
    val app_id: String,
    val app_name: String,
    val version: String,
    val icon_file_name: String,
    val jar_file_name: String,
    val activity_name: String,
    val libs: List<String>,
)
@Serializable
data class SystemConfig (
    var timerProcessTimeThresholdMs: Int = 1000,
    var ignoreOnBackCancel: Boolean = false,
) {
    companion object {
        var Instance=SystemConfig()
        fun load(path: String) {
            val file=File(path)
            if(file.exists()) Instance=Json.decodeFromString<SystemConfig>(file.readText())
            else file.createNewFile()
        }
        fun save(path: String) {
            val cfg = File(path)
            cfg.writeText(Json.encodeToString<SystemConfig>(Instance))
        }
    }
}
