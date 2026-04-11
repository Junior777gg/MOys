package app

import Activity
import DeviceManagerI
import GraphicServiceI
import Mother
import StorageServiceI
import View
import Box
import TextField
import Button
import Row
import LazyColumn
import Text
import Image
import background
import common.Log
import fillMaxHeight
import fillMaxSize
import fillMaxWidth
import height
import onClick
import width
import java.awt.Color
import java.io.File

class TerminalApp(
    val mother: Mother,
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0,0,0,0)),
                horizontalAlignment = HorizontalAlignment.Left(),
                parent = this
            ).layout {
                for (e in executeStore) {
                    Text(
                        modifier = Modifier.fillMaxWidth().height(20).background(Color(0,0,0,0)),
                        textAlign = Text.LEFT_CENTER,
                        textSize = 16,
                        text = e,
                        parent = this
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth().height(50).background(Color(0,0,0,0)), this).layout {
                val input = TextField(
                    modifier = Modifier.fillMaxSize().background(Color.DARK_GRAY),
                    textSize = 16,
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
                    val resultSplit = result.split("\n")
                    for (l in resultSplit) executeStore.add(l)
                    while (executeStore.size > maxExecuteStore) executeStore.removeFirst()
                    updateUI()
                }.fillMaxHeight().width(50), this).layout {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        file = File("${mother.getSystemPath()}/data/terminal/run.png"),
                        parent = this
                    )
                }
            }
        }
    }
}