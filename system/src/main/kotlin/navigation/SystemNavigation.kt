package navigation

import Image
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
import modifier.size
import modifier.width
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SystemNavigation(val graphicService: GraphicServiceImpl){
    var addedToTimerStack=false
    fun setUpNavigation():MutableList<View>.() -> Unit {
        return {
            Row(modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT),
                horizontalArrangement = HorizontalArrangement.SpaceEvenly(),
                verticalAlignment = VerticalAlignment.Bottom(),
                parent = this).layout {
                Row(modifier = Modifier.fillMaxWidth().height(60).background(Color(0,0,0,120)),
                    horizontalArrangement = HorizontalArrangement.SpaceEvenly(),
                    parent = this).layout {
                    Image(
                        modifier = Modifier.size(50).onClick { graphicService.popBackStack() },
                        file = File(System.getProperty("user.home") + "/MOys/data/launcher/navigation/back.png"),
                        parent = this
                    ).layout {}
                    Image(
                        modifier = Modifier.size(50).onClick { graphicService.clearStack() },
                        file = File(System.getProperty("user.home") + "/MOys/data/launcher/navigation/home.png"),
                        parent = this
                    ).layout {}
                }
            }
            Row(modifier = Modifier.fillMaxSize().background(Color.TRANSPARENT),
                horizontalArrangement = HorizontalArrangement.SpaceEvenly(),
                verticalAlignment = VerticalAlignment.Top(),
                parent = this).layout{
                Row(modifier = Modifier.fillMaxWidth().height(30).background(Color.TRANSPARENT),
                    verticalAlignment = VerticalAlignment.Center(),
                   horizontalArrangement = HorizontalArrangement.Left(),
                   parent = this).layout {
                    val clock = Text(modifier = Modifier.height(20).width(80), text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), textSize = 18, parent = this)
                    if(!addedToTimerStack) {
                        addedToTimerStack=true
                        TimerImpl.subscribe("TOP_PANEL_CLOCK",{ time ->
                            clock.text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                        }, 60L)
                    }
                }
            }
        }
    }
}