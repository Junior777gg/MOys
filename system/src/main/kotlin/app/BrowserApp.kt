package app

import impl.GraphicServiceImpl
import impl.GraphicServiceImpl.Companion.getScreenHeight
import impl.GraphicServiceImpl.Companion.getScreenWidth
import io.ktor.util.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.web.WebView
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.SwingUtilities


class BrowserApp : JFXPanel() {
    init {
        javafx.application.Platform.setImplicitExit(false)
    }
    private lateinit var webView: WebView

    fun init() {
        javafx.application.Platform.runLater {
            webView = WebView()
            val webEngine = webView.engine
            webEngine.load("http://yandex.ru")

            val scene = Scene(webView)
            setScene(scene)
        }
    }

    fun createBrowser(gs: GraphicServiceImpl) {
        val button = JButton("[eq[eq")
        button.apply {
            size = Dimension(50, 50)
            addActionListener {
                javafx.application.Platform.runLater {
                    try {
                        webView.engine.history.go(-1)
                    }catch (e: IndexOutOfBoundsException) {
                        gs.restoreSkiko()
                    }
                }
            }
        }

        val frame = gs.frame
        SwingUtilities.invokeLater {
            frame.contentPane.removeAll()
            this.add(button)
            frame.add(this)
            frame.revalidate()
        }
    }

}