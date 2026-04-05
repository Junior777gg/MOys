import app.BrowserApp
import app.CalculatorApp
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
    fun runLaunch() {
        mother.getRegisteredApps().forEach { app ->
            val appIcon = File(mother.getSystemPath()+"/install/${app.app_id}/${app.icon_file_name}")
            labels.add({
                label(
                    appIcon, app.app_name,
                    {
                        mother.runNewAppProcess(app.app_id, app.jar_file_name, app.activity_name)
                    })
            })
        }
        labels.add({
            label(
                icon = File(graphicService.getSystemResource("app/calculator.png")),
                run = { CalculatorApp(graphicService, StorageService(), deviceManager).main() },
                appName = "Калькулятор"
            )
        })
        labels.add({
            label(
                icon = File(graphicService.getSystemResource("app/storage.png")),
                run = { Storage(mother, graphicService, StorageService(), deviceManager).main()},
                appName = "Проводник"
            )
        })
        labels.add({
            label(
                icon = File(graphicService.getSystemResource("app/browser.png")),
                run = {
                    Thread {
                        Application.launch(BrowserApp::class.java)
                    }.start()
                },
                appName = "Браузер"
            )
        })
        labels.add({
            label(
                run = { TestApp(graphicService, StorageService(), deviceManager).main()},
                appName = "Scroll Test"
            )
        })
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
        graphicService.setContent{
            screen()
        }
        graphicService.redraw()
    }

    fun MutableList<View>.label(icon: File? = null, appName: String, run: () -> Unit) {
        Column(
            modifier = Modifier.padding(20).height(130).width(110)
                .onClick { run() }.background(Color(0,0,0,0)), this
        ).layout {
            Image(modifier = Modifier.size(70), icon ?: File(graphicService.getSystemResource("basic.png")), this)
            Text(modifier = Modifier.width(70).height(20), appName, 15, parent = this)
        }
    }

    fun showNewAppLabel(appName: String, jarName: String, activityName: String, appIcon: File? = null, appId: String) {
        labels.add({
            label(appIcon, appName) {
                mother.runNewAppProcess(appId, jarName, activityName)
            }
        })
        updateScreen()
    }
}