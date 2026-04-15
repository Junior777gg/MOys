package service

import View

interface GraphicService {
    fun setContent(itIsNewScreen: Boolean = false, lambda: MutableList<View>.() -> Unit)
    fun popBackStack()
    fun redraw()
    fun injectUI(lambda: MutableList<View>.() -> Unit)
    fun cancelInject()
}