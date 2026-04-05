import app.BrowserApp
import app.CalculatorApp
import app.Settings
import app.Storage
import app.TestApp
import javafx.application.Application
import java.awt.Color
import java.io.File


class SystemLauncher(
    val graphicService: GraphicService,
    val deviceManager: DeviceManager,
    val mother: Mother
) {
    val labels = mutableListOf<MutableList<View>.() -> Unit>()
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
                icon = File(graphicService.getSystemResource("app/calculator.png")),
                click = { CalculatorApp(graphicService, StorageService(), deviceManager).main() },
                appName = "Калькулятор"
            )
        })
        labels.add({
            label(
                icon = File(graphicService.getSystemResource("app/settings.png")),
                click = { Settings(mother, graphicService, StorageService(), deviceManager).main()},
                appName = "Настройки"
            )
        })
        labels.add({
            label(
                icon = File(graphicService.getSystemResource("app/storage.png")),
                click = { Storage(mother, graphicService, StorageService(), deviceManager).main()},
                appName = "Проводник"
            )
        })
        labels.add({
            label(
                icon = File(graphicService.getSystemResource("app/browser.png")),
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
        updateLabels()
        graphicService.setContent(true) {
            screen()
        }
        graphicService.redraw()
    }

    fun MutableList<View>.screen() {
        Image(modifier = Modifier.fillMaxSize(), File(graphicService.getSystemResource("background.png")), this).layout {
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
        updateLabels()
        graphicService.setContent{
            screen()
        }
        graphicService.redraw()
    }

    fun MutableList<View>.label(icon: File? = null, appName: String, click: () -> Unit, hold: (() -> Unit) = {Log.warn("Can't remove system app")}) {
        Column(
            modifier = Modifier.padding(20).height(130).width(110)
                .onClick { click() }.onHold { hold.invoke() } .background(Color(0,0,0,0)), this
        ).layout {
            Image(modifier = Modifier.size(70), icon ?: File(graphicService.getSystemResource("basic.png")), this)
            Text(modifier = Modifier.width(70).height(20), appName, 15, parent = this)
        }
    }
}