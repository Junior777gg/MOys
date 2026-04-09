import common.Timer
import java.awt.Color
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SystemNavigation(val graphicService: GraphicService){
    fun setUpNavigation():MutableList<View>.() -> Unit {
        return {
            Row(modifier = Modifier.fillMaxSize().background(Color(0,0,0,0)),
                horizontalArrangement = HorizontalArrangement.SpaceEvenly(),
                verticalAlignment = VerticalAlignment.Bottom(),
                parent = this).layout {
                Row(modifier = Modifier.width(640).height(60).background(Color(0,0,0,120)),this).layout {
                    Button(modifier = Modifier.size(50).background(Color.CYAN).onClick { graphicService.popBackStack() },this).layout {}
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
                    Timer.subscribe({ time ->
                        {
                            clock.text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                            //graphicService.redraw()
                        }}, 60L)
                }
            }
        }
    }
}