import java.awt.Color
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SystemNavigation(val graphicService: GraphicService){
    var addedToTimerStack=false
    fun setUpNavigation():MutableList<View>.() -> Unit {
        return {
            Row(modifier = Modifier.fillMaxSize().background(Color(0,0,0,0)),
                horizontalArrangement = HorizontalArrangement.SpaceEvenly(),
                verticalAlignment = VerticalAlignment.Bottom(),
                parent = this).layout {
                Row(modifier = Modifier.width(640).height(60).background(Color(0,0,0,120)),
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
            Row(modifier = Modifier.fillMaxSize().background(Color(0,0,0,0)),
                horizontalArrangement = HorizontalArrangement.SpaceEvenly(),
                verticalAlignment = VerticalAlignment.Top(),
                parent = this).layout {
                Row(
                    modifier = Modifier.width(640).height(30).background(Color(0, 0, 0, 0)),
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
                    if(!addedToTimerStack) {
                        addedToTimerStack=true
                        Timer.subscribe({ time ->
                            clock.text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                        }, 60L)
                    }
                }
            }
        }
    }
}