import java.io.File
import java.nio.file.Paths

class InstallationService {
    fun run(systemPath: String) {
        copyAppDataFiles("$systemPath/data/", "$systemPath/install/")
        Log.dbg("Initialization service successfully executed")
    }

    private fun copyAppDataFiles(dataPath: String, installPath: String) {
        copy("backgrounds/1.png", "$dataPath/launcher/backgrounds/1.png")
        copy("backgrounds/2.png", "$dataPath/launcher/backgrounds/2.png")
        copy("backgrounds/3.png", "$dataPath/launcher/backgrounds/3.png")
        copy("basic.png", "$dataPath/launcher/basic.png")
        copy("app/browser.png", "$installPath/browser/icon.png")
        copy("app/calculator.png", "$installPath/calculator/icon.png")
        copy("app/settings.png", "$installPath/settings/icon.png")
        copy("app/storage.png", "$installPath/storage/icon.png")
        write("$dataPath/launcher/config.json", "{\"background\":\"backgrounds/1.png\",\"textDark\": false}")
        Log.dbg("Launcher resources copied")

        copy("file.png", "$dataPath/storage/file.png")
        copy("folder.png", "$dataPath/storage/folder.png")
        copy("jarp.png", "$dataPath/storage/jarp.png")
        write("$dataPath/storage/config.json", "{}")
        Log.dbg("Storage resources copied")

        Log.dbg("System app data copied")
    }

    private fun write(path: String, text: String) {
        val f=File(path)
        if(!f.exists()) f.writeText(text)
    }
    private fun copy(origin: String, destination: String) {
        val dest=File(destination)
        if(!dest.exists()) File(resource(origin)).copyTo(dest)
    }
    private fun resource(path: String):String {
        return Paths.get("").toAbsolutePath().parent.parent.parent.toString()+"/res/"+path
    }
}