package navigation

import Column
import Image
import Mother
import Row
import Text
import View
import app.BrowserApp
import app.CalculatorApp
import app.SettingsApp
import app.StorageApp
import app.TerminalApp
import app.TestApp
import common.Log
import javafx.application.Application
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import common.Color
import impl.DeviceManagerImpl
import impl.GraphicServiceImpl
import impl.StorageServiceImpl
import modifier.HorizontalAlignment
import modifier.HorizontalArrangement
import modifier.Modifier
import modifier.VerticalArrangement
import modifier.background
import modifier.fillMaxSize
import modifier.height
import modifier.onClick
import modifier.onHold
import modifier.padding
import modifier.size
import modifier.width
import java.io.File

class SystemLauncher(
    val graphicService: GraphicServiceImpl,
    val deviceManager: DeviceManagerImpl,
    val mother: Mother
) {
    @Serializable
    data class LauncherConfig (
        var background: String = "backgrounds/mobile/1.png",
        var textDark: Boolean = false,
        var textDisplay: Boolean = true,
        var appsCentering: Boolean = false,
    )

    private val labels = mutableListOf<MutableList<View>.() -> Unit>()
    private var config = LauncherConfig("backgrounds/mobile/1.png",false)
    private fun updateLabels() {
        labels.clear()
        mother.getRegisteredApps().forEach { app ->
            val appIcon = File(Mother.systemPath+"/install/${app.app_id}/${app.icon_file_name}")
            labels.add({
                label(
                    appIcon, app.app_name,
                    {
                        mother.runNewAppProcess(app.app_id, app.jar_file_name, app.activity_name)
                    },
                    {
                        mother.deleteApp(app.app_id)
                        updateScreen(true)
                    })
            })
        }
        labels.add({
            label(
                icon = File("${Mother.systemPath}/install/calculator/icon.png"),
                click = {
                    val act=CalculatorApp(graphicService, StorageServiceImpl(), deviceManager)
                    graphicService.setActivity(act)
                    act.main()
                },
                appName = "Калькулятор"
            )
        })
        labels.add({
            label(
                icon = File("${Mother.systemPath}/install/settings/icon.png"),
                click = {
                    val act=SettingsApp(mother, graphicService, StorageServiceImpl(), deviceManager)
                    graphicService.setActivity(act)
                    act.main()
                },
                appName = "Настройки"
            )
        })
        labels.add({
            label(
                icon = File("${Mother.systemPath}/install/storage/icon.png"),
                click = {
                    val act=StorageApp(mother, graphicService, StorageServiceImpl(), deviceManager)
                    graphicService.setActivity(act)
                    act.main()
                },
                appName = "Проводник"
            )
        })
        labels.add({
            label(
                icon = File("${Mother.systemPath}/install/terminal/icon.png"),
                click = {
                    val act= TerminalApp(mother, graphicService, StorageServiceImpl(), deviceManager)
                    graphicService.setActivity(act)
                    act.main()
                },
                appName = "Терминал"
            )
        })
        labels.add({
            label(
                icon = File("${Mother.systemPath}/install/browser/icon.png"),
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
                click = {
                    val act=TestApp(graphicService, StorageServiceImpl(), deviceManager)
                    graphicService.setActivity(act)
                    act.main()
                },
                appName = "Testing App"
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

    private fun getAppsArrangement(): HorizontalArrangement {
        if(config.appsCentering) return HorizontalArrangement.Center()
        else return HorizontalArrangement.Left()
    }

    fun MutableList<View>.screen() {
        Image(modifier = Modifier.fillMaxSize(), File(Mother.systemPath+"/data/launcher/${config.background}"), this).layout {
            Column(modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT),
                verticalArrangement = VerticalArrangement.SpaceEvenly(),
                horizontalAlignment = HorizontalAlignment.Center(), parent = this).layout {
                var count = 0
                for (i in 0..6) {
                    Row(modifier = Modifier.height(130).width(640).background(Color.TRANSPARENT), horizontalArrangement = getAppsArrangement(), parent = this).layout {
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

    fun updateScreen(redraw: Boolean) {
        loadConfig()
        updateLabels()
        graphicService.setContent(false) {
            screen()
        }
        if(redraw) graphicService.redraw()
    }

    fun MutableList<View>.label(icon: File? = null, appName: String, click: () -> Unit, hold: (() -> Unit) = { Log.warn("Can't remove system app")}) {
        var textColor = Color.WHITE
        if(config.textDark) textColor = Color.BLACK
        Column(
            modifier = Modifier.padding(20).height(130).width(110)
                .onClick { click() }.onHold { hold.invoke() } .background(Color.TRANSPARENT), this
        ).layout {
            Image(modifier = Modifier.size(70), icon ?: File("${Mother.systemPath}/data/launcher/basic.png"), this)
            if(config.textDisplay) Text(modifier = Modifier.width(70).height(20), text = appName, textColor = textColor, textSize = 15, parent = this)
        }
    }

    fun getBackground(): String {
        return config.background
    }
    fun setBackground(path: String) {
        Log.dbg("Set launcher background as: \"$path\"")
        config.background = path
        val cfg = File("${Mother.systemPath}/data/launcher/config.json")
        cfg.writeText(Json.encodeToString<LauncherConfig>(config))
        updateScreen(false)
    }
    fun getTextDark(): Boolean {
        return config.textDark
    }
    fun setTextDark(v: Boolean) {
        Log.dbg("Set launcher text dark to: \"$v\"")
        config.textDark = v
        val cfg = File("${Mother.systemPath}/data/launcher/config.json")
        cfg.writeText(Json.encodeToString<LauncherConfig>(config))
        updateScreen(false)
    }
    fun getTextDisplay(): Boolean {
        return config.textDisplay
    }
    fun setTextDisplay(v: Boolean) {
        Log.dbg("Set launcher text display to: \"$v\"")
        config.textDisplay = v
        val cfg = File("${Mother.systemPath}/data/launcher/config.json")
        cfg.writeText(Json.encodeToString<LauncherConfig>(config))
        updateScreen(false)
    }
    fun getAppsCentering(): Boolean {
        return config.appsCentering
    }
    fun setAppsCentering(v: Boolean) {
        Log.dbg("Set launcher apps centering to: \"$v\"")
        config.appsCentering = v
        val cfg = File("${Mother.systemPath}/data/launcher/config.json")
        cfg.writeText(Json.encodeToString<LauncherConfig>(config))
        updateScreen(false)
    }
    fun loadConfig() {
        val cfg = File("${Mother.systemPath}/data/launcher/config.json")
        if(cfg.exists()) {
            config = Json.decodeFromString<LauncherConfig>(cfg.readText())
            //Check if background exists.
            if(!File("${Mother.systemPath}/data/launcher/${config.background}").exists())
                config.background="backgrounds/1.png"
        }
    }
}