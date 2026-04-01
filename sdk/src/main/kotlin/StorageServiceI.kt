interface StorageServiceI {
    fun createFileWithText(appId: String, path: String): Boolean
    fun writeText(appId: String, path: String, content: String): Boolean
    fun writeBytes(appId: String, path: String, content: ByteArray): Boolean
    fun deleteFile(appId: String, path: String): Boolean
    fun createDirectory(appId: String, path: String): Boolean
    fun deleteDirectory(appId: String, path: String): Boolean
    fun readFileText(appId: String, path: String): String?
    fun readFileBytes(appId: String, path: String): ByteArray?
    fun exists(appId: String, path: String): Boolean
}