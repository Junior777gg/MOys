package impl

import common.Log
import service.StorageService
import java.io.File

class StorageServiceImpl : StorageService {
    private val MOUNTS_PATH = "/proc/mounts"

    private val virtualFileSystems =
        listOf("tmpfs", "proc", "sysfs", "devtmpfs", "devpts", "cgroup", "cgroup2", "binfmt_misc")

    val rootDirs: MutableList<String> = mutableListOf()
    private val startDir = "/MOys/data"

    fun initialize() {
        File(MOUNTS_PATH).readLines().forEach {
            val fileSystem = it.split(" ")
            if (fileSystem[2] !in virtualFileSystems) {
                rootDirs.add(fileSystem[1])
            }
        }

        if (!File(startDir).exists()) {
            File(startDir).mkdirs()
        }

        if (rootDirs.isEmpty()) {
            throw Exception("No root directory")
        } else {
            if (!File("/", "MOys").exists()) {
                File("/", "MOys").mkdirs()
                File(startDir).mkdirs()
            }
        }

        Log.info("Storage service initialized")
    }

    private fun buildPath(appId: String, path: String): String {
        val fullPath = "$startDir/$appId/$path".replace("//","/")
        if (".." in path || "./" in path) {
            throw Exception("$fullPath is not a valid path")
        } else {
            return fullPath
        }
    }

    override fun createFileWithText(appId: String, path: String): Boolean {
        try {
            val filePath = buildPath( appId, path)
            val appDirPath = buildPath(appId, "")
            if (!File(appDirPath).exists()) {
                File(appDirPath).mkdirs()
            }
            val newFile = File(filePath)
            newFile.createNewFile()
            return true
        } catch (e: Exception) {
            Log.error(e.message.toString())
            return false
        }
    }

    override fun writeText(appId: String, path: String, content: String): Boolean {
        try {
            val filePath = buildPath(appId, path)
            val newFile = File(filePath)
            newFile.writeText(content)
            return true
        } catch (e: Exception) {
            Log.error(e.message.toString())
            return false
        }
    }

    override fun writeBytes(appId: String, path: String, content: ByteArray): Boolean {
        try {
            val filePath = buildPath( appId, path)
            val newFile = File(filePath)
            newFile.writeBytes(content)
            return true
        } catch (e: Exception) {
            Log.error(e.message.toString())
            return false
        }
    }

    override fun deleteFile(appId: String, path: String): Boolean {
        try {
            val filePath = buildPath(appId, path)
            File(filePath).delete()
            return true
        } catch (e: Exception) {
            Log.error(e.message.toString())
            return false
        }
    }

    override fun createDirectory(appId: String, path: String): Boolean {
        try {
            val dirPath = buildPath(appId, path)
            val appDirPath = buildPath(appId, "")
            if (!File(appDirPath).exists()) {
                File(appDirPath).mkdirs()
            }
            val newDir = File(dirPath)
            newDir.mkdirs()
            return true
        } catch (e: Exception) {
            Log.error(e.message.toString())
            return false
        }
    }

    override fun deleteDirectory(appId: String, path: String): Boolean {
        try {
            val dirPath = buildPath(appId, path)
            File(dirPath).deleteRecursively()
            return true
        } catch (e: Exception) {
            Log.error(e.message.toString())
            return false
        }
    }

    override fun readFileText(appId: String, path: String): String? {
        try {
            val filePath = buildPath(appId, path)
            val file = File(filePath)
            return file.readText()
        } catch (e: Exception) {
            Log.error(e.message.toString())
            return null
        }
    }

    override fun readFileBytes(appId: String, path: String): ByteArray? {
        try {
            val filePath = buildPath( appId, path)
            val file = File(filePath)
            return file.readBytes()
        } catch (e: Exception) {
            Log.error(e.message.toString())
            return null
        }
    }

    override fun exists(appId: String, path: String): Boolean {
        val filePath = buildPath(appId, path)
        val file = File(filePath)
        return file.exists()
    }
}