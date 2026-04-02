import java.awt.Color

class SystemNavigation(val graphicService: GraphicService){
    fun setUpNavigation():MutableList<View>.() -> Unit {
        return {
            Row(modifier = Modifier.fillMaxSize()
                .background(Color(0,0,0,0))
                .childrenHeightCentering(Modifier.BOTTOM),this).layout {
                Row(modifier = Modifier.width(640).height(60).background(Color.BLACK),this).layout {
                    Button(modifier = Modifier.size(50).background(Color.CYAN).onClick { graphicService.popBackStack() },this).layout {}
                }
            }
        }
    }
}