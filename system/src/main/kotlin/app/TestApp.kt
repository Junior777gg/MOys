package app

import Activity
import service.AudioService
import service.GraphicService
import service.StorageService
import service.DeviceManager
import Button
import Text
import TextField
import Column
import Image
import View
import common.Color
import impl.DeviceManagerImpl
import modifier.HorizontalAlignment
import modifier.Modifier
import modifier.TextAlignment
import modifier.VerticalArrangement
import modifier.background
import modifier.cornerRadius
import modifier.fillMaxSize
import modifier.fillMaxWidth
import modifier.height


class TestApp(
    override val gs: GraphicService,
    override val storage: StorageService,
    override val deviceManager: DeviceManager,
) : Activity {
    override fun main() {
        gs.setContent(true) {
            buildUI()
        }
        gs.redraw()
    }

    fun MutableList<View>.buildUI() {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.CYAN),
            parent = this, verticalArrangement = VerticalArrangement.SpaceEvenly(),
            horizontalAlignment = HorizontalAlignment.Left()
        ).layout {
            TextField(modifier = Modifier.fillMaxWidth().height(100).background(Color.ORANGE).cornerRadius(100), parent = this)
        }
    }

    fun updateUI() {
        gs.setContent {
            buildUI()
        }
        gs.redraw()
    }
}