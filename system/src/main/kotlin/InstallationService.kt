import common.Log
import java.io.File
import java.nio.file.Paths

class InstallationService {
    fun run(systemPath: String) {
        copyAppDataFiles("$systemPath/install/")
        Log.dbg("Initialization service successfully executed")
    }

    private fun copyAppDataFiles(installPath: String) {
        copy("backgrounds/1.png", "$installPath/launcher/res/backgrounds/mobile/1.png")
        copy("backgrounds/1d.png", "$installPath/launcher/res/backgrounds/desktop/1.png")
        copy("backgrounds/2.png", "$installPath/launcher/res/backgrounds/mobile/2.png")
        copy("backgrounds/2d.png", "$installPath/launcher/res/backgrounds/desktop/2.png")
        copy("backgrounds/3.png", "$installPath/launcher/res/backgrounds/mobile/3.png")
        copy("backgrounds/3d.png", "$installPath/launcher/res/backgrounds/desktop/3.png")
        copy("backgrounds/4.png", "$installPath/launcher/res/backgrounds/mobile/4.png")
        copy("backgrounds/4d.png", "$installPath/launcher/res/backgrounds/desktop/4.png")
        copy("backgrounds/5.png", "$installPath/launcher/res/backgrounds/mobile/5.png")
        copy("backgrounds/5d.png", "$installPath/launcher/res/backgrounds/desktop/5.png")
        copy("basic.png", "$installPath/launcher/res/basic.png")
        copy("navigation/back.png", "$installPath/launcher/res/navigation/back.png")
        copy("navigation/tabs.png", "$installPath/launcher/res/navigation/tabs.png")
        copy("navigation/home.png", "$installPath/launcher/res/navigation/home.png")
        copy("app/browser.png", "$installPath/browser/icon.png")
        copy("app/calculator.png", "$installPath/calculator/icon.png")
        copy("app/settings.png", "$installPath/settings/icon.png")
        copy("app/storage.png", "$installPath/storage/icon.png")
        copy("app/terminal.png", "$installPath/terminal/icon.png")
        copy("terminal_run.png", "$installPath/terminal/res/run.png")
        Log.dbg("Launcher resources copied")

        copy("file.png", "$installPath/storage/res/file.png")
        copy("archive_file.png", "$installPath/storage/res/archive_file.png")
        copy("audio_file.png", "$installPath/storage/res/audio_file.png")
        copy("image_file.png", "$installPath/storage/res/image_file.png")
        copy("text_file.png", "$installPath/storage/res/text_file.png")
        copy("unknown_file.png", "$installPath/storage/res/unknown_file.png")
        copy("folder.png", "$installPath/storage/res/folder.png")
        copy("jarp.png", "$installPath/storage/res/jarp.png")
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