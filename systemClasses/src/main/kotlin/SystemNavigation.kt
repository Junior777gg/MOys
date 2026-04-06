import java.awt.Color

class SystemNavigation(val graphicService: GraphicService){
    fun setUpNavigation():MutableList<View>.() -> Unit {
        return {
            Row(modifier = Modifier.fillMaxSize()
                .background(Color(0,0,0,0))
                , horizontalArrangement = HorizontalArrangement.SpaceEvenly(),
                verticalAlignment = VerticalAlignment.Bottom(), parent = this).layout {
                Row(modifier = Modifier.width(640).height(60).background(Color(0,0,0,120)),this).layout {
                    Button(modifier = Modifier.size(50).background(Color.CYAN).onClick { graphicService.popBackStack() },this).layout {}
                }
            }
        }
    }
}