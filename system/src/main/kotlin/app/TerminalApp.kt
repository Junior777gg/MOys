package app

import Activity
import service.DeviceManager
import service.GraphicService
import Mother
import service.StorageService
import View
import Box
import TextField
import Button
import Row
import LazyColumn
import Text
import Image
import common.Log
import common.Color
import modifier.HorizontalAlignment
import modifier.Modifier
import modifier.TextAlignment
import modifier.background
import modifier.fillMaxSize
import modifier.fillMaxWidth
import modifier.height
import modifier.onClick
import modifier.paddingTop
import modifier.width
import java.io.File

class TerminalApp(
    val mother: Mother,
    override val gs: GraphicService,
    override val storage: StorageService,
    override val deviceManager: DeviceManager
) : Activity {
    var maxExecuteStore=1024
    var executeStore=mutableListOf<String>("Console Output")
    override fun main() {
        gs.setContent(true){
            buildUI()
        }
        gs.redraw()
    }
    fun updateUI() {
        gs.setContent(false){
            buildUI()
        }
        gs.redraw()
    }
    fun MutableList<View>.buildUI() {
        Box(modifier = Modifier.fillMaxSize().background(Color.BLACK), this).layout {
            Row(modifier = Modifier.fillMaxWidth().height(50).background(Color(0,0,0,0)), this).layout {
                val input = TextField(
                    modifier = Modifier.height(50).fillMaxWidth().background(Color.DARK_GRAY),
                    textSize = 16,
                    textAlign = TextAlignment.Left(),
                    textColor = Color.WHITE,
                    parent = this
                )
                Button(modifier = Modifier.background(Color.WHITE).onClick {
                    //Get command.
                    val t = input.text
                    if (t.isEmpty()) return@onClick
                    Log.info("TerminalApp Execute: $t")
                    executeStore.add("> $t")
                    //Execute command.
                    val process = ProcessBuilder("/bin/sh", "-c", t)
                        .redirectOutput(ProcessBuilder.Redirect.PIPE)
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .start()
                    val result = process.inputStream.bufferedReader().readText()
                    process.waitFor()
                    Log.info("TerminalApp Result: $result")
                    //Format.
                    if(!result.isEmpty()) {
                        val resultSplit = result.split("\n")
                        for (l in resultSplit) executeStore.add(l)
                    } else executeStore.add("Unknown or invalid command: $t")
                    while (executeStore.size > maxExecuteStore) executeStore.removeFirst()
                    updateUI()
                }.height(49).width(60), this).layout {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        file = File("${Mother.systemPath}/data/terminal/run.png"),
                        parent = this
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT).paddingTop(50),
                horizontalAlignment = HorizontalAlignment.Left(),
                parent = this
            ).layout {
                for (e in executeStore) {
                    Text(
                        modifier = Modifier.fillMaxWidth().height(20).background(Color.TRANSPARENT),
                        textAlign = TextAlignment.Left(),
                        textSize = 16,
                        text = e,
                        parent = this
                    )
                }
            }
        }
    }
}