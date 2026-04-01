import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URLClassLoader
import java.time.LocalDate
import java.util.zip.ZipInputStream

class Mother(
    val graphicService: GraphicService,
    val deviceManager: DeviceManager,
    val storageService: StorageService,
) {
    init {
        val registerDir = File("MOys/register")
        val registerFile = File(registerDir.path, "register.json")
        if (!registerDir.exists()) {
            registerDir.mkdirs()
        }
        if (!registerFile.exists()) {
            registerFile.createNewFile()
            registerFile.writeText(Json.encodeToString(Apps(mutableListOf())))
        }
    }
    val systemLauncher = SystemLauncher(graphicService, deviceManager, this)
    fun start() {
        systemLauncher.runLaunch()
    }

    fun installApp(jarp: File) {
        val installationDir = File("MOys/Installation")
        if (!installationDir.exists()) {
            installationDir.mkdirs()
        }
        val tempDir = File("MOys/temp")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }

        ZipInputStream(FileInputStream(jarp)).use {
            var entry = it.nextEntry
            while (entry != null) {
                val file = File(tempDir.path, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { output ->
                        it.copyTo(output)
                    }
                }
                entry = it.nextEntry
            }
        }
        val manifest = File(tempDir.path, "manifest.json")
        val manifestObj = Json.decodeFromString<Manifest>(manifest.readText())
        val outputFile = File(installationDir, manifestObj.app_id)
        if (outputFile.exists()) {
            updateApp()
        } else {
            outputFile.mkdirs()
            tempDir.listFiles()!!.forEach { file ->
                file.copyRecursively(File(outputFile.path, file.name))
            }
            val date = LocalDate.now()
            registerNewApp(
                App(
                    app_id = manifestObj.app_id,
                    app_name = manifestObj.app_name,
                    version = manifestObj.version,
                    jar_file_name = manifestObj.jar_file_name,
                    icon_file_name = manifestObj.icon_file_name,
                    activity_name = manifestObj.activity_name,
                    install_date = date.toString(),
                    update_date = date.toString()
                )
            )
            systemLauncher.showNewAppLabel(
                appName = manifestObj.app_name,
                appIcon = File(outputFile,manifestObj.icon_file_name),
                appId = manifestObj.app_id,
                activityName = manifestObj.activity_name,
                jarName = manifestObj.jar_file_name)
            tempDir.deleteRecursively()
        }
    }

    fun updateApp() {}

    fun runNewAppProcess(appId: String, jarName: String, activityName: String) {
        val jarFile = File("MOys/Installation/$appId/$jarName")
        val load = arrayOf(jarFile.toURI().toURL())
        URLClassLoader(load, ClassLoader.getSystemClassLoader()).use { classLoader ->
            val clazz = classLoader.loadClass(activityName)
            val constructor = clazz.getDeclaredConstructor(
                GraphicServiceI::class.java,
                StorageServiceI::class.java,
                DeviceManagerI::class.java
            )
            val instance = constructor.newInstance(graphicService, storageService, deviceManager)
            val method = clazz.getMethod("main")
            method.invoke(instance)
        }
    }

    private fun killProcess() {

    }

    private fun registerNewApp(app: App) {
        val registerDir = File("MOys/register")
        val registerFile = File(registerDir.path, "register.json")
        if (!registerDir.exists()) {
            registerDir.mkdirs()
        }
        if (!registerFile.exists()) {
            registerFile.createNewFile()
            registerFile.writeText(Json.encodeToString(Apps(mutableListOf())))
        }

        val oldRegister = Json.decodeFromString<Apps>(registerFile.readText())
        oldRegister.apps.add(app)
        registerFile.writeText(Json.encodeToString(oldRegister))
    }

    fun getRegisteredApps(): List<App> {
        val register = File("MOys/register/register.json").readText()
        return Json.decodeFromString<Apps>(register).apps
    }
}