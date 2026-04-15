package impl

import Activity
import LazyColumn
import Renderer
import navigation.SystemNavigation
import View
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import common.Bounds
import common.Log
import common.Stack
import common.Vec2i
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import modifier.FillMaxHeight
import modifier.Height
import service.GraphicService
import java.awt.Color
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.io.File
import javax.swing.JFrame

//The main graphical service. Controls the window, rendering, and input processing.
class GraphicServiceImpl : GLEventListener, GraphicService {
    @Serializable
    data class GraphicalConfig (
        var width: Int,
        var height: Int
    )
    companion object {
        var config: GraphicalConfig=GraphicalConfig(1920,1080)

        fun getScreenSize(): Vec2i=Vec2i(config.width,config.height)
        fun getScreenHeight(): Int=config.height
        fun getScreenWidth(): Int=config.width
        fun isDesktopResolution(): Boolean=config.width>config.height
    }
    object RESOLUTIONS {
        val R_144p= Vec2i(256, 144)
        val R_240p= Vec2i(426, 420)
        val R_360p= Vec2i(640, 360)
        val R_480p= Vec2i(640, 480)
        val R_960p= Vec2i(960, 640)
        val R_HD= Vec2i(1366, 768)
        val R_720p= Vec2i(1280, 720)
        val R_HD_PLUS= Vec2i(1600, 900)
        val R_FULL_HD= Vec2i(1920, 1080)
        val R_WUXGA= Vec2i(1920, 1200)
        val R_2K= Vec2i(2560, 1440)
        val R_WQXGA= Vec2i(2560, 1600)
        val R_UWQHD= Vec2i(3440, 1440)
        val R_4K= Vec2i(3840, 2160)
        val R_WQUXGA= Vec2i(3840, 2400)
        val R_5K= Vec2i(5120, 2880)
        val R_8K= Vec2i(7680, 4320)
        //All default resolutions in a list.
        val R_ALL=listOf<Vec2i>(R_360p,R_480p,R_960p,R_HD,R_720p,R_HD_PLUS,R_FULL_HD,R_WUXGA,R_2K)
    }
    private lateinit var canvas: GLCanvas
    private lateinit var frame: JFrame

    //The current View-element tree that is rendered on the screen
    private val viewTree = mutableListOf<View>()

    //Stack of screens for navigating "back"
    private val stack = Stack<MutableList<View>.() -> Unit>()

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
    private var focusedActivity: Activity? = null

    private val renderer = Renderer(this, bounds, lazyColumn, getScreenHeight(), getScreenWidth())
    private val navigationLambda = SystemNavigation(this).setUpNavigation()

    fun initialize(systemPath: String) {
        Log.dbg("Getting config")
        val cfg = File("${systemPath}/register/video.json")
        if(cfg.exists()) config = Json.Default.decodeFromString<GraphicalConfig>(cfg.readText())

        Log.dbg("Create JFRAME")
        frame = JFrame("MOys")

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.preferredSize = Dimension(getScreenWidth(), getScreenHeight())

        val profile = GLProfile.get(GLProfile.GL2)
        val capabilities = GLCapabilities(profile)

        capabilities.sampleBuffers = true
        capabilities.numSamples = 4
        Log.dbg("Done JFRAME")

        Log.dbg("Create GL_CANVAS")
        canvas = GLCanvas(capabilities)
        canvas.addGLEventListener(this)

        frame.add(canvas)
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
        frame.background = Color(255, 255, 255)
        Log.dbg("Done GL_CANVAS")

        canvas.requestFocusInWindow()

        canvas.addMouseMotionListener(object : MouseMotionAdapter() {
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
        canvas.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                val x = e!!.x.toDouble()
                val y = e.y.toDouble()
                if (!isMouseDragged){
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
        canvas.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {}
            override fun keyReleased(e: KeyEvent?) {}

            override fun keyPressed(e: KeyEvent?) {
                if(e?.keyChar==null) return
                val key=e.keyChar
                if(key=='q') popBackStack()
                else if(key=='w') clearStack()
                Log.dbg("Pressed key '$key'")
            }
        })

        Log.info("Graphical service initialized")
    }

    fun shutdown(systemPath: String) {
        val cfg = File("${systemPath}/register/video.json")
        cfg.writeText(Json.Default.encodeToString<GraphicalConfig>(config))
        Log.dbg("Saved graphical settings")
    }

    //Set focus on given [newActivity]. All callbacks will be executed from it.
    fun setActivity(newActivity: Activity? = null) {
        focusedActivity = newActivity
    }

    //Removes all stack elements aside from launcher.
    fun clearStack() {
        if (stack.size() <= 1) return
        focusedActivity=null
        while(stack.size()>1) stack.popBack()
        updateStack()
    }

    //Updates screen resolution and current screen.
    fun setScreenResolution(resolution: Vec2i) {
        val x=resolution.x
        val y=resolution.y
        if(x<1 || y<1) {
            Log.error("Invalid resolution: ${x}x${y}")
            return
        }
        if((x<256 && y<144)||(y<256 && x<144)) {
            Log.warn("Unusual resolution: ${x}x${y}. Rendering may be invalid")
        }
        config.width=x
        config.height=y
        Log.info("Set resolution to ${x}x${y}")
        frame.preferredSize= Dimension(x, y)
        renderer.clearCache()
        renderer.screenWidth=x
        renderer.screenHeight=y
        frame.validate()
        frame.pack()
        updateStack()
    }

    //Resets view tree and redraws current screen.
    fun updateStack() {
        viewTree.clear()
        bounds.clear()
        viewTreeUntilInject.clear()
        val lambda = stack.peek()
        viewTree.lambda()
        navigationLambda(viewTree)
        canvas.display()
    }

    //Sets the content of the screen. If itIsNewScreen=true, adds the screen to the navigation stack
    override fun setContent(itIsNewScreen: Boolean, lambda: MutableList<View>.() -> Unit) {
        viewTree.clear()
        lazyColumn.clear()
        viewTree.lambda()
        navigationLambda(viewTree)
        if (itIsNewScreen) {
            stack.push(lambda)
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
        if (stack.size() <= 1) return
        if (focusedActivity?.onNavigationBack()==true) stack.popBack()
        updateStack()
        if (stack.size() <= 1) focusedActivity = null
    }

    //Rerender screen must call after setContent or injectUI
    override fun redraw() {
        canvas.display()
    }

    //Searches for a clickable area by coordinates and calls onClick
    private fun handleClick(x: Double, y: Double) {
        val holdDuration = System.currentTimeMillis()-cursorHoldTimestamp
        for (bound in bounds.reversed()) {
            if (x in bound.x1..bound.x2 && y in bound.y1..bound.y2) {
                if(holdDuration<cursorHoldThreshold||bound.onHold==null) bound.onClick?.invoke()
                else bound.onHold.invoke()
                return
            }
        }
    }

    //Called on canvas initialization.
    override fun init(p0: GLAutoDrawable?) {
        val gl = p0!!.gl.gL2
        gl.glMatrixMode(GL2.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glOrtho(0.0, getScreenWidth().toDouble(),getScreenHeight().toDouble(),  0.0, -1.0, 1.0)
        gl.glMatrixMode(GL2.GL_MODELVIEW)
        gl.glEnable(GL2.GL_BLEND)
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA)
    }
    //Called when canvas is destroyed.
    override fun dispose(p0: GLAutoDrawable?) {}
    //Called to draw elements.
    override fun display(p0: GLAutoDrawable?) {
        if (viewTree.isEmpty()) return
        val gl = p0!!.gl.gL2
        gl.glClear(GL.GL_COLOR_BUFFER_BIT)
        lazyColumn.clear()
        bounds.clear()
        viewTree.forEach {
            renderer.parse(gl, it)
        }
        gl.glFlush()
    }
    //Called when canvas size was changed.
    override fun reshape(p0: GLAutoDrawable?, x: Int, y: Int, w: Int, h: Int) {
        val gl = p0!!.gl.gL2
        gl.glViewport(0, 0, w, h)
        gl.glMatrixMode(GL2.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glOrtho(0.0, w.toDouble(), h.toDouble(), 0.0, -1.0, 1.0)
        gl.glMatrixMode(GL2.GL_MODELVIEW)
        renderer.screenWidth = w
        renderer.screenHeight = h
    }
}