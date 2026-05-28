package app

import Activity
import Column
import Text
import View
import WebView
import common.Color
import modifier.Modifier
import modifier.background
import modifier.fillMaxSize
import modifier.fillMaxWidth
import modifier.height
import service.DeviceManager
import service.GraphicService
import service.StorageService

class BrowserApp(
    override val gs: GraphicService,
    override val storage: StorageService,
    override val deviceManager: DeviceManager,
    override var lastState: MutableList<View>?
) : Activity {
    override fun main() {
        gs.setContent(itIsNewScreen = true) { buildUI() }
        gs.redraw()
    }
    private fun MutableList<View>.buildUI() {
        WebView(Modifier.fillMaxSize(),this, "https://yandex.ru/")
        //WebView(Modifier.fillMaxSize(), this, "data:text/html,<html><body style='background:red'><h1>Hello</h1></body></html>")
        Column(Modifier.fillMaxWidth().height(40).background(Color.TRANSPARENT), parent=this).layout {
            Text(Modifier.fillMaxWidth().height(15),text="Clicking currently doesn't work", textColor=Color.BLACK, parent=this)
            Text(Modifier.fillMaxWidth().height(15),text="Because of this Keyboard won't open too", textColor=Color.BLACK, parent=this)
        }
    }
}