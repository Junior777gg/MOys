import common.App
import common.Apps
import common.Log
import common.Manifest
import impl.DeviceManagerImpl
import impl.GraphicServiceImpl
import impl.StorageServiceImpl
import impl.TimerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import navigation.SystemLauncher
import org.jsoup.SerializationException
import security.MavenRepository
import security.SecurityClassLoader
import service.GraphicService
import service.DeviceManager
import service.StorageService
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.time.LocalDate
import java.util.zip.ZipInputStream

/**
 * The core of the operating system.
 * It manages the installation of applications, the registry, and the launch of processes.
 */
class Mother(
    val graphicService: GraphicServiceImpl,
    val deviceManager: DeviceManagerImpl,
    val storageService: StorageServiceImpl,
) {
    companion object {
        /*These paths are accessible to mortal API (SDK)*/
        val systemPath: String
            get()=System.getProperty("user.home") + "/MOys"
        val tempPath: String
            get()="$systemPath/temp"
        val libsPath: String
            get()="$systemPath/libs"

        /*These paths are private and kept behind API usage*/
        private val installPath: String
            get()="$systemPath/install"
        private val registerPath: String
            get()="$systemPath/register"
    }

    init {
        val systemFolder = File(systemPath)
        if (!systemFolder.exists()) {
            systemFolder.mkdirs()
        }

        val registerFolder = File(registerPath)
        if (!registerFolder.exists()) {
            registerFolder.mkdirs()
        }

        val libsFolder = File(libsPath)
        if (!libsFolder.exists()) {
            libsFolder.mkdirs()
        }

        val appsRegisterFile = File(registerPath, "system.json")
        if (!appsRegisterFile.exists()) {
            appsRegisterFile.createNewFile()
            appsRegisterFile.writeText(Json.encodeToString(Apps(mutableListOf())))
        } else registryCleanup()
    }

    val systemLauncher = SystemLauncher(graphicService, deviceManager, this)
    fun start() {
        InstallationService().run(systemPath)
        systemLauncher.runLaunch()
        TimerImpl.start()
    }
    fun shutdown() {
        TimerImpl.stop()
        graphicService.shutdown(systemPath)
    }

    /** Copies app to temp directory */
    fun unpackApp(jarp: File, needToUnpack: Boolean = true) : Manifest? {
        val tempDir = File(tempPath+"/install_"+getFilename(jarp.name))
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        try {
            if (needToUnpack) {
                ZipInputStream(FileInputStream(jarp)).use {
                    var entry = it.nextEntry
                    while (entry != null) {
                        val file = File(tempDir, entry.name)
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
            }
            val manifest = File(tempDir, "manifest.json")
            val decoded = Json.decodeFromString<Manifest>(manifest.readText())
            if(!File(decoded.jar_file_name).exists()||!File(decoded.icon_file_name).exists()) {
                Log.error("Couldn't install app \"${jarp.name}\": common.Manifest contains non-existent file references")
                if (tempDir.exists()) {
                    tempDir.deleteRecursively()
                    tempDir.delete()
                }
                return null
            }
            return decoded
        } catch (e: SerializationException) {
            Log.error("Couldn't install app \"${jarp.name}\": ${e.message.toString()}")
        } catch (e: IllegalArgumentException) {
            Log.error("Couldn't install app \"${jarp.name}\": ${e.message.toString()}")
        }
        //If failed to install - delete folder.
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
            tempDir.delete()
        }
        return null
    }

    /** Returns filename without extension */
    private fun getFilename(filename: String): String {
        val dotIndex = filename.lastIndexOf('.')
        return if (dotIndex == -1) filename else filename.substring(0, dotIndex)
    }

    /** Installs the application from the .jarp archive */
    fun installApp(jarp: File) {
        val installationDir = File(installPath)
        if (!installationDir.exists()) {
            installationDir.mkdirs()
        }

        var needToUnpack = true
        val tempDir = File(tempPath+"/install_"+getFilename(jarp.name))
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        } else needToUnpack = false
        try {
            val manifestObj = unpackApp(jarp, needToUnpack)
            if (manifestObj == null) return
            val outputFile = File(installPath, manifestObj.app_id)
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
            tempDir.delete()
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
            val registerFile = File(registerPath, "system.json")
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
            val outputFile = File("$installPath/$appId")
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
            val jarFile = File("$installPath/$appId/$jarName")
            val load = buildList<URL>{
                add(jarFile.toURI().toURL())
                File(libsPath).listFiles().forEach { libFile ->
                    add(libFile.toURI().toURL())
                }
            }.toTypedArray()
            val classLoader = SecurityClassLoader(load, this.javaClass.classLoader)
                classLoader.use { classLoader ->
                val clazz = classLoader.loadClass(activityName)!!
                val constructor = clazz.getDeclaredConstructor(
                    GraphicService::class.java,
                    StorageService::class.java,
                    DeviceManager::class.java
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
        val registerFile = File(registerPath, "system.json")
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
        val register = File("$registerPath/system.json").readText()
        return Json.decodeFromString<Apps>(register).apps
    }

    /** Removes duplicates from registry */
    private fun registryCleanup() {
        val registerFile = File(registerPath, "system.json")
        val register = Json.decodeFromString<Apps>(registerFile.readText())
        val newRegister = Apps(register.apps.toList().distinct().toMutableList())
        registerFile.writeText(Json.encodeToString(newRegister))
    }
}
