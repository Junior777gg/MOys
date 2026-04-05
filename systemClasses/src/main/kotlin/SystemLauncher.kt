import app.BrowserApp
import app.CalculatorApp
import app.Settings
import app.Storage
import app.TestApp
import javafx.application.Application
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Color
import java.io.File

class SystemLauncher(
    val graphicService: GraphicService,
    val deviceManager: DeviceManager,
    val mother: Mother
) {
    @Serializable
    data class LauncherConfig (
        var background: String,
    )

    val labels = mutableListOf<MutableList<View>.() -> Unit>()
    var config = LauncherConfig("backgrounds/1.png")
    private fun updateLabels() {
        labels.clear()
        mother.getRegisteredApps().forEach { app ->
            val appIcon = File(mother.getSystemPath()+"/install/${app.app_id}/${app.icon_file_name}")
            labels.add({
                label(
                    appIcon, app.app_name,
                    {
                        mother.runNewAppProcess(app.app_id, app.jar_file_name, app.activity_name)
                    },
                    {
                        mother.deleteApp(app.app_id)
                        updateScreen()
                    })
            })
        }
        labels.add({
            label(
                icon = File("${mother.getSystemPath()}/install/calculator/icon.png"),
                click = { CalculatorApp(graphicService, StorageService(), deviceManager).main() },
                appName = "Калькулятор"
            )
        })
        labels.add({
            label(
                icon = File("${mother.getSystemPath()}/install/settings/icon.png"),
                click = { Settings(mother, graphicService, StorageService(), deviceManager).main()},
                appName = "Настройки"
            )
        })
        labels.add({
            label(
                icon = File("${mother.getSystemPath()}/install/storage/icon.png"),
                click = { Storage(mother, graphicService, StorageService(), deviceManager).main()},
                appName = "Проводник"
            )
        })
        labels.add({
            label(
                icon = File("${mother.getSystemPath()}/install/browser/icon.png"),
                click = {
                    Thread {
                        Application.launch(BrowserApp::class.java)
                    }.start()
                },
                appName = "Браузер"
            )
        })
        labels.add({
            label(
                click = { TestApp(graphicService, StorageService(), deviceManager).main()},
                appName = "Scroll Test"
            )
        })
    }
    fun runLaunch() {
        loadConfig()
        updateLabels()
        graphicService.setContent(true) {
            screen()
        }
        graphicService.redraw()
    }

    fun MutableList<View>.screen() {
        Image(modifier = Modifier.fillMaxSize(), File(mother.getSystemPath()+"/data/launcher/${config.background}"), this).layout {
            Column(modifier = Modifier.fillMaxSize().background(Color(0,0,0,0)), this).layout {
                var count = 0
                for (i in 0..6) {
                    Row(modifier = Modifier.height(130).width(640).background(Color(0,0,0,0)), this).layout {
                        while (this.size < 5) {
                            if (count > labels.size - 1) break
                            labels[count](this)
                            count++
                        }
                    }
                }
            }
        }
    }

    fun updateScreen() {
        loadConfig()
        updateLabels()
        graphicService.setContent(false) {
            screen()
        }
        graphicService.redraw()
    }

    fun MutableList<View>.label(icon: File? = null, appName: String, click: () -> Unit, hold: (() -> Unit) = {Log.warn("Can't remove system app")}) {
        Column(
            modifier = Modifier.padding(20).height(130).width(110)
                .onClick { click() }.onHold { hold.invoke() } .background(Color(0,0,0,0)), this
        ).layout {
            Image(modifier = Modifier.size(70), icon ?: File(mother.getSystemPath()+"/data/launcher/basic.png"), this)
            Text(modifier = Modifier.width(70).height(20), appName, 15, parent = this)
        }
    }

    fun setBackground(path: String) {
        Log.dbg("Set launcher background as: \"$path\"")
        config.background = path
        val cfg = File("${mother.getSystemPath()}/data/launcher/config.json")
        cfg.writeText(Json.encodeToString<LauncherConfig>(config))
        graphicService.setContent(false) {
            screen()
        }
    }
    fun loadConfig() {
        val cfg = File("${mother.getSystemPath()}/data/launcher/config.json")
        if(cfg.exists()) {
            config = Json.decodeFromString<LauncherConfig>(cfg.readText())
            //Check if background exists.
            if(!File("${mother.getSystemPath()}/data/launcher/${config.background}").exists())
                config.background="backgrounds/1.png"
        }
    }
}