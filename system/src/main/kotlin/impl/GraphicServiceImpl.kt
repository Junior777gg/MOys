package impl

import Activity
import LazyColumn
import Renderer
import navigation.SystemNavigation
import View
import common.Bounds
import common.Log
import common.Stack
import common.SystemConfig
import common.Vec2i
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import modifier.FillMaxHeight
import modifier.Height
import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoView
import service.GraphicService
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.image.BufferedImage
import java.io.File
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

        fun getScreenSize(): Vec2i = Vec2i(config.width, config.height)
        fun getScreenHeight(): Int = config.height
        fun getScreenWidth(): Int = config.width
        fun isDesktopResolution(): Boolean = config.width > config.height
    }

    lateinit var frame: JFrame
    private lateinit var skikoLayer: SkiaLayer

    //The current View-element tree that is rendered on the screen
    private val viewTree = mutableListOf<View>()

    //Stack of screens for navigating "back"
    val runtimeStack = Stack<MutableList<View>.() -> Unit>()
    val activityStack = mutableMapOf<Class<Activity>, Activity>()

    //The list of clickable areas on the current frame
    private val bounds = mutableListOf<Bounds>()

    //Saved tree before calling injectUI (for restoration when cancelInject is called)
    private val viewTreeUntilInject = mutableListOf<View>()

    private val lazyColumn = mutableListOf<LazyColumn>()

    //After how much time click counts as hold
    private val cursorHoldThreshold = 400L
    private var cursorHoldTimestamp = 0L
    private var isMouseDragged = false
    private var lastMouseY = 0.0
    var focusedActivity: Activity? = null

    private val renderer = Renderer(this, bounds, lazyColumn, getScreenHeight(), getScreenWidth())
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
                lazyColumn.clear()
                bounds.clear()
                viewTree.forEach {
                    renderer.parse(canvas, it)
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
                if (lazyColumn.isNotEmpty()) {
                    isMouseDragged = true
                    val deltaY = e.y.toDouble() - lastMouseY
                    lastMouseY = e.y.toDouble()
                    val list = lazyColumn[0]
                    list.offset += deltaY
                    val containerHeight = list.modifier.get<Height>()?.height?.toDouble()
                        ?: list.modifier.get<FillMaxHeight>()?.let { getScreenHeight().toDouble() }
                        ?: getScreenHeight().toDouble()
                    list.updateScrollBound(containerHeight)
                    redraw()
                }
            }
        })
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
            override fun keyReleased(e: KeyEvent?) {}

            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyChar == null) return
                val key = e.keyChar
                if (key == 'q') popBackStack()
                else if (key == 'w') clearStack()
                Log.dbg("Pressed key '$key'")
            }
        })

        Log.info("Graphical service initialized")
    }

    fun shutdown(systemPath: String) {
        renderer.clearCacheFull()

        val cfg = File("${systemPath}/register/video.json")
        cfg.writeText(Json.encodeToString<GraphicalConfig>(config))
        Log.dbg("Saved graphical settings")
    }

    //Set focus on given [newActivity]. All callbacks will be executed from it.
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

    //Removes all stack elements aside from launcher.
    fun clearStack() {
        if (runtimeStack.size() <= 1) return
        focusedActivity?.lastState = viewTree.toMutableList()
        focusedActivity?.onDestroy()
        focusedActivity = null
        while (runtimeStack.size() > 1) runtimeStack.popBack()
        renderer.clearCacheFull()
        updateStack()
    }

    fun restore() {
        SwingUtilities.invokeLater {
            frame.contentPane.removeAll()
            frame.add(skikoLayer)
            frame.revalidate()
            skikoLayer.needRedraw()
        }
    }

    //Updates screen resolution and current screen.
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

    //Resets view tree and redraws current screen.
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

    //Sets the content of the screen. If itIsNewScreen=true, adds the screen to the navigation stack
    override fun setContent(itIsNewScreen: Boolean, lambda: MutableList<View>.() -> Unit) {
        viewTree.clear()
        lazyColumn.clear()
        viewTree.lambda()
        focusedActivity?.lastState = viewTree.toMutableList()
        navigation.setUpNavigation(viewTree)
        if (itIsNewScreen) {
            runtimeStack.push(lambda)
        }
    }

    //Adds UI on top of the current screen (such as a keyboard). Preserves the previous state
    override fun injectUI(lambda: MutableList<View>.() -> Unit) {
        viewTreeUntilInject.clear()
        viewTreeUntilInject.addAll(viewTree)
        viewTree.lambda()
    }

    //Removes the injected UI and restores the previous state
    override fun cancelInject() {
        viewTree.clear()
        bounds.clear()
        viewTree.addAll(viewTreeUntilInject)
    }

    //Return to the previous screen in the navigation stack
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
            updateStack()
        }
        if (runtimeStack.size() <= 1) {
            focusedActivity = null
            renderer.clearCacheFull()
        }
    }

    //Rerender screen must call after setContent or injectUI
    override fun redraw() {
        SwingUtilities.invokeLater {
            skikoLayer.needRedraw()
        }
    }

    //Searches for a clickable area by coordinates and calls onClick
    private fun handleClick(x: Double, y: Double) {
        val holdDuration = System.currentTimeMillis() - cursorHoldTimestamp
        for (bound in bounds.reversed()) {
            if (x in bound.x1..bound.x2 && y in bound.y1..bound.y2) {
                if (holdDuration < cursorHoldThreshold || bound.onHold == null) bound.onClick?.invoke()
                else bound.onHold.invoke()
                return
            }
        }
    }
}