package navigation

import Column
import Image
import Mother
import Row
import Text
import View
import app.BrowserApp
import app.CalculatorApp
import app.CameraApp
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
    ) {
        companion object {
            var Instance=LauncherConfig()
            fun save() {
                val cfg = File("${Mother.systemPath}/install/launcher/config.json")
                cfg.writeText(Json.encodeToString<LauncherConfig>(Instance))
            }
            fun load() {
                val cfg = File("${Mother.systemPath}/install/launcher/config.json")
                if(cfg.exists()) {
                    Instance=Json.decodeFromString<LauncherConfig>(cfg.readText())
                    //Check if background exists.
                    if(!File("${Mother.systemPath}/install/launcher/res/${Instance.background}").exists())
                        Instance.background="backgrounds/1.png"
                }
            }
        }
    }

    private val labels = mutableListOf<MutableList<View>.() -> Unit>()
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
                    val act=CalculatorApp(graphicService, StorageServiceImpl(), deviceManager, null)
                    act.main()
                    graphicService.setActivity(act)
                },
                appName = "Калькулятор"
            )
        })
        labels.add({
            label(
                icon = File("${Mother.systemPath}/install/settings/icon.png"),
                click = {
                    val act=SettingsApp(mother, graphicService, StorageServiceImpl(), deviceManager, null)
                    act.main()
                    graphicService.setActivity(act)
                },
                appName = "Настройки"
            )
        })
        labels.add({
            label(
                icon = File("${Mother.systemPath}/install/storage/icon.png"),
                click = {
                    val act=StorageApp(mother, graphicService, StorageServiceImpl(), deviceManager, null)
                    act.main()
                    graphicService.setActivity(act)
                },
                appName = "Проводник"
            )
        })
        labels.add({
            label(
                icon = File("${Mother.systemPath}/install/terminal/icon.png"),
                click = {
                    val act= TerminalApp(mother, graphicService, StorageServiceImpl(), deviceManager, null)
                    act.main()
                    graphicService.setActivity(act)
                },
                appName = "Терминал"
            )
        })
        labels.add({
            label(
                icon = File("${Mother.systemPath}/install/browser/icon.png"),
                click = {
                    Thread {
                        val browserApp = BrowserApp()
                        browserApp.init()
                        browserApp.createBrowser(graphicService)
                    }.start()
                },
                appName = "Браузер"
            )
        })
        labels.add({
            label(
                click = {
                    val act= CameraApp(graphicService, StorageServiceImpl(), deviceManager, null)
                    act.main()
                    graphicService.setActivity(act)
                },
                appName = "Камера"
            )
        })
        labels.add({
            label(
                click = {
                    val act=TestApp(graphicService, StorageServiceImpl(), deviceManager, null)
                    act.main()
                    graphicService.setActivity(act)
                },
                appName = "Testing App"
            )
        })
    }
    fun runLaunch() {
        LauncherConfig.load()
        updateLabels()
        graphicService.setContent(true) {
            screen()
        }
        graphicService.redraw()
    }

    private fun getAppsArrangement(): HorizontalArrangement {
        if(getAppsCentering()) return HorizontalArrangement.Center()
        else return HorizontalArrangement.Left()
    }

    fun MutableList<View>.screen() {
        Image(modifier = Modifier.fillMaxSize(), File(getBackgroundPath()), parent = this).layout {
            Column(modifier = Modifier.fillMaxSize().padding(10).background(Color.TRANSPARENT),
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
        LauncherConfig.load()
        updateLabels()
        graphicService.setContent(false) {
            screen()
        }
        if(redraw) graphicService.redraw()
    }

    fun MutableList<View>.label(icon: File? = null, appName: String, click: () -> Unit, hold: (() -> Unit) = { Log.warn("Can't remove system app")}) {
        var textColor = Color.WHITE
        if(getTextDark()) textColor = Color.BLACK
        Column(
            modifier = Modifier.padding(20).height(130).width(110)
                .onClick { click() }.onHold { hold.invoke() } .background(Color.TRANSPARENT), this
        ).layout {
            Image(modifier = Modifier.size(70), icon ?: File("${Mother.installPath}/launcher/res/basic.png"), parent = this)
            if(getTextDisplay()) Text(modifier = Modifier.width(70).height(20), text = appName, textColor = textColor, textSize = 14, parent = this)
        }
    }

    fun getBackgroundPath(): String {
        return "${Mother.installPath}/launcher/res/${getBackgroundRaw()}"
    }
    fun getBackgroundRaw(): String {
        return LauncherConfig.Instance.background
    }
    // Метод из второй версии, добавлен для совместимости
    fun getBackground(): String {
        return LauncherConfig.Instance.background
    }
    fun setBackground(path: String) {
        Log.dbg("Set launcher background as: \"$path\"")
        LauncherConfig.Instance.background=path
        LauncherConfig.save()
        updateScreen(false)
    }
    fun getTextDark(): Boolean {
        return LauncherConfig.Instance.textDark
    }
    fun setTextDark(v: Boolean) {
        Log.dbg("Set launcher text dark to: \"$v\"")
        LauncherConfig.Instance.textDark=v
        LauncherConfig.save()
        updateScreen(false)
    }
    fun getTextDisplay(): Boolean {
        return LauncherConfig.Instance.textDisplay
    }
    fun setTextDisplay(v: Boolean) {
        Log.dbg("Set launcher text display to: \"$v\"")
        LauncherConfig.Instance.textDisplay=v
        LauncherConfig.save()
        updateScreen(false)
    }
    fun getAppsCentering(): Boolean {
        return LauncherConfig.Instance.appsCentering
    }
    fun setAppsCentering(v: Boolean) {
        Log.dbg("Set launcher apps centering to: \"$v\"")
        LauncherConfig.Instance.appsCentering=v
        LauncherConfig.save()
        updateScreen(false)
    }
}