package navigation

import Column
import Image
import LazyColumn
import Row
import Text
import View
import common.Color
import impl.GraphicServiceImpl
import impl.TimerImpl
import modifier.HorizontalArrangement
import modifier.Modifier
import modifier.VerticalAlignment
import modifier.background
import modifier.fillMaxSize
import modifier.fillMaxWidth
import modifier.height
import modifier.onClick
import modifier.padding
import modifier.size
import modifier.width
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SystemNavigation(val graphicService: GraphicServiceImpl) {
    var addedToTimerStack = false
    var onTabs = false
    fun setUpNavigation(viewTree: MutableList<View>) {
        viewTree.showNavigation()
    }

    fun MutableList<View>.showNavigation() {
        Row(
            modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT),
            horizontalArrangement = HorizontalArrangement.SpaceEvenly(),
            verticalAlignment = VerticalAlignment.Bottom(),
            parent = this
        ).layout {
            Row(
                modifier = Modifier.fillMaxWidth().height(60).background(Color(0, 0, 0, 120)),
                horizontalArrangement = HorizontalArrangement.SpaceEvenly(),
                parent = this
            ).layout {
                Image(
                    modifier = Modifier.size(50).onClick {
                        if (onTabs) {
                            onTabs = false
                            graphicService.popBackStack()
                        } else {
                            onTabs = false
                            graphicService.popBackStack()
                        }
                    },
                    file = File(System.getProperty("user.home") + "/MOys/data/launcher/navigation/back.png"),
                    parent = this
                ).layout {}
                Image(
                    modifier = Modifier.size(50).onClick {
                        if (onTabs) {
                            onTabs = false
                            graphicService.activityStack.clear()
                            graphicService.clearStack()
                        } else {
                            onTabs = true
                            graphicService.setContent(true) {
                                graphicService.focusedActivity = null
                                this.showTabs()
                            }
                            graphicService.redraw()
                        }
                    },
                    file = File(System.getProperty("user.home") + "/MOys/data/launcher/navigation/home.png"),
                    parent = this
                ).layout {}
            }
        }
        Row(
            modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT),
            horizontalArrangement = HorizontalArrangement.SpaceEvenly(),
            verticalAlignment = VerticalAlignment.Top(),
            parent = this
        ).layout {
            Row(
                modifier = Modifier.fillMaxWidth().height(30).background(Color.TRANSPARENT),
                verticalAlignment = VerticalAlignment.Center(),
                horizontalArrangement = HorizontalArrangement.Left(),
                parent = this
            ).layout {
                val clock = Text(
                    modifier = Modifier.height(20).width(80),
                    text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                    textSize = 18,
                    parent = this
                )
                if (!addedToTimerStack) {
                    addedToTimerStack = true
                    TimerImpl.subscribe("TOP_PANEL_CLOCK", { time ->
                        clock.text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    }, 60L)
                }
            }
        }

    }

    fun MutableList<View>.showTabs() {
        Column(modifier = Modifier.fillMaxSize().background(Color.PINK), parent = this).layout {
            LazyColumn(modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT),this).layout {
                graphicService.activityStack.values.forEach{activity ->
                    Column(modifier = Modifier.size(400).padding(10).onClick {
                        graphicService.clearStack()
                        graphicService.focusedActivity = activity
                        graphicService.setContent(true) {
                            this.addAll(activity.lastState!!.toMutableList())
                        }
                        graphicService.redraw()
                        onTabs = false
                    }, parent = this).layout {
                        Text(modifier = Modifier.fillMaxSize(), text = activity.javaClass.name, textSize = 17 , textColor = Color.BLACK ,parent = this)
                    }
                }
            }
        }
    }
}