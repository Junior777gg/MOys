import common.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jsoup.SerializationException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import java.net.URL
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
    companion object {
        /*These paths are accessible to mortal API (SDK)*/
        private val systemFolderPath = System.getProperty("user.home") + "/MOys"
        private val tempFolderPath = "$systemFolderPath/temp"

        /*These paths are private and kept behind API usage*/
        private val installFolderPath = "$systemFolderPath/install"
        private val registerFolderPath = "$systemFolderPath/register"

        fun getSystemPath(): String {
            return systemFolderPath;
        }

        fun getTempPath(): String {
            return tempFolderPath;
        }
    }
    private val libsFolderPath = "$systemFolderPath/libs"

    init {
        val systemFolder = File(systemFolderPath)
        if (!systemFolder.exists()) {
            systemFolder.mkdirs()
        }

        val registerDir = File(registerFolderPath)
        if (!registerDir.exists()) {
            registerDir.mkdirs()
        }

        val libsFolder = File(libsFolderPath)
        if (!libsFolder.exists()) {
            libsFolder.mkdirs()
        }

        val appsRegisterFile = File(registerFolderPath, "system.json")
        if (!appsRegisterFile.exists()) {
            appsRegisterFile.createNewFile()
            appsRegisterFile.writeText(Json.encodeToString(Apps(mutableListOf())))
        } else registryCleanup()
    }

    val systemLauncher = SystemLauncher(graphicService, deviceManager, this)
    fun start() {
        InstallationService().run(systemFolderPath)
        systemLauncher.runLaunch()
        Timer.start()
    }
    fun shutdown() {
        Timer.stop()
        graphicService.shutdown(systemFolderPath)
    }

    fun getLibsPath(): String {
        return libsFolderPath
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
        try {
            val manifestObj = Json.decodeFromString<Manifest>(manifest.readText())
            val outputFile = File(installFolderPath, manifestObj.app_id)
            if (outputFile.exists()) {
                Log.warn("Duplicate app entry \"${manifestObj.app_id}\". Updating...")
                outputFile.deleteRecursively()
            }
            outputFile.mkdirs()
            if (outputFile.exists()) {
                outputFile.deleteRecursively()
            }
            val listTempFiles = tempDir.listFiles() ?: emptyArray<File>()
            listTempFiles.forEach { file ->
                file.copyRecursively(File(outputFile.path, file.name))
            }
            CoroutineScope(Dispatchers.IO).launch {
                manifestObj.libs.forEach { lib ->
                    MavenRepository.getLibs(lib, this@Mother)
                }
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
                    update_date = date.toString(),
                    libs = manifestObj.libs,

                    )
            )
            systemLauncher.updateScreen(false)
            tempDir.deleteRecursively()
        } catch (e: SerializationException) {
            Log.error("Couldn't install app \"${jarp.name}\": ${e.message.toString()}")
        } catch (e: IllegalArgumentException) {
            Log.error("Couldn't install app \"${jarp.name}\": ${e.message.toString()}")
        }
    }

    /** Removes app's data and register entry */
    fun deleteApp(appId: String) {
        try {
            //Delete app from register.
            val registerFile = File(registerFolderPath, "system.json")
            val register = Json.decodeFromString<Apps>(registerFile.readText())
            var removedRegistryKey = false
            for (e in register.apps) {
                if (appId == e.app_id) {
                    register.apps.remove(e)
                    registerFile.writeText(Json.encodeToString(register))
                    removedRegistryKey = true
                    break
                }
            }
            if (!removedRegistryKey) Log.warn("Register entry for \"$appId\" not found")
            //Delete app from storage.
            val outputFile = File("$installFolderPath/$appId")
            if (outputFile.exists()) outputFile.deleteRecursively()
            else Log.warn("Install directory for \"$appId\" not found")

            Log.info("Deleted app with id \"$appId\"")
        } catch (e: Exception) {
            Log.error("Couldn't delete app \"$appId\": ${e.message.toString()}")
        }
    }

    /** Starts the application in a separate ClassLoader using the appId */
    fun runNewAppProcess(appId: String, jarName: String, activityName: String) {
        try {
            val jarFile = File("$installFolderPath/$appId/$jarName")
            val load = buildList<URL>{
                add(jarFile.toURI().toURL())
                File(getLibsPath()).listFiles().forEach { libFile ->
                    add(libFile.toURI().toURL())
                }
            }.toTypedArray()
            println(load.toString())
            SecurityClassLoader(load, this.javaClass.classLoader).use { classLoader ->
                val clazz = classLoader.loadClass(activityName)!!
                val constructor = clazz.getDeclaredConstructor(
                    GraphicServiceI::class.java,
                    StorageServiceI::class.java,
                    DeviceManagerI::class.java
                )
                val instance = constructor.newInstance(graphicService, storageService, deviceManager)
                graphicService.setActivity(instance as Activity)
                val method = clazz.getMethod("main")
                method.invoke(instance)
            }
        } catch (e: Exception) {
            Log.error("Couldn't launch app \"$appId\": ${e.message.toString()}")
            e.printStackTrace()
        }
    }

    /** Saves application information in the registry */
    private fun registerNewApp(app: App) {
        val registerFile = File(registerFolderPath, "system.json")
        val register = Json.decodeFromString<Apps>(registerFile.readText())
        //No duplicates (replace with error if possible, because the method is 'registerNewApp' and the id already exists)
        var entryFound = false
        for (e in register.apps) {
            if (app.app_id == e.app_id) {
                entryFound = true
                break
            }
        }
        if (!entryFound) {
            register.apps.add(app)
            registerFile.writeText(Json.encodeToString(register))
        }
    }

    /** Returns a list of all installed applications from the registry */
    fun getRegisteredApps(): List<App> {
        val register = File("$registerFolderPath/system.json").readText()
        return Json.decodeFromString<Apps>(register).apps
    }

    /** Removes duplicates from registry */
    private fun registryCleanup() {
        val registerFile = File(registerFolderPath, "system.json")
        val register = Json.decodeFromString<Apps>(registerFile.readText())
        val newRegister = Apps(register.apps.toList().distinct().toMutableList())
        registerFile.writeText(Json.encodeToString(newRegister))
    }
}
