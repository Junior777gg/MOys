import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URLClassLoader
import java.time.LocalDate
import java.util.zip.ZipInputStream

/**
 * The core of the operating system.
 * It manages the installation of applications, the registry, and the launch of processes.
 */
class Mother(
    val graphicService: GraphicService,
    val deviceManager: DeviceManager,
    val storageService: StorageService,
) {
    /*These paths are accessible to mortal API (SDK)*/
    private val systemFolderPath = System.getProperty("user.home") + "/MOys"
    private val tempFolderPath = "$systemFolderPath/temp"
    /*These paths are private and kept behind API usage*/
    private val installFolderPath = "$systemFolderPath/install"
    private val registerFolderPath = "$systemFolderPath/register"

    init {
        val systemFolder = File(systemFolderPath)
        if (!systemFolder.exists()) {
            systemFolder.mkdirs()
        }

        val registerDir = File(registerFolderPath)
        if (!registerDir.exists()) {
            registerDir.mkdirs()
        }

        val appsRegisterFile = File(registerFolderPath, "system.json")
        if (!appsRegisterFile.exists()) {
            appsRegisterFile.createNewFile()
            appsRegisterFile.writeText(Json.encodeToString(Apps(mutableListOf())))
        } else registryCleanup()
    }

    val systemLauncher = SystemLauncher(graphicService, deviceManager, this)
    fun start() {
        systemLauncher.runLaunch()
    }

    fun getSystemPath() : String {
        return systemFolderPath;
    }
    fun getTempPath() : String {
        return tempFolderPath;
    }

    /** Installs the application from the .jarp archive */
    fun installApp(jarp: File) {
        val installationDir = File(installFolderPath)
        if (!installationDir.exists()) {
            installationDir.mkdirs()
        }
        val tempDir = File(tempFolderPath)
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }

        ZipInputStream(FileInputStream(jarp)).use {
            var entry = it.nextEntry
            while (entry != null) {
                val file = File(tempFolderPath, entry.name)
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
        val manifest = File(tempFolderPath, "manifest.json")
        val manifestObj = Json.decodeFromString<Manifest>(manifest.readText())
        val outputFile = File(installFolderPath, manifestObj.app_id)
        if (outputFile.exists()) {
            Log.warn("Duplicate app entry \"${manifestObj.app_id}\". Updating...")
            outputFile.deleteRecursively()
        }
        outputFile.mkdirs()
        val listTempFiles = tempDir.listFiles() ?: emptyArray<File>()
        listTempFiles.forEach { file ->
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
            appIcon = File(outputFile, manifestObj.icon_file_name),
            appId = manifestObj.app_id,
            activityName = manifestObj.activity_name,
            jarName = manifestObj.jar_file_name
        )
        tempDir.deleteRecursively()
    }

    /** Starts the application in a separate ClassLoader using the appId */
    fun runNewAppProcess(appId: String, jarName: String, activityName: String) {
        try {
            val jarFile = File("$installFolderPath/$appId/$jarName")
            val load = arrayOf(jarFile.toURI().toURL())
            SecurityClassLoader(load, ClassLoader.getSystemClassLoader()).use { classLoader ->
                val clazz = classLoader.loadClass(activityName)!!
                val constructor = clazz.getDeclaredConstructor(
                    GraphicServiceI::class.java,
                    StorageServiceI::class.java,
                    DeviceManagerI::class.java
                )
                val instance = constructor.newInstance(graphicService, storageService, deviceManager)
                val method = clazz.getMethod("main")
                method.invoke(instance)
            }
        } catch (e: Exception) {
            Log.error(e.message.toString())
        }
    }

    /** Saves application information in the registry */
    private fun registerNewApp(app: App) {
        val registerFile = File(registerFolderPath, "system.json")
        val oldRegister = Json.decodeFromString<Apps>(registerFile.readText())
        //No duplicates (replace with error if possible, because the method is 'registerNewApp' and the id already exists)
        var entryFound=false
        for(e in oldRegister.apps) if(app.app_id==e.app_id) {entryFound=true;break}
        if(!entryFound) oldRegister.apps.add(app)
        registerFile.writeText(Json.encodeToString(oldRegister))
    }

    /** Returns a list of all installed applications from the registry */
    fun getRegisteredApps(): List<App> {
        val register = File("$registerFolderPath/system.json").readText()
        return Json.decodeFromString<Apps>(register).apps
    }

    /** Removes duplicates from registry */
    private fun registryCleanup() {
        val registerFile = File(registerFolderPath, "system.json")
        var register = Json.decodeFromString<Apps>(registerFile.readText())
        var newRegister = Apps(register.apps.toList().distinct().toMutableList())
        registerFile.writeText(Json.encodeToString(newRegister))
    }
}
