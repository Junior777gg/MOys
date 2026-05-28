package impl

import Activity
import LazyColumn
import Renderer
import navigation.SystemNavigation
import View
import WebView
import common.Bounds
import common.Log
import common.Stack
import common.Vec2i
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import modifier.FillMaxHeight
import modifier.Height
import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoView
import web.WebViewEngine
import service.GraphicService
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.SwingUtilities

//The main graphical service. Controls the window, rendering, and input processing.
class GraphicServiceImpl : GraphicService {
    @Serializable
    data class GraphicalConfig(
        var width: Int,
        var height: Int
    )

    companion object {
        var config: GraphicalConfig = GraphicalConfig(1920, 1080)
    }

    //Rendering backend.
    lateinit var frame: JFrame
    private lateinit var skikoLayer: SkiaLayer

    //UI trees & stacks.
    private val viewTree = mutableListOf<View>()
    private val viewTreeUntilInject = mutableListOf<View>()
    private val lazyColumns = mutableListOf<LazyColumn>()
    val runtimeStack = Stack<MutableList<View>.() -> Unit>()
    val activityStack = mutableMapOf<Class<Activity>, Activity>()

    //After how much time click counts as hold.
    private val bounds = mutableListOf<Bounds>()
    private val cursorHoldThreshold = 400L
    private var cursorHoldTimestamp = 0L
    private var isMouseDragged = false
    private var lastMouseY = 0.0
    var focusedActivity: Activity? = null

    //Screenshoting.
    private var saveScreenshot = false
    private var screenshotPath = ""

    //renderer.Renderer holders.
    private val renderer = Renderer(this, bounds, lazyColumns, getScreenHeight(), getScreenWidth())
    private val navigation = SystemNavigation(this)

    fun initialize(systemPath: String) {
        Log.dbg("Getting config")
        val cfg = File("${systemPath}/register/video.json")
        if (cfg.exists()) config = Json.decodeFromString<GraphicalConfig>(cfg.readText())

        Log.dbg("Create JFRAME")
        frame = JFrame("MOys")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.preferredSize = Dimension(getScreenWidth(), getScreenHeight())
        val skikoView = object : SkikoView {
            override fun onRender(
                canvas: Canvas,
                width: Int,
                height: Int,
                nanoTime: Long
            ) {
                canvas.clear(org.jetbrains.skia.Color.makeRGB(0, 0, 0))
                renderer.screenWidth = width
                renderer.screenHeight = height

                if (viewTree.isEmpty()) return

                renderer.currentRenderTree.clear()
                lazyColumns.clear()
                bounds.clear()
                viewTree.forEach {
                    renderer.calculate(it)
                }

                renderer.currentRenderTree.forEach { renderNodes ->
                    renderer.draw(canvas, renderNodes)
                }

                renderer.currentAnimations.forEach { (animator, state) ->
                    if (!state.update()) {
                        renderer.draw(canvas, renderer.mapOfNodes[animator.parentView]!!)
                        redraw()
                    }
                }

                if(saveScreenshot) {
                    saveScreenshot=false
                    val screenshotBuffer = skikoLayer.screenshot()
                    if (screenshotBuffer != null) {
                        //Run on other thread to not take up additional render time.
                        Thread {
                            try {
                                //Get raw BGRA byte channels.
                                val bytes=screenshotBuffer.peekPixels()?.buffer?.bytes ?: return@Thread
                                //Instantiate image buffer.
                                val bufferedImage=BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                                val rasterData=(bufferedImage.raster.dataBuffer as DataBufferInt).data
                                //Process raw pixels manually.
                                //Skiko output format: Byte 0=B, Byte 1=G, Byte 2=R, Byte 3=A.
                                val size=width * height
                                for(i in 0 until size) {
                                    val byteOffset=i * 4
                                    if (byteOffset + 3 >= bytes.size) break
                                    //Offset bytes and get their pixel color value.
                                    val b=bytes[byteOffset+0].toInt() and 0xFF
                                    val g=bytes[byteOffset+1].toInt() and 0xFF
                                    val r=bytes[byteOffset+2].toInt() and 0xFF
                                    //val a = bytes[byteOffset + 3].toInt() and 0xFF //Isn't needed for RGB picture.
                                    //Pack channels into Java's standard packed INT representation (0xRRGGBB).
                                    rasterData[i]=(r shl 16) or (g shl 8) or b
                                }
                                //Write screenshot to given file.
                                ImageIO.write(bufferedImage, "png", File(screenshotPath))
                                Log.info("JFrame Image saved to $screenshotPath")
                            } catch (e: Exception) {
                                Log.error("JFrame Image couldn't be captured",e)
                                e.printStackTrace()
                            }
                        }.start()
                    }
                }
            }
        }
        skikoLayer = SkiaLayer().apply {
            preferredSize = Dimension(getScreenWidth(), getScreenHeight())
        }
        skikoLayer.skikoView = skikoView

        frame.add(skikoLayer)
        Log.dbg("Done JFRAME")
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
        skikoLayer.requestFocusInWindow()

        skikoLayer.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                val deltaY = e.y.toDouble() - lastMouseY
                lastMouseY = e.y.toDouble()

                if (fireScrollToWebViews(deltaY, e.x.toDouble(), e.y.toDouble(), false)) {
                    isMouseDragged = true
                    redraw()
                    return
                }

                if (lazyColumns.isNotEmpty()) {
                    isMouseDragged = true
                    val list = lazyColumns[0]
                    list.offset += deltaY
                    val containerHeight = list.modifier.get<Height>()?.height?.toDouble()
                        ?: list.modifier.get<FillMaxHeight>()?.let { getScreenHeight().toDouble() }
                        ?: getScreenHeight().toDouble()
                    list.updateScrollBound(containerHeight)
                    redraw()
                }
            }

            override fun mouseMoved(e: MouseEvent) {
                fireHoverToWebViews(e.x.toDouble(), e.y.toDouble())
            }
        })
        skikoLayer.addMouseWheelListener { e ->
            val deltaY = e.wheelRotation.toDouble()

            if (fireScrollToWebViews(deltaY, e.x.toDouble(), e.y.toDouble(), true)) {
                redraw()
                return@addMouseWheelListener
            }
            if (lazyColumns.isNotEmpty()) {
                val list = lazyColumns[0]
                list.offset += -deltaY * 25
                val containerHeight = list.modifier.get<Height>()?.height?.toDouble()
                    ?: list.modifier.get<FillMaxHeight>()?.let { getScreenHeight().toDouble() }
                    ?: getScreenHeight().toDouble()
                list.updateScrollBound(containerHeight)
                redraw()
            }
        }
        skikoLayer.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                val x = e!!.x.toDouble()
                val y = e.y.toDouble()
                if (!isMouseDragged) {
                    handleClick(x, y)
                    cursorHoldTimestamp = 0L
                }
                isMouseDragged = false
            }

            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                cursorHoldTimestamp = System.currentTimeMillis()
                lastMouseY = e!!.y.toDouble()
            }
        })
        skikoLayer.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {}

            override fun keyReleased(e: KeyEvent?) {
                if (e == null) return
                forwardKeyToWebViews(e.keyChar, e.keyCode, false)
            }

            override fun keyPressed(e: KeyEvent?) {
                if (e == null) return

                if (e.isControlDown) {
                    when (e.keyCode) {
                        KeyEvent.VK_Q -> { popBackStack(); return }
                        KeyEvent.VK_W -> { clearStack(); return }
                        KeyEvent.VK_H -> { takeScreenshot("${systemPath}/screenshot.png"); return }
                    }
                }

                if (forwardKeyToWebViews(e.keyChar, e.keyCode, true)) {
                    redraw()
                    return
                }

                Log.dbg("Pressed key '${e.keyChar}'")
            }
        })

        Log.info("Graphical service initialized")
    }

    /**Destroys rendering service.*/
    fun shutdown(systemPath: String) {
        renderer.clearCacheFull()

        val cfg = File("${systemPath}/register/video.json")
        cfg.writeText(Json.encodeToString<GraphicalConfig>(config))
        Log.dbg("Saved graphical settings")
    }

    /**Set focus on given [newActivity]. All callbacks will be executed from it.*/
    fun setActivity(newActivity: Activity? = null) {
        val activityClass = newActivity?.javaClass
        val existingActivity = activityStack[activityClass]

        if (existingActivity != null) {
            focusedActivity = existingActivity
            viewTree.clear()
            viewTree.addAll(existingActivity.lastState ?: mutableListOf())
            Log.dbg("Restored activity ${activityClass?.simpleName} with ${viewTree.size} views")
            redraw()
        } else {
            activityStack[activityClass!!] = newActivity
            focusedActivity = newActivity
            Log.dbg("Created new activity ${activityClass.simpleName}")
        }
    }

    /**Removes all stack elements aside from system navigation.*/
    fun clearStack() {
        if (runtimeStack.size() <= 1) return
        focusedActivity?.lastState = viewTree.toMutableList()
        focusedActivity?.onDestroy()
        focusedActivity = null
        while (runtimeStack.size() > 1) runtimeStack.popBack()
        WebViewEngine.disposeAll()
        renderer.clearCacheFull()
        updateStack()
    }

    /**Restores renderer context.*/
    fun restore() {
        SwingUtilities.invokeLater {
            frame.contentPane.removeAll()
            frame.add(skikoLayer)
            frame.revalidate()
            skikoLayer.needRedraw()
        }
    }

    /**Updates screen resolution and current screen.*/
    fun setScreenResolution(resolution: Vec2i) {
        val x = resolution.x
        val y = resolution.y
        if (x < 1 || y < 1) {
            Log.error("Invalid resolution: ${x}x${y}")
            return
        }
        if ((x < 256 && y < 144) || (y < 256 && x < 144)) {
            Log.warn("Unusual resolution: ${x}x${y}. Rendering may be invalid")
        }
        config.width = x
        config.height = y
        Log.info("Set resolution to ${x}x${y}")
        frame.preferredSize = Dimension(x, y)
        renderer.screenWidth = x
        renderer.screenHeight = y
        frame.validate()
        frame.pack()
        updateStack()
    }

    /**Resets view tree and redraws current screen.*/
    fun updateStack() {
        SwingUtilities.invokeLater {
            viewTree.clear()
            bounds.clear()
            viewTreeUntilInject.clear()
            val lambda = runtimeStack.peek()
            viewTree.lambda()
            navigation.setUpNavigation(viewTree)
            skikoLayer.needRedraw()
        }
    }

    /**Basically does a screenshot.*/
    fun takeScreenshot(path: String) {
        screenshotPath=path
        saveScreenshot=true
        skikoLayer.needRedraw()
    }

    /**Screen resolution helpers.*/
    override fun getScreenHeight(): Int = config.height
    override fun getScreenWidth(): Int = config.width

    /**Sets the content of the screen. If itIsNewScreen=true, adds the screen to the navigation stack*/
    override fun setContent(itIsNewScreen: Boolean, lambda: MutableList<View>.() -> Unit) {
        renderer.currentAnimations.clear()

        viewTree.clear()
        lazyColumns.clear()
        viewTree.lambda()
        focusedActivity?.lastState = viewTree.toMutableList()
        navigation.setUpNavigation(viewTree)
        if (itIsNewScreen) {
            runtimeStack.push(lambda)
        }
    }

    /**Adds UI on top of the current screen (such as a keyboard). Preserves the previous state*/
    override fun injectUI(lambda: MutableList<View>.() -> Unit) {
        viewTreeUntilInject.clear()
        viewTreeUntilInject.addAll(viewTree)
        viewTree.lambda()
    }

    /**Removes the injected UI and restores the previous state.*/
    override fun cancelInject() {
        viewTree.clear()
        bounds.clear()
        viewTree.addAll(viewTreeUntilInject)
    }

    /**Return to the previous screen in the navigation stack*/
    override fun popBackStack() {
        if (runtimeStack.size() <= 1) return
        if (viewTreeUntilInject.isNotEmpty()) {
            cancelInject()
        }
        focusedActivity?.lastState = viewTree.toMutableList()
        if (focusedActivity != null && !focusedActivity!!.onNavigationBack()) {
            null
        } else {
            focusedActivity?.onDestroy()
            runtimeStack.popBack()
            WebViewEngine.disposeAll()
            updateStack()
        }
        if (runtimeStack.size() <= 1) {
            focusedActivity = null
            renderer.clearCacheFull()
        }
    }

    /**Rerender screen must call after [setContent] or [injectUI].*/
    override fun redraw() {
        SwingUtilities.invokeLater {
            skikoLayer.needRedraw()
        }
    }

    /**Searches for a clickable area by coordinates and calls onClick*/
    private fun handleClick(x: Double, y: Double) {
        val holdDuration = System.currentTimeMillis() - cursorHoldTimestamp
        for (bound in bounds.reversed()) {
            if (x in bound.x1..bound.x2 && y in bound.y1..bound.y2) {
                if (holdDuration < cursorHoldThreshold || bound.onHold == null) bound.onClick?.invoke()
                else bound.onHold.invoke()
                return
            }
        }
        fireClickToWebViews(x,y)
    }
    /**Communicates given key to all WebViews*/
    private fun forwardKeyToWebViews(char: Char, code: Int, down: Boolean): Boolean {
        val webViews = collectWebViews(viewTree)
        if (webViews.isEmpty()) return false
        webViews.first().pendingInputs.add(
            WebViewInputEvent.Key(char, code, down)
        )
        return true
    }
    /**Communicates given scroll to all WebViews*/
    private fun fireScrollToWebViews(
        delta: Double,
        x: Double,
        y: Double,
        isWheelEvent: Boolean = false
    ): Boolean {
        val webViews = collectWebViews(viewTree)
        for (webView in webViews) {
            val bounds = webView.lastBounds ?: continue
            if (x >= bounds[0] && x <= bounds[2] &&
                y >= bounds[1] && y <= bounds[3]
            ) {
                isMouseDragged = true
                WebViewEngine.dispatchScroll(
                    webView,
                    (x - bounds[0]).toFloat(),
                    (y - bounds[1]).toFloat(),
                    delta.toFloat(),
                    isWheelEvent
                )
                return true
            }
        }
        return false
    }
    /**Communicates given hover position to all WebViews*/
    private fun fireHoverToWebViews(x: Double, y: Double) {
        val webViews = collectWebViews(viewTree)
        for (webView in webViews) {
            val bounds = webView.lastBounds ?: continue
            if (x >= bounds[0] && x <= bounds[2] && y >= bounds[1] && y <= bounds[3]) {
                val lx = (x - bounds[0]).toFloat()
                val ly = (y - bounds[1]).toFloat()
                //Queue a lightweight JS mousemove.
                javafx.application.Platform.runLater {
                    val holder = WebViewEngine.getHolder(webView) ?: return@runLater
                    holder.executeMouseMove(lx, ly)
                }
                return
            }
        }
    }
    /**Communicates given click position to all WebViews*/
    private fun fireClickToWebViews(x: Double, y: Double): Boolean {
        val webViews = collectWebViews(viewTree)
        if (webViews.isEmpty()) return false
        for (webView in webViews) {
            val bounds = webView.lastBounds ?: continue
            if (x >= bounds[0] && x <= bounds[2] && y >= bounds[1] && y <= bounds[3]) {
                WebViewEngine.dispatchClick(webView,
                    (x - bounds[0]).toFloat(),
                    (y - bounds[1]).toFloat())
                return true
            }
        }
        return false
    }
    /**Returns list of all WebViews*/
    private fun collectWebViews(roots: List<View>): List<WebView> {
        val out = mutableListOf<WebView>()
        fun walk(v: View) { if (v is WebView) out += v; v.children.forEach(::walk) }
        roots.forEach(::walk); return out
    }
}