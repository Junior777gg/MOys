import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.web.WebView
import javafx.stage.Stage

class BrowserApp : Application() {
    override fun start(primaryStage: Stage) {
        val webView = WebView()
        webView.engine.load("https://google.com")

        val root = BorderPane()
        root.center = webView

        primaryStage.scene = Scene(root, 640.0, 960.0)
        primaryStage.title = "Браузер"
        primaryStage.show()
    }
}