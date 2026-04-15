package common

import kotlinx.serialization.Serializable

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

